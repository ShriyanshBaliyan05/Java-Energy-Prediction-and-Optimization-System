package com.energy.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JPanel;

import com.energy.utils.Constants;

public class SimpleBarChart extends JPanel {

    private List<String> labels;
    private List<Double> values;
    private int highlightIndex = -1;

    public SimpleBarChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(100, 240));
    }

    public void setData(List<String> labels, List<Double> values, int highlightIndex, String unused) {
        this.labels = labels;
        this.values = values;
        this.highlightIndex = highlightIndex;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (values == null || values.isEmpty()) {
            g2.setColor(Constants.COLOR_TEXT_SECONDARY);
            g2.setFont(Constants.FONT_LABEL);
            String msg = "No data to display.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
            g2.dispose();
            return;
        }

        int padding = 35;
        int bottomPadding = 45;
        int chartWidth = width - (2 * padding);
        int chartHeight = height - padding - bottomPadding;
        if (chartWidth < 10 || chartHeight < 10) {
            g2.dispose();
            return;
        }

        double maxVal = values.stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        if (maxVal <= 0) maxVal = 1.0;

        int n = values.size();
        int gap = 8;
        int barWidth = Math.max(8, (chartWidth - (gap * (n - 1))) / n);

        g2.setColor(Constants.COLOR_BORDER);
        g2.drawLine(padding, padding, padding, padding + chartHeight);
        g2.drawLine(padding, padding + chartHeight, padding + chartWidth, padding + chartHeight);

        for (int i = 0; i < n; i++) {
            double val = values.get(i);
            int barHeight = (int) ((val / maxVal) * chartHeight);
            int x = padding + i * (barWidth + gap);
            int y = padding + chartHeight - Math.max(barHeight, 2);

            Color barColor = (i == highlightIndex) ? Constants.COLOR_ACCENT_ORANGE : Constants.COLOR_ACCENT;
            g2.setColor(barColor);
            g2.fillRoundRect(x, y, barWidth, Math.max(barHeight, 2), 6, 6);

            g2.setColor(Constants.COLOR_TEXT_SECONDARY);
            g2.setFont(Constants.FONT_SMALL);
            String valLabel = String.format("%.1f", val);
            FontMetrics fmv = g2.getFontMetrics();
            g2.drawString(valLabel, x + (barWidth - fmv.stringWidth(valLabel)) / 2, Math.max(y - 4, 12));

            if (labels != null && i < labels.size()) {
                String lbl = labels.get(i);
                if (lbl.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    lbl = lbl.substring(5); // show MM-dd for ISO dates
                }
                FontMetrics fml = g2.getFontMetrics();
                g2.drawString(lbl, x + (barWidth - fml.stringWidth(lbl)) / 2, padding + chartHeight + 18);
            }
        }

        g2.dispose();
    }
}