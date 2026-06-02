package com.combocounter;

import com.combocounter.model.ComboEntry;
import com.combocounter.repository.FileComboRepository;

import java.nio.file.Files;
import java.nio.file.Path;

public final class RepositorySmokeTest {
    private RepositorySmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("combocounter-test");
        Path dataFile = directory.resolve("combos.tsv");
        FileComboRepository repository = new FileComboRepository(dataFile);

        ComboEntry combo = new ComboEntry("Ryu", "DI Punish");
        ComboEntry saved = new ComboEntry(
                combo.id(),
                combo.characterName(),
                combo.title(),
                "Drive Impact punish counter",
                "HP > 214HP > 623HP",
                "Punish counter",
                "中央",
                "Normal",
                2740,
                0,
                0,
                "DI,確反",
                "最初に入れるサンプルとして保存層を確認する。",
                combo.createdAt(),
                combo.updatedAt()
        );

        repository.save(saved);

        FileComboRepository reloadedRepository = new FileComboRepository(dataFile);
        ComboEntry reloaded = reloadedRepository.findAll().getFirst();
        if (!"Ryu".equals(reloaded.characterName()) || reloaded.damage() != 2740 || !reloaded.inputs().contains("214HP")) {
            throw new IllegalStateException("Repository smoke test failed.");
        }

        reloadedRepository.delete(reloaded.id());
        if (!reloadedRepository.findAll().isEmpty()) {
            throw new IllegalStateException("Repository delete failed.");
        }

        System.out.println("Repository smoke test passed.");
    }
}
