package br.dev.brunoxkk0.mixins;

import com.hypixel.hytale.common.util.HardwareUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

@Mixin(HardwareUtil.class)
public class HardwareUtilsMixin {

    /**
     * @author brunoxkk0
     * @reason This plugin provides a custom method for obtaining the UUID from the system, which is necessary in isolated contexts such as Docker containers.
     */
    @Nullable
    @Overwrite
    public static UUID getUUID() {

        File file = new File("SystemUUID.txt");

        try {
            if (!file.exists()) {

                if (file.getParentFile() != null)
                    file.getParentFile().mkdirs();

                try (BufferedWriter writer = Files.newBufferedWriter(
                        file.toPath(), StandardCharsets.UTF_8)) {
                    writer.write(UUID.randomUUID().toString());
                }
            }

            try (BufferedReader reader = Files.newBufferedReader(
                    file.toPath(), StandardCharsets.UTF_8)) {

                String uuid = reader.readLine();
                if (uuid == null || uuid.isEmpty()) {
                    throw new IllegalStateException("UUID file is empty");
                }
                return UUID.fromString(uuid);
            }

        } catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException("Failed to load system UUID", e);
        }

    }


}