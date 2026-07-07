package de.paulb.chestesp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

final class ChestEspConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("chest-esp.json");

    boolean enabled = true;
    boolean chests = true;
    boolean enderChests = true;
    boolean barrels = true;
    boolean copperChests = true;

    static ChestEspConfig load() {
        if (!Files.exists(PATH)) {
            ChestEspConfig config = new ChestEspConfig();
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            ChestEspConfig config = GSON.fromJson(reader, ChestEspConfig.class);
            return config == null ? new ChestEspConfig() : config;
        } catch (IOException ignored) {
            return new ChestEspConfig();
        }
    }

    void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
        }
    }
}
