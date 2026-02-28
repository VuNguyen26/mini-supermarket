package presentation.component;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BarChartPanel extends JPanel {

    public static class Bar {
        public final String label;
        public final BigDecimal value;

        public Bar(String label, BigDecimal value) {
            this.label = label;
            this.value = value == null ? BigDecimal.ZERO : value;
        }
    }

    private List<Bar> data = new ArrayList<>();
    private String title = "Doanh thu";

    public BarChartPanel() {
        setOpaque(false);
    }

    public void setTitle(String title) {
        this.title = title;
        repaint();
    }

    public void setData(List<Bar> bars) {
        this.data = (bars == null) ? new ArrayList<>() : bars;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // padding
        int padL = 40, padR = 14, padT = 30, padB = 36;

        // title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(new Color(15, 23, 42));
        g2.drawString(title, padL, 20);

        int chartX = padL;
        int chartY = padT;
        int chartW = w - padL - padR;
        int chartH = h - padT - padB;

        // axis
        g2.setColor(new Color(229, 231, 235));
        g2.drawLine(chartX, chartY + chartH, chartX + chartW, chartY + chartH);

        if (data.isEmpty()) {
            g2.setColor(new Color(100, 116, 139));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.drawString("(Không có dữ liệu)", chartX + 10, chartY + 30);
            g2.dispose();
            return;
        }

        BigDecimal max = BigDecimal.ZERO;
        for (Bar b : data) if (b.value.compareTo(max) > 0) max = b.value;
        if (max.compareTo(BigDecimal.ZERO) == 0) max = BigDecimal.ONE;

        int n = data.size();
        int gap = 10;
        int barW = Math.max(12, Math.min(60, (chartW - gap * (n - 1)) / n));
        int totalBarsW = n * barW + (n - 1) * gap;
        int startX = chartX + Math.max(0, (chartW - totalBarsW) / 2);

        // grid lines (3 lines)
        g2.setColor(new Color(241, 245, 249));
        for (int i = 1; i <= 3; i++) {
            int y = chartY + chartH - (chartH * i / 3);
            g2.drawLine(chartX, y, chartX + chartW, y);
        }

        // bars
        for (int i = 0; i < n; i++) {
            Bar b = data.get(i);
            double ratio = b.value.doubleValue() / max.doubleValue();
            int bh = (int) Math.round(chartH * ratio);

            int x = startX + i * (barW + gap);
            int y = chartY + chartH - bh;

            g2.setColor(new Color(37, 99, 235)); // xanh
            g2.fillRoundRect(x, y, barW, bh, 10, 10);

            // label
            g2.setColor(new Color(100, 116, 139));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            String lb = b.label;
            int lw = g2.getFontMetrics().stringWidth(lb);
            int lx = x + (barW - lw) / 2;
            g2.drawString(lb, Math.max(chartX, lx), chartY + chartH + 18);
        }

        g2.dispose();
    }
}