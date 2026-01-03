package app;

import com.formdev.flatlaf.FlatLightLaf;
import presentation.LoginFrame;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FlatLightLaf.setup();
                UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
                UIManager.put("Component.arc", 12);
                UIManager.put("Button.arc", 12);
                UIManager.put("TextComponent.arc", 12);
                UIManager.put("Component.focusWidth", 0);
                UIManager.put("Component.innerFocusWidth", 0);
                UIManager.put("Button.boldText", false);
                UIManager.put("TextComponent.arc", 12);
                UIManager.put("TextField.margin", new Insets(8, 10, 8, 10));
                UIManager.put("PasswordField.margin", new Insets(8, 10, 8, 10));

                // Optional: scrollbar gọn hơn
                UIManager.put("ScrollBar.width", 10);

            } catch (Exception ex) {
                // fallback to system L&F
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
            }

            new LoginFrame().setVisible(true);
        });
    }
}
