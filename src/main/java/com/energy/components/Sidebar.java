package com.energy.components;

import com.energy.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Sidebar extends JPanel {

    private Consumer<String> navigationListener;
    private final Map<String, JButton> buttons = new LinkedHashMap<>();

    private static final String[][] NAV_ITEMS = {
            {Constants.CARD_DASHBOARD, "\u2302", "Dashboard"},
            {Constants.CARD_ENERGY_INPUT, "\u25B2", "Energy Input"},
            {Constants.CARD_APPLIANCES, "\u25A0", "Appliances"},
            {Constants.CARD_PREDICTION, "\u25C6", "Prediction"},
            {Constants.CARD_ANALYTICS, "\u25CF", "Analytics"},
            {Constants.CARD_BILL_CALCULATOR, "\u2605", "Bill Calculator"},
            {Constants.CARD_OPTIMIZATION, "\u25AC", "Optimization"},
            {Constants.CARD_HISTORY, "\u25AA", "History"},
            {Constants.CARD_REPORTS, "\u25CB", "Reports"},
            {Constants.CARD_SETTINGS, "\u25A1", "Settings"},
            {Constants.CARD_ABOUT, "?", "About"}
    };

    public Sidebar() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Constants.COLOR_SIDEBAR);
        setPreferredSize(new Dimension(220, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel appTitle = new JLabel("  ENERGY SYS");
        appTitle.setFont(Constants.FONT_HEADING);
        appTitle.setForeground(Constants.COLOR_ACCENT);
        appTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(appTitle);
        add(Box.createVerticalStrut(30));

        for (String[] item : NAV_ITEMS) {
            JButton btn = createNavButton(item[1], item[2], item[0]);
            buttons.put(item[0], btn);
            add(btn);
            add(Box.createVerticalStrut(4));
        }

        add(Box.createVerticalGlue());
        setActive(Constants.CARD_DASHBOARD);
    }

    private JButton createNavButton(String icon, String text, String cardName) {
        JButton btn = new JButton("  " + icon + "    " + text);
        btn.setFont(Constants.FONT_LABEL);
        btn.setForeground(Constants.COLOR_TEXT_PRIMARY);
        btn.setBackground(Constants.COLOR_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 42));
        btn.setPreferredSize(new Dimension(220, 42));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            setActive(cardName);
            if (navigationListener != null) navigationListener.accept(cardName);
        });
        return btn;
    }

    public void setActive(String cardName) {
        for (Map.Entry<String, JButton> entry : buttons.entrySet()) {
            if (entry.getKey().equals(cardName)) {
                entry.getValue().setBackground(Constants.COLOR_ACCENT.darker());
                entry.getValue().setForeground(Color.WHITE);
            } else {
                entry.getValue().setBackground(Constants.COLOR_SIDEBAR);
                entry.getValue().setForeground(Constants.COLOR_TEXT_PRIMARY);
            }
        }
    }

    public void setNavigationListener(Consumer<String> listener) {
        this.navigationListener = listener;
    }
}
