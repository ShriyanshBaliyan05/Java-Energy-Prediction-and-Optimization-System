package com.energy.ui;

import com.energy.service.BillCalculationService;
import com.energy.utils.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {

    private static final Color CARD_BG = new Color(30, 34, 45);
    private static final Color BORDER_COLOR = new Color(55, 60, 72);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    private final BillCalculationService billCalculationService = new BillCalculationService();

    private JTextField tariffField;
    private JTextField fixedChargesField;
    private JTextField taxPercentField;
    private JButton saveButton;
    private JButton resetButton;
    private JLabel statusLabel;

    private Runnable onSettingsChanged;

    public SettingsPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Constants.COLOR_BG_DARK);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("Settings");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(buildBillingCard());
        content.add(Box.createVerticalStrut(20));
        content.add(buildInfoCard());
        content.add(Box.createVerticalGlue());

        add(content, BorderLayout.CENTER);

        loadCurrentSettings();
    }

    public void setOnSettingsChanged(Runnable callback) {
        this.onSettingsChanged = callback;
    }

    public void refreshData() {
        loadCurrentSettings();
    }

    private JPanel buildBillingCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(560, 260));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel heading = new JLabel("Billing Defaults");
        heading.setFont(Constants.FONT_LABEL_BOLD);
        heading.setForeground(Color.WHITE);
        card.add(heading, gbc);

        gbc.gridwidth = 1;

        gbc.gridy = 1; gbc.gridx = 0;
        card.add(makeLabel("Tariff (" + Constants.CURRENCY_SYMBOL + " per kWh):"), gbc);
        gbc.gridx = 1;
        tariffField = makeField();
        card.add(tariffField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        card.add(makeLabel("Fixed Monthly Charges:"), gbc);
        gbc.gridx = 1;
        fixedChargesField = makeField();
        card.add(fixedChargesField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        card.add(makeLabel("Tax Percent (%):"), gbc);
        gbc.gridx = 1;
        taxPercentField = makeField();
        card.add(taxPercentField, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonRow.setOpaque(false);

        saveButton = new JButton("Save Settings");
        styleButton(saveButton, Constants.COLOR_ACCENT_GREEN);
        saveButton.addActionListener(e -> saveSettings());

        resetButton = new JButton("Load Recommended Defaults");
        styleButton(resetButton, Constants.COLOR_ACCENT);
        resetButton.addActionListener(e -> resetToDefaults());

        buttonRow.add(saveButton);
        buttonRow.add(resetButton);
        card.add(buttonRow, gbc);

        gbc.gridy = 5;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(SMALL_FONT);
        card.add(statusLabel, gbc);

        gbc.gridy = 6;
        JLabel note = new JLabel("<html>These values are used as the default Tariff, Fixed Charges and Tax"
                + " in the Bill Calculator, Energy Input, and Dashboard screens.</html>");
        note.setFont(SMALL_FONT);
        note.setForeground(new Color(160, 165, 175));
        card.add(note, gbc);

        return card;
    }

    private JPanel buildInfoCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(560, 150));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel heading = new JLabel("Application Information");
        heading.setFont(Constants.FONT_LABEL_BOLD);
        heading.setForeground(Color.WHITE);
        card.add(heading, gbc);

        gbc.gridy = 1;
        card.add(makeLabel("Energy Unit: kWh (fixed, standard unit)"), gbc);

        gbc.gridy = 2;
        card.add(makeLabel("Currency Symbol: " + Constants.CURRENCY_SYMBOL), gbc);

        gbc.gridy = 3;
        card.add(makeLabel("Database File: data/energy.db (SQLite)"), gbc);

        return card;
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY_FONT);
        label.setForeground(Color.WHITE);
        return label;
    }

    private JTextField makeField() {
        JTextField field = new JTextField(10);
        field.setFont(BODY_FONT);
        field.setBackground(new Color(22, 26, 35));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 8, 6, 8)));
        return field;
    }

    private void styleButton(JButton button, Color bg) {
        button.setFont(Constants.FONT_LABEL_BOLD);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadCurrentSettings() {
        setStatus("Loading current settings...", false);
        SwingWorker<double[], Void> worker = new SwingWorker<>() {
            @Override
            protected double[] doInBackground() {
                return new double[] {
                        billCalculationService.getSavedTariff(),
                        billCalculationService.getSavedFixedCharges(),
                        billCalculationService.getSavedTaxPercent()
                };
            }

            @Override
            protected void done() {
                try {
                    double[] values = get();
                    tariffField.setText(String.format("%.2f", values[0]));
                    fixedChargesField.setText(String.format("%.2f", values[1]));
                    taxPercentField.setText(String.format("%.2f", values[2]));
                    setStatus(" ", false);
                } catch (Exception ex) {
                    setStatus("Failed to load settings: " + ex.getMessage(), true);
                }
            }
        };
        worker.execute();
    }

    private void resetToDefaults() {
        tariffField.setText("8.00");
        fixedChargesField.setText("50.00");
        taxPercentField.setText("5.00");
        setStatus("Recommended defaults loaded. Click \"Save Settings\" to apply.", false);
    }

    private void saveSettings() {
        double tariff, fixedCharges, taxPercent;
        try {
            tariff = Double.parseDouble(tariffField.getText().trim());
            fixedCharges = Double.parseDouble(fixedChargesField.getText().trim());
            taxPercent = Double.parseDouble(taxPercentField.getText().trim());
        } catch (NumberFormatException ex) {
            setStatus("Please enter valid numbers in all fields.", true);
            return;
        }

        if (tariff <= 0) {
            setStatus("Tariff must be greater than 0.", true);
            return;
        }
        if (fixedCharges < 0) {
            setStatus("Fixed charges cannot be negative.", true);
            return;
        }
        if (taxPercent < 0 || taxPercent > 100) {
            setStatus("Tax percent must be between 0 and 100.", true);
            return;
        }

        saveButton.setEnabled(false);
        setStatus("Saving...", false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                billCalculationService.saveDefaults(tariff, fixedCharges, taxPercent);
                return null;
            }

            @Override
            protected void done() {
                saveButton.setEnabled(true);
                try {
                    get();
                    setStatus("Settings saved successfully.", false);
                    if (onSettingsChanged != null) {
                        onSettingsChanged.run();
                    }
                } catch (Exception ex) {
                    setStatus("Failed to save settings: " + ex.getMessage(), true);
                }
            }
        };
        worker.execute();
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Constants.COLOR_ACCENT_RED : Constants.COLOR_ACCENT_GREEN);
    }
}