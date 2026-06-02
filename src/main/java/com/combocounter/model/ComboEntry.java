package com.combocounter.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ComboEntry {
    private final String id;
    private String characterName;
    private String title;
    private String starter;
    private String inputs;
    private String situation;
    private String position;
    private String difficulty;
    private int damage;
    private double driveCost;
    private double superArtCost;
    private String tags;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;

    public ComboEntry(String characterName, String title) {
        this(UUID.randomUUID().toString(), characterName, title, "", "", "", "", "", 0, 0, 0, "", "", Instant.now(), Instant.now());
    }

    public ComboEntry(
            String id,
            String characterName,
            String title,
            String starter,
            String inputs,
            String situation,
            String position,
            String difficulty,
            int damage,
            double driveCost,
            double superArtCost,
            String tags,
            String notes,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = requireText(id, "id");
        this.characterName = clean(characterName);
        this.title = clean(title);
        this.starter = clean(starter);
        this.inputs = clean(inputs);
        this.situation = clean(situation);
        this.position = clean(position);
        this.difficulty = clean(difficulty);
        this.damage = Math.max(0, damage);
        this.driveCost = Math.max(0, driveCost);
        this.superArtCost = Math.max(0, superArtCost);
        this.tags = clean(tags);
        this.notes = clean(notes);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public String id() {
        return id;
    }

    public String characterName() {
        return characterName;
    }

    public String title() {
        return title;
    }

    public String starter() {
        return starter;
    }

    public String inputs() {
        return inputs;
    }

    public String situation() {
        return situation;
    }

    public String position() {
        return position;
    }

    public String difficulty() {
        return difficulty;
    }

    public int damage() {
        return damage;
    }

    public double driveCost() {
        return driveCost;
    }

    public double superArtCost() {
        return superArtCost;
    }

    public String tags() {
        return tags;
    }

    public String notes() {
        return notes;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public void updateFrom(ComboEntry source) {
        this.characterName = clean(source.characterName);
        this.title = clean(source.title);
        this.starter = clean(source.starter);
        this.inputs = clean(source.inputs);
        this.situation = clean(source.situation);
        this.position = clean(source.position);
        this.difficulty = clean(source.difficulty);
        this.damage = Math.max(0, source.damage);
        this.driveCost = Math.max(0, source.driveCost);
        this.superArtCost = Math.max(0, source.superArtCost);
        this.tags = clean(source.tags);
        this.notes = clean(source.notes);
        this.updatedAt = Instant.now();
    }

    public ComboEntry copy() {
        return new ComboEntry(id, characterName, title, starter, inputs, situation, position, difficulty, damage, driveCost, superArtCost, tags, notes, createdAt, updatedAt);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String requireText(String value, String field) {
        String cleaned = clean(value);
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return cleaned;
    }
}
