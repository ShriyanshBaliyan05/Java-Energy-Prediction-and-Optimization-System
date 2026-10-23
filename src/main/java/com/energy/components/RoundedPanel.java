package com.energy.components;
import javax.swing.*;
import java.awt.*;
public class RoundedPanel extends JPanel {
    private final int cornerRadius;
    private Color backgroundColorOverride;
    public RoundedPanel(int radius) {
        this.cornerRadius = radius;
        setOpaque(false);
    }
    public RoundedPanel(int radius, Color backgroundColor) {
        this(radius);
        setBackgroundColor(backgroundColor);
    }
    public void setBackgroundColor(Color color) {
        this.backgroundColorOverride = color;
        repaint();
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg = backgroundColorOverride != null ? backgroundColorOverride : getBackground();
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        g2.dispose();
        super.paintComponent(g);
    }
}
