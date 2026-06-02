package com.combocounter;

import com.combocounter.repository.ComboRepository;
import com.combocounter.repository.FileComboRepository;
import com.combocounter.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Path;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();

            Path dataPath = Path.of(System.getProperty("user.home"), ".combocounter", "combos.tsv");
            ComboRepository repository = new FileComboRepository(dataPath);

            MainFrame frame = new MainFrame(repository);
            frame.setVisible(true);
        });
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) {
            // Swing's default look and feel is fine as a fallback.
        }
    }
}
