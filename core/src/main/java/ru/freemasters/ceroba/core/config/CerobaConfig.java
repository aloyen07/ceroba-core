package ru.freemasters.ceroba.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CerobaConfig {

    public static CerobaConfig load(File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (file.exists()) {

            try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                return gson.fromJson(reader, CerobaConfig.class);
            }

        } else {

            CerobaConfig config = new CerobaConfig();
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
                gson.toJson(config, writer);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save default configuration to " + file.getAbsolutePath(), e);
            }

            return config;
        }
    }

    public static void save(@NotNull CerobaConfig config, File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            gson.toJson(config, writer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save configuration to " + file.getAbsolutePath(), e);
        }
    }

    @SerializedName("useInternalVoskModel")
    private boolean useInternalVoskModel = true;

    @SerializedName("voskModelPath")
    private String voskModelPath = "ceroba-assets/models/vosk-model-small-ru-0.22.zip";

}
