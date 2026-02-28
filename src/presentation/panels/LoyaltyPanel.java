package presentation.panels;

import javax.swing.*;
import java.awt.*;

public class LoyaltyPanel extends JPanel {
    public LoyaltyPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Loyalty / Tích điểm");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(new JLabel("Chưa triển khai logic DB. (Placeholder panel)"));

        add(title, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }
}