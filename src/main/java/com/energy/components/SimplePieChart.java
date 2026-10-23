package com.energy.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JPanel;

public class SimplePieChart extends JPanel {
    private List<String> labels;
    private List<Double> values;
    private final Color[] colors = {
            new Color(0x4C, 0xC9, 0xF0), new Color(0x4C, 0xE0, 0x7A),
            new Color(0xF2, 0xA6, 0x5A), new Color(0xE5, 0x5C, 0x5C),
            new Color(0xAB, 0x47, 0xBC), new Color(0xFF, 0xEB, 0x3B)
    };

    public SimplePieChart() { setOpaque(false); }

    public void setData(List<String> labels, List<Double> values) {
        this.labels = labels;
        this.values = values;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (values == null || values.isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double total = values.stream().mapToDouble(Double::doubleValue).sum();
        int size = Math.min(getWidth(), getHeight()) - 40;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        double angle = 0;
        for (int i = 0; i < values.size(); i++) {
            double slice = (values.get(i) / total) * 360;
            g2.setColor(colors[i % colors.length]);
            g2.fillArc(x, y, size, size, (int) angle, (int) slice + 1);
            angle += slice;
        }
        g2.dispose();
    }
}