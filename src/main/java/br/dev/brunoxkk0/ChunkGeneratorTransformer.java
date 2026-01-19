package br.dev.brunoxkk0;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.plugin.early.ClassTransformer;
import org.objectweb.asm.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkGeneratorTransformer implements ClassTransformer {

    @Nullable
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String path, @Nonnull byte[] bytes) {

        if (!name.equals("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator"))
            return bytes;

        int processors = Runtime.getRuntime().availableProcessors();

        int originalPoolSize = Math.max(
                2,
                fastCeil(processors * 0.75F)
        );

        int newPoolSize = Math.max(
                2,
                fastCeil(processors * .25F)
        );

        HytaleLogger.get("HyHardwarePatch").atInfo().log("(POOL_SIZE) original: " + originalPoolSize + " | changed to: " + newPoolSize);

        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {

            @Override
            public MethodVisitor visitMethod(
                    int access,
                    String methodName,
                    String descriptor,
                    String signature,
                    String[] exceptions
            ) {

                MethodVisitor mv = super.visitMethod(access, methodName, descriptor, signature, exceptions);

                if (!methodName.equals("<clinit>"))
                    return mv;

                return new MethodVisitor(Opcodes.ASM9, mv) {

                    @Override
                    public void visitLdcInsn(Object value) {

                        if (value instanceof Float && ((Float) value) == 0.75F) {
                            super.visitLdcInsn(0.25F); // novo multiplicador
                            return;
                        }

                        super.visitLdcInsn(value);
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

    private static int fastCeil(float f) {
        int i = (int) f;
        if (f > 0.0F && f != (float) i) {
            return f > (float) Integer.MAX_VALUE ? Integer.MAX_VALUE : i + 1;
        } else {
            return i;
        }
    }

}
