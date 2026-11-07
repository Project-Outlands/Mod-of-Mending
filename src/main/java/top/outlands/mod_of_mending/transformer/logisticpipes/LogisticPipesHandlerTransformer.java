package top.outlands.mod_of_mending.transformer.logisticpipes;

import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * LogisticsPipes bails out of a broken class by calling {@code exitJava} or by printing the stack trace and carrying
 * on with half-initialised state; both calls are dropped, the method keeps running.
 */
public class LogisticPipesHandlerTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.replaceCallStatement(bytes,
                call -> call.name().equals("exitJava") || call.name().equals("printStackTrace"), mv -> {});
    }
}
