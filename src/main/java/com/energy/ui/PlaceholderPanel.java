package com.energy.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.energy.utils.Constants;

public class PlaceholderPanel extends JPanel {
    public PlaceholderPanel(String title, String icon, String phaseInfo) {
        this(title, icon, "This module will be implemented in " + phaseInfo + " of the development roadmap.", true);
    }

    public PlaceholderPanel(String title, String description) {
        this(title, "\u25C6", description, true);
    }

    private PlaceholderPanel(String title, String icon, String description, boolean directDescription) {
        setLayout(new GridBagLayout());
        setBackground(Constants.COLOR_BACKGROUND);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        iconLabel.setForeground(Constants.COLOR_SECONDARY_ACCENT);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Constants.FONT_HEADING);
        titleLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel info = new JLabel(description);
        info.setFont(Constants.FONT_BODY);
        info.setForeground(Constants.COLOR_TEXT_SECONDARY);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(iconLabel);
        box.add(Box.createVerticalStrut(14));
        box.add(titleLabel);
        box.add(Box.createVerticalStrut(8));
        box.add(info);

        add(box);
    }
}
