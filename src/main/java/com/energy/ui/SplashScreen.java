package com.energy.ui;

import com.energy.utils.Constants;

import javax.swing.*;
import java.awt.*;

public class SplashScreen {

    private JWindow window;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public void showSplash() {
        window = new JWindow();
        window.setSize(520, 320);
        window.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Constants.COLOR_BACKGROUND);
        panel.setBorder(BorderFactory.createLineBorder(Constants.COLOR_PRIMARY, 2));

        JLabel title = new JLabel("\u25A0 " + Constants.APP_NAME, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Constants.COLOR_TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(50, 20, 10, 20));

        JLabel tagline = new JLabel(Constants.APP_TAGLINE, SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        tagline.setForeground(Constants.COLOR_TEXT_SECONDARY);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.setOpaque(false);
        centerPanel.add(title);
        centerPanel.add(tagline);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setForeground(Constants.COLOR_SECONDARY_ACCENT);
        progressBar.setBackground(Constants.COLOR_CARD);
        progressBar.setBorderPainted(false);

        statusLabel = new JLabel("Initializing application...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JPanel progressWrap = new JPanel(new BorderLayout());
        progressWrap.setOpaque(false);
        progressWrap.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));
        progressWrap.add(progressBar, BorderLayout.CENTER);

        JPanel bottomWrap = new JPanel(new BorderLayout());
        bottomWrap.setOpaque(false);
        bottomWrap.add(progressWrap, BorderLayout.NORTH);
        bottomWrap.add(statusLabel, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(bottomWrap, BorderLayout.SOUTH);

        window.setContentPane(panel);
        window.setVisible(true);

        final int[] progress = {0};
        String[] messages = {
            "Initializing application...",
            "Connecting to database...",
            "Loading saved records...",
            "Preparing dashboard...",
            "Almost ready..."
        };

        Timer timer = new Timer(30, null);
        timer.addActionListener(e -> {
            progress[0] += 2;
            progressBar.setValue(progress[0]);
            int msgIndex = Math.min(messages.length - 1, progress[0] / 25);
            statusLabel.setText(messages[msgIndex]);

            if (progress[0] >= 100) {
                ((Timer) e.getSource()).stop();
                window.dispose();
                SwingUtilities.invokeLater(() -> {
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                });
            }
        });
        timer.start();
    }
}