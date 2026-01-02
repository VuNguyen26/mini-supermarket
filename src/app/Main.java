package app;

import presentation.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Run Swing on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            new LoginFrame().setVisible(true);
        });
    }
}
