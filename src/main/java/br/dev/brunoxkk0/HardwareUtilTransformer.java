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

        if (!name.equals("com.hypixel.hytale.common.util.HardwareUtil"))
            return bytes;

        HytaleLogger.get("HyHardwarePatch").atInfo().log("Implementing custom Hardware UUID calculation method.");

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

                /*
                 * Remove o método original getUUID()
                 */
                if (methodName.equals("getUUID")
                        && descriptor.equals("()Ljava/util/UUID;")) {

                    // NÃO chama super.visitMethod → método removido
                    return null;
                }

                return super.visitMethod(
                        access, methodName, descriptor, signature, exceptions
                );
            }

            @Override
            public void visitEnd() {
                /*
                 * Injeta o novo método getUUID()
                 */
                MethodVisitor mv = super.visitMethod(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                        "getUUID",
                        "()Ljava/util/UUID;",
                        null,
                        null
                );

                mv.visitCode();

                /*
                 * Corpo do método gerado via ASMifier
                 * (equivalente 1:1 ao código Java do mixin)
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
                 * UUID.randomUUID().toString() → write file
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
                        "newBufferedWriter",
                        "(Ljava/nio/file/Path;Ljava/nio/charset/Charset;)Ljava/io/BufferedWriter;",
                        false
                );
                mv.visitVarInsn(Opcodes.ASTORE, 1);

                mv.visitVarInsn(Opcodes.ALOAD, 1);
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
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/BufferedWriter",
                        "write",
                        "(Ljava/lang/String;)V",
                        false
                );
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/BufferedWriter",
                        "close",
                        "()V",
                        false
                );

                mv.visitLabel(fileExists);

                /*
                 * Read UUID and return
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
                        "newBufferedReader",
                        "(Ljava/nio/file/Path;Ljava/nio/charset/Charset;)Ljava/io/BufferedReader;",
                        false
                );
                mv.visitVarInsn(Opcodes.ASTORE, 2);

                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/BufferedReader",
                        "readLine",
                        "()Ljava/lang/String;",
                        false
                );
                mv.visitVarInsn(Opcodes.ASTORE, 3);

                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/BufferedReader",
                        "close",
                        "()V",
                        false
                );

                mv.visitVarInsn(Opcodes.ALOAD, 3);
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/util/UUID",
                        "fromString",
                        "(Ljava/lang/String;)Ljava/util/UUID;",
                        false
                );
                mv.visitInsn(Opcodes.ARETURN);

                mv.visitLabel(tryEnd);

                mv.visitLabel(catchBlock);
                mv.visitVarInsn(Opcodes.ASTORE, 4);
                mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn("Failed to load system UUID");
                mv.visitVarInsn(Opcodes.ALOAD, 4);
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
