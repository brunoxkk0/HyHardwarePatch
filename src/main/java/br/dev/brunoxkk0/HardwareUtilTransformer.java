package br.dev.brunoxkk0;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.plugin.early.ClassTransformer;
import org.objectweb.asm.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HardwareUtilTransformer implements ClassTransformer {

    @Nullable
    @Override
    public byte[] transform(
            @Nonnull String name,
            @Nonnull String path,
            @Nonnull byte[] bytes
    ) {

        if (!name.equals("com.hypixel.hytale.common.util.HardwareUtil")) {
            return bytes;
        }

        HytaleLogger.get("HyHardwarePatch")
                .atInfo()
                .log("Replacing HardwareUtil.getUUID() with custom implementation (Java 25 compatible)");

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

                // Remove o método original
                if (methodName.equals("getUUID")
                        && descriptor.equals("()Ljava/util/UUID;")) {
                    return null;
                }

                return super.visitMethod(
                        access, methodName, descriptor, signature, exceptions
                );
            }

            @Override
            public void visitEnd() {

                MethodVisitor mv = super.visitMethod(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                        "getUUID",
                        "()Ljava/util/UUID;",
                        null,
                        null
                );

                mv.visitCode();

                /*
                 * File file = new File("SystemUUID.txt");
                 */
                mv.visitTypeInsn(Opcodes.NEW, "java/io/File");
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn("SystemUUID.txt");
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL,
                        "java/io/File",
                        "<init>",
                        "(Ljava/lang/String;)V",
                        false
                );
                mv.visitVarInsn(Opcodes.ASTORE, 0);

                Label tryStart = new Label();
                Label tryEnd = new Label();
                Label catchBlock = new Label();

                mv.visitTryCatchBlock(
                        tryStart,
                        tryEnd,
                        catchBlock,
                        "java/lang/Exception"
                );

                mv.visitLabel(tryStart);

                /*
                 * if (!file.exists())
                 */
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/File",
                        "exists",
                        "()Z",
                        false
                );
                Label fileExists = new Label();
                mv.visitJumpInsn(Opcodes.IFNE, fileExists);

                /*
                 * Files.writeString(
                 *   file.toPath(),
                 *   UUID.randomUUID().toString(),
                 *   UTF_8,
                 *   CREATE, TRUNCATE_EXISTING
                 * );
                 */
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/File",
                        "toPath",
                        "()Ljava/nio/file/Path;",
                        false
                );

                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/util/UUID",
                        "randomUUID",
                        "()Ljava/util/UUID;",
                        false
                );
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/util/UUID",
                        "toString",
                        "()Ljava/lang/String;",
                        false
                );

                mv.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "java/nio/charset/StandardCharsets",
                        "UTF_8",
                        "Ljava/nio/charset/Charset;"
                );

                mv.visitInsn(Opcodes.ICONST_2);
                mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/nio/file/OpenOption");

                mv.visitInsn(Opcodes.DUP);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "java/nio/file/StandardOpenOption",
                        "CREATE",
                        "Ljava/nio/file/StandardOpenOption;"
                );
                mv.visitInsn(Opcodes.AASTORE);

                mv.visitInsn(Opcodes.DUP);
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "java/nio/file/StandardOpenOption",
                        "TRUNCATE_EXISTING",
                        "Ljava/nio/file/StandardOpenOption;"
                );
                mv.visitInsn(Opcodes.AASTORE);

                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/nio/file/Files",
                        "writeString",
                        "(Ljava/nio/file/Path;Ljava/lang/CharSequence;Ljava/nio/charset/Charset;[Ljava/nio/file/OpenOption;)Ljava/nio/file/Path;",
                        false
                );
                mv.visitInsn(Opcodes.POP);

                mv.visitLabel(fileExists);

                /*
                 * String uuid = Files.readString(path, UTF_8).trim();
                 */
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/File",
                        "toPath",
                        "()Ljava/nio/file/Path;",
                        false
                );

                mv.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "java/nio/charset/StandardCharsets",
                        "UTF_8",
                        "Ljava/nio/charset/Charset;"
                );

                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/nio/file/Files",
                        "readString",
                        "(Ljava/nio/file/Path;Ljava/nio/charset/Charset;)Ljava/lang/String;",
                        false
                );

                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/String",
                        "trim",
                        "()Ljava/lang/String;",
                        false
                );

                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/util/UUID",
                        "fromString",
                        "(Ljava/lang/String;)Ljava/util/UUID;",
                        false
                );

                mv.visitInsn(Opcodes.ARETURN);

                mv.visitLabel(tryEnd);

                /*
                 * catch (Exception e)
                 */
                mv.visitLabel(catchBlock);
                mv.visitVarInsn(Opcodes.ASTORE, 1);
                mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn("Failed to load system UUID");
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL,
                        "java/lang/RuntimeException",
                        "<init>",
                        "(Ljava/lang/String;Ljava/lang/Throwable;)V",
                        false
                );
                mv.visitInsn(Opcodes.ATHROW);

                mv.visitMaxs(0, 0);
                mv.visitEnd();

                super.visitEnd();
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
