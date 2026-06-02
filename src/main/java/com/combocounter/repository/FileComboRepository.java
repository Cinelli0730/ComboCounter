package com.combocounter.repository;

import com.combocounter.model.ComboEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class FileComboRepository implements ComboRepository {
    private static final int FIELD_COUNT = 15;

    private final Path filePath;
    private final Map<String, ComboEntry> combos = new LinkedHashMap<>();
    private boolean loaded;

    public FileComboRepository(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public synchronized List<ComboEntry> findAll() throws IOException {
        ensureLoaded();
        return combos.values().stream().map(ComboEntry::copy).toList();
    }

    @Override
    public synchronized Optional<ComboEntry> findById(String id) throws IOException {
        ensureLoaded();
        ComboEntry combo = combos.get(id);
        return combo == null ? Optional.empty() : Optional.of(combo.copy());
    }

    @Override
    public synchronized void save(ComboEntry combo) throws IOException {
        ensureLoaded();
        ComboEntry current = combos.get(combo.id());
        if (current == null) {
            combos.put(combo.id(), combo.copy());
        } else {
            current.updateFrom(combo);
        }
        persist();
    }

    @Override
    public synchronized void delete(String id) throws IOException {
        ensureLoaded();
        combos.remove(id);
        persist();
    }

    private void ensureLoaded() throws IOException {
        if (loaded) {
            return;
        }
        loaded = true;
        if (!Files.exists(filePath)) {
            return;
        }

        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            ComboEntry combo = decode(line);
            combos.put(combo.id(), combo);
        }
    }

    private void persist() throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        List<String> lines = new ArrayList<>();
        for (ComboEntry combo : combos.values()) {
            lines.add(encode(combo));
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }

    private static String encode(ComboEntry combo) {
        return String.join("\t",
                field(combo.id()),
                field(combo.characterName()),
                field(combo.title()),
                field(combo.starter()),
                field(combo.inputs()),
                field(combo.situation()),
                field(combo.position()),
                field(combo.difficulty()),
                field(Integer.toString(combo.damage())),
                field(Double.toString(combo.driveCost())),
                field(Double.toString(combo.superArtCost())),
                field(combo.tags()),
                field(combo.notes()),
                field(combo.createdAt().toString()),
                field(combo.updatedAt().toString())
        );
    }

    private static ComboEntry decode(String line) {
        String[] parts = line.split("\t", -1);
        if (parts.length != FIELD_COUNT) {
            throw new IllegalArgumentException("Invalid combo record: " + line);
        }
        return new ComboEntry(
                value(parts[0]),
                value(parts[1]),
                value(parts[2]),
                value(parts[3]),
                value(parts[4]),
                value(parts[5]),
                value(parts[6]),
                value(parts[7]),
                parseInt(value(parts[8])),
                parseDouble(value(parts[9])),
                parseDouble(value(parts[10])),
                value(parts[11]),
                value(parts[12]),
                Instant.parse(value(parts[13])),
                Instant.parse(value(parts[14]))
        );
    }

    private static String field(String value) {
        return Base64.getUrlEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String value(String field) {
        return new String(Base64.getUrlDecoder().decode(field), StandardCharsets.UTF_8);
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
