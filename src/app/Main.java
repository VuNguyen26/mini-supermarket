package app;

import com.formdev.flatlaf.FlatLightLaf;
import bus.AuthService.AuthUser;
import presentation.LoginFrame;
import presentation.MainFrame;

import javax.swing.*;
import java.awt.*;

public class Main {
    // DEBUG MODE for bypass login, quick view
    // Để FALSE mặc định để chạy đúng luồng đăng nhập
    private static final boolean DEBUG_MODE = false;

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
                UIManager.put("TextField.margin", new Insets(8, 10, 8, 10));
                UIManager.put("PasswordField.margin", new Insets(8, 10, 8, 10));

                // Optional: scrollbar gọn hơn
                UIManager.put("ScrollBar.width", 10);

            } catch (Exception ex) {
                // fallback to system L&F
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }
            }

            if (DEBUG_MODE) {
                AuthUser administrator = new AuthUser();
                administrator.fullName = "";
                administrator.roleName = "";

                MainFrame main = new MainFrame(administrator);
                main.setVisible(true);
            } else {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
