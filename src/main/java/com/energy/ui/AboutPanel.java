package com.energy.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.energy.utils.Constants;

public class AboutPanel extends JPanel {
    public AboutPanel() {
        setLayout(new BorderLayout());
        setBackground(Constants.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(sectionTitle("\u25A0 " + Constants.APP_NAME));
        content.add(Box.createVerticalStrut(6));
        content.add(bodyText("Version: " + Constants.APP_VERSION));
        content.add(Box.createVerticalStrut(20));

        content.add(sectionTitle("Project Type"));
        content.add(bodyText("B.Tech Computer Science Engineering (AI) Project"));
        content.add(Box.createVerticalStrut(20));

        content.add(sectionTitle("Purpose"));
        content.add(bodyText("A desktop application to monitor, predict, analyze, and optimize electricity "
                + "consumption using real consumption data, appliance-level breakdowns, and transparent "
                + "statistical prediction models."));
        content.add(Box.createVerticalStrut(20));

        content.add(sectionTitle("Technology Stack"));
        content.add(bodyText("- Language: Java 17\n- UI: Java Swing (custom Graphics2D components)\n"
                + "- Database: SQLite via JDBC\n- Architecture: Layered (UI / Service / DAO / Model)"));
        content.add(Box.createVerticalStrut(20));

        content.add(sectionTitle("Core Features"));
        content.add(bodyText("- Energy consumption input and appliance management\n"
                + "- Consumption prediction (moving average / linear regression)\n"
                + "- Energy analytics and visualizations\n- Electricity bill calculation\n"
                + "- Optimization recommendations\n- Historical records, reports and CSV/PDF export"));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Constants.COLOR_BACKGROUND);

        add(scroll, BorderLayout.CENTER);
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Constants.FONT_SUBHEADING);
        label.setForeground(Constants.COLOR_SECONDARY_ACCENT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextArea bodyText(String text) {
        JTextArea area = new JTextArea(text);
        area.setFont(Constants.FONT_BODY);
        area.setForeground(Constants.COLOR_TEXT_SECONDARY);
        area.setBackground(Constants.COLOR_BACKGROUND);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        area.setMaximumSize(new Dimension(800, 200));
        return area;
    }
}