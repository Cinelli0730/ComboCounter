package com.combocounter.ui;

import com.combocounter.model.ComboEntry;
import com.combocounter.repository.ComboRepository;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class MainFrame extends JFrame {
    private static final String[] DEFAULT_CHARACTERS = {
            "Ryu", "Luke", "Jamie", "Chun-Li", "Guile", "Kimberly", "Juri", "Ken", "Blanka", "Dhalsim",
            "E. Honda", "Dee Jay", "Manon", "Marisa", "JP", "Zangief", "Lily", "Cammy", "Rashid", "A.K.I.",
            "Ed", "Akuma", "M. Bison", "Terry", "Mai", "Elena"
    };

    private final ComboRepository repository;
    private final ComboTableModel tableModel = new ComboTableModel();
    private final JTable table = new JTable(tableModel);
    private final JComboBox<String> characterFilter = new JComboBox<>();
    private final JTextField searchField = new JTextField(22);
    private final JComboBox<String> characterField = new JComboBox<>(DEFAULT_CHARACTERS);
    private final JTextField titleField = new JTextField();
    private final JTextField starterField = new JTextField();
    private final JTextArea inputsArea = new JTextArea(5, 20);
    private final JTextField situationField = new JTextField();
    private final JComboBox<String> positionField = new JComboBox<>(new String[]{"中央", "端", "端背負い", "画面入れ替え", "どこでも"});
    private final JComboBox<String> difficultyField = new JComboBox<>(new String[]{"Easy", "Normal", "Hard", "Very Hard"});
    private final JSpinner damageField = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 10));
    private final JSpinner driveCostField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 6.0, 0.5));
    private final JSpinner superArtCostField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 3.0, 0.5));
    private final JTextField tagsField = new JTextField();
    private final JTextArea notesArea = new JTextArea(4, 20);

    private String selectedId;
    private List<ComboEntry> allCombos = List.of();

    public MainFrame(ComboRepository repository) {
        super("ComboCounter - Street Fighter 6");
        this.repository = repository;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 680));
        setLocationByPlatform(true);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createEditorPanel(), BorderLayout.EAST);

        configureEvents();
        reloadCombos();
        pack();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.add(new JLabel("キャラ"));
        characterFilter.setPrototypeDisplayValue("キャラクターすべて");
        toolbar.add(characterFilter);
        toolbar.add(new JLabel("検索"));
        toolbar.add(searchField);

        JButton refreshButton = new JButton("再読み込み");
        refreshButton.addActionListener(event -> reloadCombos());
        toolbar.add(refreshButton);
        return toolbar;
    }

    private JScrollPane createTablePanel() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(26);
        table.getColumnModel().getColumn(0).setPreferredWidth(95);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(8).setPreferredWidth(160);
        return new JScrollPane(table);
    }

    private JPanel createEditorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(390, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int row = 0;
        row = addField(panel, row, "キャラ", characterField);
        row = addField(panel, row, "タイトル", titleField);
        row = addField(panel, row, "始動", starterField);
        row = addArea(panel, row, "入力", inputsArea);
        row = addField(panel, row, "状況", situationField);
        row = addField(panel, row, "位置", positionField);
        row = addField(panel, row, "難度", difficultyField);
        row = addField(panel, row, "ダメージ", damageField);
        row = addField(panel, row, "Drive消費", driveCostField);
        row = addField(panel, row, "SA消費", superArtCostField);
        row = addField(panel, row, "タグ", tagsField);
        row = addArea(panel, row, "メモ", notesArea);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton newButton = new JButton("新規");
        JButton saveButton = new JButton("保存");
        JButton deleteButton = new JButton("削除");
        newButton.addActionListener(event -> clearForm());
        saveButton.addActionListener(event -> saveForm());
        deleteButton.addActionListener(event -> deleteSelected());
        buttons.add(newButton);
        buttons.add(saveButton);
        buttons.add(deleteButton);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(10, 0, 0, 0);
        panel.add(buttons, constraints);

        return panel;
    }

    private int addField(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints labelConstraints = baseConstraints(row);
        labelConstraints.gridx = 0;
        labelConstraints.weightx = 0;
        labelConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = baseConstraints(row);
        fieldConstraints.gridx = 1;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, fieldConstraints);
        return row + 1;
    }

    private int addArea(JPanel panel, int row, String label, JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(260, label.equals("入力") ? 110 : 85));
        return addField(panel, row, label, scrollPane);
    }

    private GridBagConstraints baseConstraints(int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row;
        constraints.insets = new Insets(4, 4, 4, 4);
        return constraints;
    }

    private void configureEvents() {
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    fillForm(tableModel.rowAt(modelRow));
                }
            }
        });

        characterFilter.addActionListener(event -> applyFilters());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                applyFilters();
            }
        });
    }

    private void reloadCombos() {
        try {
            allCombos = repository.findAll().stream()
                    .sorted(Comparator.comparing(ComboEntry::updatedAt).reversed())
                    .toList();
            updateCharacterFilter();
            applyFilters();
        } catch (IOException | IllegalArgumentException ex) {
            showError("データの読み込みに失敗しました。", ex);
        }
    }

    private void updateCharacterFilter() {
        String current = (String) characterFilter.getSelectedItem();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("すべて");
        allCombos.stream()
                .map(ComboEntry::characterName)
                .filter(value -> !value.isBlank())
                .distinct()
                .sorted()
                .forEach(model::addElement);
        characterFilter.setModel(model);
        if (current != null) {
            characterFilter.setSelectedItem(current);
        }
    }

    private void applyFilters() {
        String character = String.valueOf(characterFilter.getSelectedItem());
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);

        List<ComboEntry> filtered = allCombos.stream()
                .filter(combo -> "すべて".equals(character) || combo.characterName().equals(character))
                .filter(combo -> query.isEmpty() || searchableText(combo).contains(query))
                .toList();
        tableModel.setRows(filtered);
    }

    private String searchableText(ComboEntry combo) {
        return String.join(" ",
                combo.characterName(),
                combo.title(),
                combo.starter(),
                combo.inputs(),
                combo.situation(),
                combo.position(),
                combo.difficulty(),
                combo.tags(),
                combo.notes()
        ).toLowerCase(Locale.ROOT);
    }

    private void fillForm(ComboEntry combo) {
        selectedId = combo.id();
        characterField.setSelectedItem(combo.characterName());
        titleField.setText(combo.title());
        starterField.setText(combo.starter());
        inputsArea.setText(combo.inputs());
        situationField.setText(combo.situation());
        positionField.setSelectedItem(combo.position().isBlank() ? "中央" : combo.position());
        difficultyField.setSelectedItem(combo.difficulty().isBlank() ? "Normal" : combo.difficulty());
        damageField.setValue(combo.damage());
        driveCostField.setValue(combo.driveCost());
        superArtCostField.setValue(combo.superArtCost());
        tagsField.setText(combo.tags());
        notesArea.setText(combo.notes());
    }

    private void clearForm() {
        selectedId = null;
        table.clearSelection();
        characterField.setSelectedIndex(0);
        titleField.setText("");
        starterField.setText("");
        inputsArea.setText("");
        situationField.setText("");
        positionField.setSelectedIndex(0);
        difficultyField.setSelectedIndex(1);
        damageField.setValue(0);
        driveCostField.setValue(0.0);
        superArtCostField.setValue(0.0);
        tagsField.setText("");
        notesArea.setText("");
    }

    private void saveForm() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "タイトルを入力してください。", "入力エラー", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ComboEntry combo = selectedId == null
                ? new ComboEntry(String.valueOf(characterField.getSelectedItem()), title)
                : findSelectedOrNew(title);
        ComboEntry formValues = new ComboEntry(
                combo.id(),
                String.valueOf(characterField.getSelectedItem()),
                title,
                starterField.getText(),
                inputsArea.getText(),
                situationField.getText(),
                String.valueOf(positionField.getSelectedItem()),
                String.valueOf(difficultyField.getSelectedItem()),
                ((Number) damageField.getValue()).intValue(),
                ((Number) driveCostField.getValue()).doubleValue(),
                ((Number) superArtCostField.getValue()).doubleValue(),
                tagsField.getText(),
                notesArea.getText(),
                combo.createdAt(),
                combo.updatedAt()
        );

        try {
            repository.save(formValues);
            selectedId = formValues.id();
            reloadCombos();
            selectRow(selectedId);
        } catch (IOException ex) {
            showError("保存に失敗しました。", ex);
        }
    }

    private ComboEntry findSelectedOrNew(String title) {
        return allCombos.stream()
                .filter(combo -> combo.id().equals(selectedId))
                .findFirst()
                .orElseGet(() -> new ComboEntry(String.valueOf(characterField.getSelectedItem()), title));
    }

    private void deleteSelected() {
        if (selectedId == null) {
            return;
        }
        int result = JOptionPane.showConfirmDialog(this, "選択中のコンボを削除しますか？", "削除確認", JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            repository.delete(selectedId);
            clearForm();
            reloadCombos();
        } catch (IOException ex) {
            showError("削除に失敗しました。", ex);
        }
    }

    private void selectRow(String id) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (tableModel.rowAt(row).id().equals(id)) {
                int viewRow = table.convertRowIndexToView(row);
                table.setRowSelectionInterval(viewRow, viewRow);
                table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                return;
            }
        }
    }

    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(this, message + System.lineSeparator() + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
    }
}
