package com.combocounter.ui;

import com.combocounter.model.ComboEntry;
import com.combocounter.repository.ComboRepository;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class MainFrame extends JFrame {
    private static final String CARD_CHARACTER_SELECT = "characterSelect";
    private static final String CARD_COMBOS = "combos";
    private static final String INPUT_SEPARATOR = " => ";
    private static final String[] DEFAULT_CHARACTERS = {
            "Ryu", "Luke", "Jamie", "Chun-Li", "Guile", "Kimberly", "Juri", "Ken", "Blanka", "Dhalsim",
            "E. Honda", "Dee Jay", "Manon", "Marisa", "JP", "Zangief", "Lily", "Cammy", "Rashid", "A.K.I.",
            "Ed", "Akuma", "M. Bison", "Terry", "Mai", "Elena"
    };
    private static final String[] STARTER_OPTIONS = {"通常食らい", "カウンター", "パニカン", "ガード"};
    private static final String[] POSITION_OPTIONS = {"中央", "端", "端背負い", "画面入れ替え", "どこでも"};
    private static final String[] ATTACK_BUTTONS = {"弱P", "中P", "強P", "弱K", "中K", "強K"};
    private static final String[] NUMPAD_BUTTONS = {"7", "8", "9", "4", "5", "6", "1", "2", "3", "N", "0", "+"};

    private final ComboRepository repository;
    private final ComboTableModel tableModel = new ComboTableModel();
    private final JTable table = new JTable(tableModel);
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final JLabel selectedCharacterLabel = new JLabel();
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> characterField = new JComboBox<>(DEFAULT_CHARACTERS);
    private final JTextField titleField = new JTextField(16);
    private final JComboBox<String> starterField = new JComboBox<>(STARTER_OPTIONS);
    private final JPanel inputStepsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
    private final JTextField situationField = new JTextField(11);
    private final JComboBox<String> positionField = new JComboBox<>(POSITION_OPTIONS);
    private final JComboBox<String> difficultyField = new JComboBox<>(new String[]{"Easy", "Normal", "Hard", "Very Hard"});
    private final JSpinner damageField = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 10));
    private final JSpinner driveCostField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 6.0, 0.5));
    private final JSpinner superArtCostField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 3.0, 0.5));
    private final JTextField tagsField = new JTextField(12);
    private final JTextArea notesArea = new JTextArea(4, 20);
    private final List<JTextField> inputStepFields = new ArrayList<>();

    private JTextField activeInputStep;
    private String selectedCharacter = DEFAULT_CHARACTERS[0];
    private String selectedId;
    private List<ComboEntry> allCombos = List.of();

    public MainFrame(ComboRepository repository) {
        super("ComboCounter - Street Fighter 6");
        this.repository = repository;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setLocationByPlatform(true);

        cards.add(createCharacterSelectPanel(), CARD_CHARACTER_SELECT);
        cards.add(createComboPanel(), CARD_COMBOS);
        add(cards, BorderLayout.CENTER);

        configureEvents();
        reloadCombos();
        showCharacterSelect();
        pack();
    }

    private JPanel createCharacterSelectPanel() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("キャラクター選択");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        root.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 6, 10, 10));
        for (String character : DEFAULT_CHARACTERS) {
            JButton button = new JButton(character, new CharacterIcon(character));
            button.setHorizontalTextPosition(JButton.CENTER);
            button.setVerticalTextPosition(JButton.BOTTOM);
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(136, 112));
            button.addActionListener(event -> openCharacter(character));
            grid.add(button);
        }
        root.add(new JScrollPane(grid), BorderLayout.CENTER);
        return root;
    }

    private JPanel createComboPanel() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(createComboToolbar(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(createTablePanel(), BorderLayout.CENTER);
        center.add(createNotesPanel(), BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);
        root.add(createInputButtonPanel(), BorderLayout.EAST);
        return root;
    }

    private JPanel createComboToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

        JButton backButton = new JButton("キャラ選択");
        backButton.addActionListener(event -> showCharacterSelect());
        toolbar.add(backButton);

        selectedCharacterLabel.setFont(selectedCharacterLabel.getFont().deriveFont(Font.BOLD, 18f));
        toolbar.add(selectedCharacterLabel);

        toolbar.add(new JLabel("タイトル"));
        toolbar.add(titleField);
        toolbar.add(new JLabel("始動"));
        toolbar.add(starterField);
        toolbar.add(new JLabel("状況"));
        toolbar.add(situationField);
        toolbar.add(new JLabel("位置"));
        toolbar.add(positionField);
        toolbar.add(new JLabel("ダメージ"));
        toolbar.add(damageField);
        toolbar.add(new JLabel("タグ"));
        toolbar.add(tagsField);
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
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(8).setPreferredWidth(160);
        return new JScrollPane(table);
    }

    private JPanel createNotesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        int row = 0;
        row = addField(panel, row, "キャラ", characterField);
        row = addField(panel, row, "難易度", difficultyField);
        row = addField(panel, row, "Drive消費", driveCostField);
        row = addField(panel, row, "SA消費", superArtCostField);
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
        constraints.gridwidth = 4;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(6, 0, 0, 0);
        panel.add(buttons, constraints);

        return panel;
    }

    private JPanel createInputButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setPreferredSize(new Dimension(360, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(0, 10, 0, 0)
        ));

        JScrollPane stepsScroll = new JScrollPane(inputStepsPanel);
        stepsScroll.setPreferredSize(new Dimension(340, 126));
        panel.add(stepsScroll, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridBagLayout());
        JPanel numpad = new JPanel(new GridLayout(4, 3, 6, 6));
        for (String value : NUMPAD_BUTTONS) {
            JButton button = inputButton(value);
            button.addActionListener(event -> appendToActiveStep(value));
            numpad.add(button);
        }

        JPanel attacks = new JPanel(new GridLayout(3, 2, 6, 6));
        for (String value : ATTACK_BUTTONS) {
            JButton button = inputButton(value);
            button.addActionListener(event -> appendToActiveStep(value));
            attacks.add(button);
        }

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 8);
        constraints.gridx = 0;
        constraints.weightx = 0.55;
        buttons.add(numpad, constraints);
        constraints.gridx = 1;
        constraints.weightx = 0.45;
        constraints.insets = new Insets(0, 0, 0, 0);
        buttons.add(attacks, constraints);
        panel.add(buttons, BorderLayout.CENTER);

        JPanel commands = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton addStepButton = new JButton("段追加");
        JButton clearStepButton = new JButton("段消去");
        addStepButton.addActionListener(event -> addInputStep(""));
        clearStepButton.addActionListener(event -> clearActiveStep());
        commands.add(addStepButton);
        commands.add(clearStepButton);
        panel.add(commands, BorderLayout.SOUTH);

        addInputStep("");
        return panel;
    }

    private JButton inputButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(54, 44));
        return button;
    }

    private int addField(JPanel panel, int row, String label, Component component) {
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
        scrollPane.setPreferredSize(new Dimension(360, 74));
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

    private void showCharacterSelect() {
        cardLayout.show(cards, CARD_CHARACTER_SELECT);
    }

    private void openCharacter(String character) {
        selectedCharacter = character;
        selectedCharacterLabel.setText(character);
        characterField.setSelectedItem(character);
        clearForm();
        characterField.setSelectedItem(character);
        applyFilters();
        cardLayout.show(cards, CARD_COMBOS);
    }

    private void reloadCombos() {
        try {
            allCombos = repository.findAll().stream()
                    .sorted(Comparator.comparing(ComboEntry::updatedAt).reversed())
                    .toList();
            applyFilters();
        } catch (IOException | IllegalArgumentException ex) {
            showError("データの読み込みに失敗しました。", ex);
        }
    }

    private void applyFilters() {
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);

        List<ComboEntry> filtered = allCombos.stream()
                .filter(combo -> combo.characterName().equals(selectedCharacter))
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
        starterField.setSelectedItem(combo.starter().isBlank() ? STARTER_OPTIONS[0] : combo.starter());
        setInputSteps(combo.inputs());
        situationField.setText(combo.situation());
        positionField.setSelectedItem(combo.position().isBlank() ? POSITION_OPTIONS[0] : combo.position());
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
        characterField.setSelectedItem(selectedCharacter);
        titleField.setText("");
        starterField.setSelectedIndex(0);
        setInputSteps("");
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
                String.valueOf(starterField.getSelectedItem()),
                collectInputSteps(),
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

    private void appendToActiveStep(String value) {
        if (activeInputStep == null) {
            addInputStep("");
        }
        activeInputStep.setText(activeInputStep.getText() + value);
        activeInputStep.requestFocusInWindow();
    }

    private void clearActiveStep() {
        if (activeInputStep != null) {
            activeInputStep.setText("");
            activeInputStep.requestFocusInWindow();
        }
    }

    private void setInputSteps(String inputs) {
        inputStepFields.clear();
        inputStepsPanel.removeAll();

        String normalized = inputs == null ? "" : inputs.trim();
        if (normalized.isEmpty()) {
            addInputStep("");
        } else {
            for (String step : normalized.split("\\s*(?:⇒|=>|>|,)\\s*")) {
                if (!step.isBlank()) {
                    addInputStep(step);
                }
            }
        }
        inputStepsPanel.revalidate();
        inputStepsPanel.repaint();
    }

    private void addInputStep(String text) {
        int stepNumber = inputStepFields.size() + 1;
        if (!inputStepFields.isEmpty()) {
            JLabel arrow = new JLabel("⇒");
            arrow.setFont(arrow.getFont().deriveFont(Font.BOLD, 18f));
            inputStepsPanel.add(arrow);
        }

        JPanel stepPanel = new JPanel();
        stepPanel.setLayout(new BoxLayout(stepPanel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(stepNumber + "段目");
        JTextField field = new JTextField(text, 8);
        field.setMaximumSize(new Dimension(100, 28));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent event) {
                activeInputStep = field;
            }
        });
        stepPanel.add(label);
        stepPanel.add(field);
        inputStepFields.add(field);
        activeInputStep = field;
        inputStepsPanel.add(stepPanel);
        inputStepsPanel.revalidate();
        inputStepsPanel.repaint();
    }

    private String collectInputSteps() {
        return inputStepFields.stream()
                .map(field -> field.getText().trim())
                .filter(value -> !value.isEmpty())
                .reduce((left, right) -> left + INPUT_SEPARATOR + right)
                .orElse("");
    }

    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(this, message + System.lineSeparator() + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
    }

    private static final class CharacterIcon implements Icon {
        private static final Color[] PALETTE = {
                new Color(198, 50, 64), new Color(42, 99, 170), new Color(44, 132, 89), new Color(224, 150, 48),
                new Color(137, 88, 166), new Color(46, 141, 156), new Color(179, 75, 126), new Color(99, 108, 121)
        };

        private final String name;
        private final Color color;

        private CharacterIcon(String name) {
            this.name = name;
            this.color = PALETTE[Math.floorMod(name.hashCode(), PALETTE.length)];
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.fillRoundRect(x + 8, y + 2, getIconWidth() - 16, getIconHeight() - 10, 10, 10);
            g.setColor(new Color(255, 255, 255, 210));
            g.fillOval(x + 34, y + 14, 36, 36);
            g.setColor(color.darker());
            g.fillRoundRect(x + 24, y + 52, 56, 28, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(component.getFont().deriveFont(Font.BOLD, 18f));
            String initial = name.substring(0, 1).toUpperCase(Locale.ROOT);
            int width = g.getFontMetrics().stringWidth(initial);
            g.drawString(initial, x + (getIconWidth() - width) / 2, y + 38);
            g.dispose();
        }

        @Override
        public int getIconWidth() {
            return 104;
        }

        @Override
        public int getIconHeight() {
            return 86;
        }
    }
}
