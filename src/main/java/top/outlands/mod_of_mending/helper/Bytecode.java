package top.outlands.mod_of_mending.helper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import top.outlands.mod_of_mending.common.ModOfMending;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The handful of class-file edits this mod performs, without a bytecode framework that needs its own compiler,
 * class pool and stack-map bookkeeping.
 *
 * <p>Everything here is total: a class that does not match is returned untouched, and a failure returns the input
 * rather than the exception. A transformer whose target moved or disappeared must degrade to "no patch", not to a
 * crash - that is also what the javassist code these replace did, by accident of a {@code catch (Throwable)}.
 *
 * <p>None of these edits changes control flow, so stack map frames and max stack of the original class stay valid and
 * {@code ClassWriter(reader, 0)} can be used verbatim. That matters: {@code COMPUTE_MAXS} simulates every method (a
 * 77KB class costs ~1.8ms) and {@code COMPUTE_FRAMES} additionally needs {@code getCommonSuperClass}, which loads
 * classes while classes are being transformed.
 */
public final class Bytecode {

    /** {@code -Dmod_of_mending.verify=true} runs the JDK's own verifier over every patched class. */
    private static final boolean VERIFY = Boolean.getBoolean("mod_of_mending.verify");

    /**
     * Escape hatch for anything the shapes above do not cover: hand the parsed class to the caller and write it back.
     * Nothing is computed for you, so keep the same rules - no control flow changes, no {@code COMPUTE_FRAMES}.
     */
    public static byte[] edit(byte[] bytes, Consumer<ClassNode> edit) {
        try {
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode(Opcodes.ASM9);
            reader.accept(node, 0);
            edit.accept(node);
            ClassWriter writer = new ClassWriter(reader, 0);
            node.accept(writer);
            return verify(writer.toByteArray(), "edit on " + node.name);
        } catch (Throwable t) {
            ModOfMending.LOGGER.error("[Bytecode] edit failed", t);
            return bytes;
        }
    }

    /**
     * Replaces the body of every matching method with a call to a static helper, forwarding the parameters in order -
     * the {@code setBody("{return SomeHelper#method($$);}")} idiom. Nothing is emitted when the helper has no method
     * with the same parameters and return type, which is how a snippet that stopped fitting dies: untouched class, one
     * warning, no runtime surprise.
     */
    public static byte[] forwardBody(byte[] bytes, Predicate<MethodNode> match, String owner, String name) {
        return replaceBody(bytes, match, method -> {
            if (!canForward(method, owner, name)) {
                ModOfMending.LOGGER.warn("[Bytecode] {} has no compatible {}{}, leaving {} alone", owner, name,
                        method.desc, method.name);
                return;
            }
            int slot = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
            for (Type argument : Type.getArgumentTypes(method.desc)) {
                method.instructions.add(new VarInsnNode(argument.getOpcode(Opcodes.ILOAD), slot));
                slot += argument.getSize();
            }
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, method.desc, false));
            method.instructions.add(new InsnNode(Type.getReturnType(method.desc).getOpcode(Opcodes.IRETURN)));
            method.maxStack = Math.max(argumentSlots(method.desc), 0) + Type.getReturnType(method.desc).getSize();
        });
    }

    private static int argumentSlots(String desc) {
        int slots = 0;
        for (Type argument : Type.getArgumentTypes(desc)) {
            slots += argument.getSize();
        }
        return slots;
    }

    private static boolean canForward(MethodNode method, String owner, String name) {
        try {
            Class<?> type = Class.forName(owner.replace('/', '.'), false, Bytecode.class.getClassLoader());
            Class<?>[] parameters = new Class<?>[Type.getArgumentTypes(method.desc).length];
            Type[] types = Type.getArgumentTypes(method.desc);
            for (int i = 0; i < types.length; i++) {
                parameters[i] = load(types[i]);
            }
            type.getMethod(name, parameters);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Throwable t) {
            // the helper lives in this mod, so this is "cannot happen" rather than "does not fit"
            ModOfMending.LOGGER.warn("[Bytecode] could not check {}.{}", owner, name, t);
            return false;
        }
    }

    private static Class<?> load(Type type) throws ClassNotFoundException {
        return switch (type.getSort()) {
            case Type.BOOLEAN -> boolean.class;
            case Type.BYTE -> byte.class;
            case Type.CHAR -> char.class;
            case Type.SHORT -> short.class;
            case Type.INT -> int.class;
            case Type.LONG -> long.class;
            case Type.FLOAT -> float.class;
            case Type.DOUBLE -> double.class;
            case Type.VOID -> void.class;
            case Type.ARRAY -> Class.forName(type.getDescriptor().replace('/', '.'));
            default -> Class.forName(type.getClassName());
        };
    }

    /** {@code INVOKESTATIC owner.name desc}-shaped call site. */
    public record Call(int opcode, String owner, String name, String desc) {
        public boolean isStatic() {
            return opcode == Opcodes.INVOKESTATIC;
        }
    }

    /**
     * Replaces the body of every matching method: the {@code setBody("{}")} idiom, generalised. The body is responsible
     * for its own {@code maxStack}; the original {@code maxLocals} is kept, which already covers the arguments.
     */
    public static byte[] replaceBody(byte[] bytes, Predicate<MethodNode> match, Consumer<MethodNode> body) {
        try {
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode(Opcodes.ASM9);
            reader.accept(node, 0);
            boolean touched = false;
            for (MethodNode method : node.methods) {
                if (!match.test(method) || (method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
                    continue;
                }
                // the body is built aside: a body that decides not to emit anything must leave the method untouched,
                // not produce a method whose code falls off the end
                MethodNode replacement = new MethodNode(method.access, method.name, method.desc, method.signature,
                        method.exceptions == null ? null : method.exceptions.toArray(new String[0]));
                replacement.maxLocals = method.maxLocals;
                replacement.maxStack = method.maxStack;
                body.accept(replacement);
                if (replacement.instructions.size() == 0) {
                    ModOfMending.LOGGER.warn("[Bytecode] nothing emitted for {}, leaving it alone", method.name);
                    continue;
                }
                method.instructions = replacement.instructions;
                method.tryCatchBlocks = replacement.tryCatchBlocks;
                method.localVariables = replacement.localVariables;
                method.maxStack = Math.max(replacement.maxStack, method.maxStack);
                method.maxLocals = Math.max(replacement.maxLocals, method.maxLocals);
                touched = true;
            }
            if (!touched) {
                return bytes;
            }
            ClassWriter writer = new ClassWriter(reader, 0);
            node.accept(writer);
            return verify(writer.toByteArray(), "replaceBody on " + node.name);
        } catch (Throwable t) {
            ModOfMending.LOGGER.error("[Bytecode] replaceBody failed", t);
            return bytes;
        }
    }

    /**
     * Makes a method do nothing: {@code void} returns, everything else returns the default value. Constructors are
     * refused - emptying one removes the {@code super()} call and the class stops verifying.
     *
     * @param desc may be {@code null} to match by name alone
     */
    public static byte[] emptyBody(byte[] bytes, String name, String desc) {
        if (name.equals("<init>")) {
            ModOfMending.LOGGER.warn("[Bytecode] refusing to empty a constructor ({})", name);
            return bytes;
        }
        return replaceBody(bytes, method -> method.name.equals(name)
                && (desc == null || method.desc.equals(desc)), Bytecode::empty);
    }

    public static byte[] emptyBody(byte[] bytes, String name) {
        return emptyBody(bytes, name, null);
    }

    /**
     * Empties constructors, keeping the mandatory {@code super()} call - what {@code setBody("{}")} means for a
     * constructor, and the reason it is not just {@link #emptyBody}.
     */
    public static byte[] emptyConstructor(byte[] bytes, Predicate<MethodNode> match) {
        return edit(bytes, node -> {
            for (MethodNode method : node.methods) {
                if (!method.name.equals("<init>") || !match.test(method)) {
                    continue;
                }
                clear(method);
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, node.superName, "<init>",
                        "()V", false));
                method.instructions.add(new InsnNode(Opcodes.RETURN));
                method.maxStack = 1;
            }
        });
    }

    /** Adds a method, the {@code CtMethod.make(...)} idiom. */
    public static byte[] addMethod(byte[] bytes, int access, String name, String desc, Consumer<MethodNode> body) {
        try {
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode(Opcodes.ASM9);
            reader.accept(node, 0);
            for (MethodNode existing : node.methods) {
                if (existing.name.equals(name) && existing.desc.equals(desc)) {
                    ModOfMending.LOGGER.debug("[Bytecode] {} already declares {}{}", node.name, name, desc);
                    return bytes;
                }
            }
            MethodNode method = new MethodNode(access, name, desc, null, null);
            // an instance method reserves slot 0 for "this"; leaving maxLocals at 0 is invalid, not "computed later"
            method.maxLocals = ((access & Opcodes.ACC_STATIC) != 0 ? 0 : 1) + argumentSlots(desc);
            body.accept(method);
            node.methods.add(method);
            ClassWriter writer = new ClassWriter(reader, 0);
            node.accept(writer);
            return verify(writer.toByteArray(), "addMethod " + node.name + "." + name);
        } catch (Throwable t) {
            ModOfMending.LOGGER.error("[Bytecode] addMethod failed", t);
            return bytes;
        }
    }

    /**
     * Redirects a call to another implementation with the same descriptor, the {@code m.replace("$_ = Other#same($$);")}
     * idiom. The opcode is taken from the call site by default: javassist hides whether a method is static, and a
     * static method in the same class looks exactly like an instance method until the verifier complains.
     */
    public static byte[] redirectCall(byte[] bytes, Predicate<Call> match, String owner, String name, String desc) {
        return redirectCall(bytes, null, match, owner, name, desc, -1);
    }

    /** @param opcode {@code -1} keeps the opcode of the call site */
    public static byte[] redirectCall(byte[] bytes, Predicate<Call> match, String owner, String name, String desc,
            int opcode) {
        return redirectCall(bytes, null, match, owner, name, desc, opcode);
    }

    /** @param inMethod restricts the rewrite to matching methods, {@code null} means the whole class */
    public static byte[] redirectCall(byte[] bytes, Predicate<MethodNode> inMethod, Predicate<Call> match,
            String owner, String name, String desc) {
        return redirectCall(bytes, inMethod, match, owner, name, desc, -1);
    }

    /** @param opcode {@code -1} keeps the opcode of the call site */
    public static byte[] redirectCall(byte[] bytes, Predicate<MethodNode> inMethod, Predicate<Call> match,
            String owner, String name, String desc, int opcode) {
        return transform(bytes, inMethod, (mv, call) -> {
            if (!match.test(call) || !desc.equals(call.desc())) {
                return false;
            }
            int op = opcode == -1 ? call.opcode() : opcode;
            mv.visitMethodInsn(op, owner, name, desc, op == Opcodes.INVOKEINTERFACE);
            return true;
        });
    }

    /**
     * Replaces a call statement with something that builds its own value, the {@code m.replace("$_ = null;")} /
     * {@code replace("")} / "different arguments" idiom. The receiver and arguments are popped, so the net stack effect
     * stays zero and the frames of the surrounding code stay valid.
     */
    public static byte[] replaceCallStatement(byte[] bytes, Predicate<Call> match, Consumer<MethodVisitor> replacement) {
        return replaceCallStatement(bytes, null, match, replacement);
    }

    /** @param inMethod restricts the rewrite to matching methods, {@code null} means the whole class */
    public static byte[] replaceCallStatement(byte[] bytes, Predicate<MethodNode> inMethod, Predicate<Call> match,
            Consumer<MethodVisitor> replacement) {
        return transform(bytes, inMethod, (mv, call) -> {
            if (!match.test(call)) {
                return false;
            }
            if (!call.isStatic()) {
                mv.visitInsn(Opcodes.POP);
            }
            Type[] args = Type.getArgumentTypes(call.desc());
            for (int i = args.length - 1; i >= 0; i--) {
                pop(mv, args[i]);
            }
            replacement.accept(mv);
            return true;
        });
    }

    /**
     * Drops everything that describes the old body: instructions, exception handlers and the local variable tables.
     * Annotations, parameters, signature and {@code maxLocals} stay - they belong to the method, not to its code.
     */
    public static void clear(MethodNode method) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) {
            method.localVariables.clear();
        }
        method.visibleLocalVariableAnnotations = null;
        method.invisibleLocalVariableAnnotations = null;
    }

    private static void empty(MethodNode method) {
        Type returns = Type.getReturnType(method.desc);
        switch (returns.getSort()) {
            case Type.VOID -> method.instructions.add(new InsnNode(Opcodes.RETURN));
            case Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> {
                method.instructions.add(new InsnNode(Opcodes.ICONST_0));
                method.instructions.add(new InsnNode(Opcodes.IRETURN));
            }
            case Type.LONG -> {
                method.instructions.add(new InsnNode(Opcodes.LCONST_0));
                method.instructions.add(new InsnNode(Opcodes.LRETURN));
            }
            case Type.FLOAT -> {
                method.instructions.add(new InsnNode(Opcodes.FCONST_0));
                method.instructions.add(new InsnNode(Opcodes.FRETURN));
            }
            case Type.DOUBLE -> {
                method.instructions.add(new InsnNode(Opcodes.DCONST_0));
                method.instructions.add(new InsnNode(Opcodes.DRETURN));
            }
            default -> {
                method.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
                method.instructions.add(new InsnNode(Opcodes.ARETURN));
            }
        }
        method.maxStack = returns.getSize();
        // maxLocals is left alone: the original value already covers the arguments, and shrinking it is not our job
    }

    private interface CallSiteEdit {
        boolean apply(MethodVisitor mv, Call call);
    }

    private static byte[] transform(byte[] bytes, Predicate<MethodNode> inMethod, CallSiteEdit edit) {
        try {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, 0);
            boolean[] touched = {false};
            reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
                    MethodVisitor mv = super.visitMethod(access, name, desc, sig, ex);
                    if (mv == null || (inMethod != null
                            && !inMethod.test(new MethodNode(access, name, desc, sig, ex)))) {
                        return mv;
                    }
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                            if (edit.apply(mv, new Call(opcode, owner, name, desc))) {
                                touched[0] = true;
                            } else {
                                super.visitMethodInsn(opcode, owner, name, desc, itf);
                            }
                        }
                    };
                }
            }, 0);
            return touched[0] ? verify(writer.toByteArray(), "transform") : bytes;
        } catch (Throwable t) {
            ModOfMending.LOGGER.error("[Bytecode] transform failed", t);
            return bytes;
        }
    }

    private static void pop(MethodVisitor mv, Type type) {
        if (type.getSize() == 2) {
            mv.visitInsn(Opcodes.POP2);
        } else if (type.getSize() == 1) {
            mv.visitInsn(Opcodes.POP);
        }
    }

    /**
     * The JDK ships a class-file verifier; {@code java.lang.classfile.ClassFile#verify} returns the errors instead of
     * throwing, which makes it usable as a self-check and as a test oracle. Off unless the property is set.
     */
    private static byte[] verify(byte[] patched, String what) {
        if (!VERIFY) {
            return patched;
        }
        try {
            var errors = java.lang.classfile.ClassFile.of().verify(patched);
            if (errors.isEmpty()) {
                ModOfMending.LOGGER.info("[Bytecode] {} verified", what);
            } else {
                ModOfMending.LOGGER.error("[Bytecode] {} produced {} verify errors, first: {}", what, errors.size(),
                        errors.get(0));
            }
        } catch (Throwable t) {
            ModOfMending.LOGGER.warn("[Bytecode] could not verify {}", what, t);
        }
        return patched;
    }

    private Bytecode() {}
}
