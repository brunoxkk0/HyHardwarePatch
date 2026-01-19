package br.dev.brunoxkk0;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.plugin.early.ClassTransformer;
import org.objectweb.asm.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HytaleServerTransformer implements ClassTransformer {

    private static final int THREAD_COUNT =
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2);

    @Nullable
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String path, @Nonnull byte[] bytes) {

        if (!name.equals("com.hypixel.hytale.server.core.HytaleServer"))
            return bytes;

        HytaleLogger.get("HyHardwarePatch").atInfo().log("(SCHEDULED_EXECUTOR) updated to " + THREAD_COUNT + " threads");

        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(
                reader,
                ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES
        );

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {

            @Override
            public MethodVisitor visitMethod(
                    int access,
                    String methodName,
                    String descriptor,
                    String signature,
                    String[] exceptions
            ) {

                MethodVisitor mv = super.visitMethod(
                        access, methodName, descriptor, signature, exceptions
                );

                if (!methodName.equals("<clinit>"))
                    return mv;

                return new MethodVisitor(Opcodes.ASM9, mv) {

                    @Override
                    public void visitMethodInsn(
                            int opcode,
                            String owner,
                            String name,
                            String descriptor,
                            boolean isInterface
                    ) {

                        if (opcode == Opcodes.INVOKESTATIC
                                && owner.equals("java/util/concurrent/Executors")
                                && name.equals("newSingleThreadScheduledExecutor")
                                && descriptor.equals(
                                "(Ljava/util/concurrent/ThreadFactory;)" +
                                        "Ljava/util/concurrent/ScheduledExecutorService;"
                        )) {

                            super.visitLdcInsn(THREAD_COUNT);

                            super.visitInsn(Opcodes.SWAP);

                            super.visitMethodInsn(
                                    Opcodes.INVOKESTATIC,
                                    "java/util/concurrent/Executors",
                                    "newScheduledThreadPool",
                                    "(ILjava/util/concurrent/ThreadFactory;)" +
                                            "Ljava/util/concurrent/ScheduledExecutorService;",
                                    false
                            );
                            return;
                        }

                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }
                };
            }
        };

        reader.accept(visitor, 0);
        return writer.toByteArray();
    }

    @Override
    public int priority() {
        return -99;
    }
}
