package com.combocounter.ui;

import com.combocounter.model.ComboEntry;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

final class ComboTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"キャラ", "タイトル", "始動", "ダメージ", "Drive", "SA", "位置", "難易度", "タグ"};

    private final List<ComboEntry> rows = new ArrayList<>();

    void setRows(List<ComboEntry> combos) {
        rows.clear();
        rows.addAll(combos);
        fireTableDataChanged();
    }

    ComboEntry rowAt(int rowIndex) {
        return rows.get(rowIndex).copy();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ComboEntry combo = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> combo.characterName();
            case 1 -> combo.title();
            case 2 -> combo.starter();
            case 3 -> combo.damage();
            case 4 -> combo.driveCost();
            case 5 -> combo.superArtCost();
            case 6 -> combo.position();
            case 7 -> combo.difficulty();
            case 8 -> combo.tags();
            default -> "";
        };
    }
}
