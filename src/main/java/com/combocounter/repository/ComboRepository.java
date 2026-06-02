package com.combocounter.repository;

import com.combocounter.model.ComboEntry;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ComboRepository {
    List<ComboEntry> findAll() throws IOException;

    Optional<ComboEntry> findById(String id) throws IOException;

    void save(ComboEntry combo) throws IOException;

    void delete(String id) throws IOException;
}
