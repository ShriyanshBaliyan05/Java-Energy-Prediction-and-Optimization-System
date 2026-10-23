package com.energy.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.energy.utils.Constants;

public class StatCard extends RoundedPanel {

    private final JLabel valueLabel;
    private final JLabel descriptionLabel;

    public StatCard(String icon, String title, String value, Color accentColor) {
        this(icon, title, value, "", "", accentColor);
    }

    public StatCard(String icon, String title, String value, String unit, String description, Color accentColor) {
        super(18);
        setBackgroundColor(Constants.COLOR_CARD);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        setPreferredSize(new Dimension(220, 140));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(Constants.FONT_ICON.deriveFont(26f));
        iconLabel.setForeground(accentColor != null ? accentColor : Constants.COLOR_TEXT_PRIMARY);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Constants.FONT_SMALL);
        titleLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(iconLabel, BorderLayout.WEST);
        topRow.add(titleLabel, BorderLayout.SOUTH);

        valueLabel = new JLabel(value + " " + unit);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accentColor != null ? accentColor : Constants.COLOR_TEXT_PRIMARY);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));

        descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(Constants.FONT_SMALL);
        descriptionLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(valueLabel);
        centerPanel.add(descriptionLabel);

        add(topRow, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void updateValue(String value, String unit) {
        valueLabel.setText(value + " " + unit);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void updateDescription(String description) {
        descriptionLabel.setText(description);
    }
}
