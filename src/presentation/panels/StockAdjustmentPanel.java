package presentation.panels;

import javax.swing.*;
import java.awt.*;

public class StockAdjustmentPanel extends JPanel {

    public StockAdjustmentPanel() {
        initUI();
    }

    private void initUI() {
        // layout đơn giản
        setLayout(new BorderLayout());

        JLabel label = new JLabel("Hello Stock Adjustment", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));

        add(label, BorderLayout.CENTER);
    }
}
