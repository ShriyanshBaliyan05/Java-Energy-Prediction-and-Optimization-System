package com.energy.ui;

import com.energy.components.RoundedPanel;
import com.energy.database.ApplianceDAO;
import com.energy.database.EnergyRecordDAO;
import com.energy.model.Appliance;
import com.energy.service.BillCalculationService;
import com.energy.utils.Constants;
import com.energy.utils.DateUtils;
import com.energy.utils.ValidationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BillCalculatorPanel extends JPanel {

    private final BillCalculationService billService = new BillCalculationService();
    private final EnergyRecordDAO energyRecordDAO = new EnergyRecordDAO();
    private final ApplianceDAO applianceDAO = new ApplianceDAO();

    private JTabbedPane sourceTabs;

    // Tab 1: Manual
    private JTextField manualKwhField;

    // Tab 2: Date range (actual records)
    private JSpinner fromDateSpinner;
    private JSpinner toDateSpinner;
    private JLabel fetchedTotalLabel;
    private double fetchedTotalKwh = 0.0;

    // Tab 3: Appliances (projected)
    private DefaultTableModel applianceTableModel;
    private JTable applianceTable;
    private List<Appliance> allAppliances;

    // Charges
    private JTextField tariffField;
    private JTextField fixedChargesField;
    private JTextField additionalChargesField;
    private JTextField taxPercentField;

    // Result area
    private JPanel resultContainer;
    private JLabel estimatedBillLabel;

    private Runnable onSettingsChanged;

    public BillCalculatorPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Constants.COLOR_BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Electricity Bill Calculator");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.COLOR_TEXT_PRIMARY);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        center.add(buildSourceTabs());
        center.add(Box.createVerticalStrut(20));
        center.add(buildChargesPanel());
        center.add(Box.createVerticalStrut(20));
        center.add(buildResultSection());

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        loadApplianceTable();
    }

    public void setOnSettingsChanged(Runnable r) {
        this.onSettingsChanged = r;
    }

    // ---------------- Source Tabs ----------------

    private JComponent buildSourceTabs() {
        sourceTabs = new JTabbedPane();
        sourceTabs.setFont(Constants.FONT_LABEL_BOLD);

        sourceTabs.addTab("Manual Entry", buildManualTab());
        sourceTabs.addTab("Actual Records (Date Range)", buildDateRangeTab());
        sourceTabs.addTab("Projected (From Appliances)", buildApplianceTab());

        sourceTabs.setPreferredSize(new Dimension(100, 260));
        return sourceTabs;
    }

    private JPanel buildManualTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Constants.COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel("Enter Total Units Consumed (kWh):");
        label.setFont(Constants.FONT_LABEL_BOLD);
        label.setForeground(Constants.COLOR_TEXT_PRIMARY);
        panel.add(label, gbc);

        manualKwhField = new JTextField(15);
        gbc.gridy = 1;
        panel.add(manualKwhField, gbc);

        JLabel hint = new JLabel("Use this if you already know your total consumption (e.g. from an electricity meter/bill).");
        hint.setFont(Constants.FONT_SMALL);
        hint.setForeground(Constants.COLOR_TEXT_SECONDARY);
        gbc.gridy = 2;
        panel.add(hint, gbc);

        return panel;
    }

    private JPanel buildDateRangeTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Constants.COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        fromDateSpinner = DateUtils.createDateSpinner();
        toDateSpinner = DateUtils.createDateSpinner();

        gbc.gridy = 0;
        panel.add(labeled("From Date:", fromDateSpinner), gbc);
        gbc.gridy = 1;
        panel.add(labeled("To Date:", toDateSpinner), gbc);

        JButton fetchBtn = new JButton("Fetch Consumption from Records");
        fetchBtn.addActionListener(e -> fetchDateRangeTotal());
        gbc.gridy = 2;
        panel.add(fetchBtn, gbc);

        fetchedTotalLabel = new JLabel("Fetched Total: " + Constants.CURRENCY_SYMBOL + " N/A");
        fetchedTotalLabel.setFont(Constants.FONT_LABEL_BOLD);
        fetchedTotalLabel.setForeground(Constants.COLOR_ACCENT);
        fetchedTotalLabel.setText("Fetched Total: 0.00 kWh (click Fetch)");
        gbc.gridy = 3;
        panel.add(fetchedTotalLabel, gbc);

        JLabel hint = new JLabel("Uses the real consumption_kwh values you logged in Energy Input, summed over the selected dates.");
        hint.setFont(Constants.FONT_SMALL);
        hint.setForeground(Constants.COLOR_TEXT_SECONDARY);
        gbc.gridy = 4;
        panel.add(hint, gbc);

        return panel;
    }

    private JPanel buildApplianceTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Constants.COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Include", "Appliance", "Category", "Monthly kWh"};
        applianceTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 0;
            }
        };
        applianceTable = new JTable(applianceTableModel);
        applianceTable.setRowHeight(26);
        applianceTable.setFont(Constants.FONT_LABEL);
        applianceTable.getTableHeader().setFont(Constants.FONT_LABEL_BOLD);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setOpaque(false);
        JButton selectAllBtn = new JButton("Select All");
        JButton selectNoneBtn = new JButton("Select None");
        JButton refreshBtn = new JButton("Refresh List");
        selectAllBtn.addActionListener(e -> setAllChecked(true));
        selectNoneBtn.addActionListener(e -> setAllChecked(false));
        refreshBtn.addActionListener(e -> loadApplianceTable());
        topBar.add(selectAllBtn);
        topBar.add(selectNoneBtn);
        topBar.add(refreshBtn);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(applianceTable), BorderLayout.CENTER);

        JLabel hint = new JLabel("Projects a bill using each appliance's Monthly kWh = (Power x Qty x Daily Hours / 1000) x Days/Month.");
        hint.setFont(Constants.FONT_SMALL);
        hint.setForeground(Constants.COLOR_TEXT_SECONDARY);
        panel.add(hint, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel labeled(String label, JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Constants.FONT_LABEL_BOLD);
        l.setForeground(Constants.COLOR_TEXT_PRIMARY);
        l.setPreferredSize(new Dimension(80, 25));
        p.add(l);
        p.add(comp);
        return p;
    }

    private void setAllChecked(boolean checked) {
        for (int i = 0; i < applianceTableModel.getRowCount(); i++) {
            applianceTableModel.setValueAt(checked, i, 0);
        }
    }

    private void loadApplianceTable() {
        SwingWorker<List<Appliance>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Appliance> doInBackground() {
                return applianceDAO.getAll();
            }

            @Override
            protected void done() {
                try {
                    allAppliances = get();
                    applianceTableModel.setRowCount(0);
                    for (Appliance a : allAppliances) {
                        applianceTableModel.addRow(new Object[]{
                                Boolean.TRUE, a.getName(), a.getCategory(),
                                String.format("%.2f", a.getMonthlyConsumptionKwh())
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void fetchDateRangeTotal() {
        String from = DateUtils.getSpinnerDateAsString(fromDateSpinner);
        String to = DateUtils.getSpinnerDateAsString(toDateSpinner);

        if (from.compareTo(to) > 0) {
            JOptionPane.showMessageDialog(this, "'From Date' must not be after 'To Date'.", "Invalid Range", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<Double, Void> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() {
                return energyRecordDAO.getTotalConsumption(from, to);
            }

            @Override
            protected void done() {
                try {
                    fetchedTotalKwh = get();
                    fetchedTotalLabel.setText("Fetched Total: " + String.format("%.2f", fetchedTotalKwh) + " kWh (" + from + " to " + to + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    // ---------------- Charges Panel ----------------

    private RoundedPanel buildChargesPanel() {
        RoundedPanel panel = new RoundedPanel(16, Constants.COLOR_CARD_BG);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Billing Parameters");
        heading.setFont(Constants.FONT_HEADING);
        heading.setForeground(Constants.COLOR_TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        panel.add(heading, gbc);
        gbc.gridwidth = 1;

        tariffField = new JTextField(String.valueOf(billService.getSavedTariff()));
        fixedChargesField = new JTextField(String.valueOf(billService.getSavedFixedCharges()));
        additionalChargesField = new JTextField("0.0");
        taxPercentField = new JTextField(String.valueOf(billService.getSavedTaxPercent()));

        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(formField("Tariff (" + Constants.CURRENCY_SYMBOL + "/kWh) *", tariffField), gbc);
        gbc.gridx = 1;
        panel.add(formField("Fixed Charges (" + Constants.CURRENCY_SYMBOL + ") *", fixedChargesField), gbc);
        gbc.gridx = 2;
        panel.add(formField("Additional Charges (" + Constants.CURRENCY_SYMBOL + ")", additionalChargesField), gbc);
        gbc.gridx = 3;
        panel.add(formField("Tax (%) *", taxPercentField), gbc);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonRow.setOpaque(false);

        JButton calcBtn = new JButton("Calculate Bill");
        calcBtn.setFont(Constants.FONT_LABEL_BOLD);
        calcBtn.addActionListener(e -> calculateBill());

        JButton saveDefaultsBtn = new JButton("Save as Default Tariff/Charges");
        saveDefaultsBtn.addActionListener(e -> saveDefaults());

        JButton resetBtn = new JButton("Reset to Saved Defaults");
        resetBtn.addActionListener(e -> resetToDefaults());

        buttonRow.add(calcBtn);
        buttonRow.add(saveDefaultsBtn);
        buttonRow.add(resetBtn);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        panel.add(buttonRow, gbc);

        return panel;
    }

    private JPanel formField(String labelText, JTextField field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(Constants.FONT_LABEL);
        label.setForeground(Constants.COLOR_TEXT_SECONDARY);
        p.add(label);
        p.add(field);
        return p;
    }

    private void saveDefaults() {
        String tariffStr = tariffField.getText().trim();
        String fixedStr = fixedChargesField.getText().trim();
        String taxStr = taxPercentField.getText().trim();

        StringBuilder errors = new StringBuilder();
        if (!ValidationUtils.isValidPositiveDouble(tariffStr)) errors.append("- Tariff must be a positive number.\n");
        if (!ValidationUtils.isValidNonNegativeDouble(fixedStr)) errors.append("- Fixed charges must be a non-negative number.\n");
        if (!ValidationUtils.isValidNonNegativeDouble(taxStr)) errors.append("- Tax % must be a non-negative number.\n");

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, "Please fix the following:\n" + errors, "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        billService.saveDefaults(
                Double.parseDouble(tariffStr),
                Double.parseDouble(fixedStr),
                Double.parseDouble(taxStr)
        );
        JOptionPane.showMessageDialog(this, "Defaults saved. Other screens using the default tariff will now reflect this.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        if (onSettingsChanged != null) onSettingsChanged.run();
    }

    private void resetToDefaults() {
        tariffField.setText(String.valueOf(billService.getSavedTariff()));
        fixedChargesField.setText(String.valueOf(billService.getSavedFixedCharges()));
        taxPercentField.setText(String.valueOf(billService.getSavedTaxPercent()));
        additionalChargesField.setText("0.0");
    }

    // ---------------- Calculation ----------------

    private double resolveTotalKwh() {
        int tabIndex = sourceTabs.getSelectedIndex();
        if (tabIndex == 0) {
            String kwhStr = manualKwhField.getText().trim();
            if (!ValidationUtils.isValidPositiveDouble(kwhStr)) return -1;
            return Double.parseDouble(kwhStr);
        } else if (tabIndex == 1) {
            return fetchedTotalKwh;
        } else {
            double sum = 0.0;
            for (int i = 0; i < applianceTableModel.getRowCount(); i++) {
                boolean included = (Boolean) applianceTableModel.getValueAt(i, 0);
                if (included) {
                    String kwhStr = String.valueOf(applianceTableModel.getValueAt(i, 3));
                    sum += ValidationUtils.parseDoubleOrZero(kwhStr);
                }
            }
            return sum;
        }
    }

    private void calculateBill() {
        double totalKwh = resolveTotalKwh();
        int tabIndex = sourceTabs.getSelectedIndex();

        if (tabIndex == 0 && totalKwh < 0) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive kWh value.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (tabIndex == 1 && totalKwh <= 0) {
            JOptionPane.showMessageDialog(this, "Please fetch a valid consumption total first (must be greater than zero).", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tabIndex == 2 && totalKwh <= 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one appliance with consumption.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tariffStr = tariffField.getText().trim();
        String fixedStr = fixedChargesField.getText().trim();
        String additionalStr = additionalChargesField.getText().trim();
        String taxStr = taxPercentField.getText().trim();

        StringBuilder errors = new StringBuilder();
        if (!ValidationUtils.isValidPositiveDouble(tariffStr)) errors.append("- Tariff must be a positive number.\n");
        if (!ValidationUtils.isValidNonNegativeDouble(fixedStr)) errors.append("- Fixed charges must be a non-negative number.\n");
        if (!additionalStr.isEmpty() && !ValidationUtils.isValidNonNegativeDouble(additionalStr))
            errors.append("- Additional charges must be a non-negative number.\n");
        if (!ValidationUtils.isValidNonNegativeDouble(taxStr)) errors.append("- Tax % must be a non-negative number.\n");

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, "Please fix the following:\n" + errors, "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double tariff = Double.parseDouble(tariffStr);
        double fixed = Double.parseDouble(fixedStr);
        double additional = additionalStr.isEmpty() ? 0.0 : Double.parseDouble(additionalStr);
        double tax = Double.parseDouble(taxStr);

        BillCalculationService.BillBreakdown b = billService.calculateBill(totalKwh, tariff, fixed, additional, tax);
        renderBreakdown(b);
    }

    // ---------------- Result Section ----------------

    private JComponent buildResultSection() {
        resultContainer = new RoundedPanel(16, Constants.COLOR_CARD_BG);
        resultContainer.setLayout(new BoxLayout(resultContainer, BoxLayout.Y_AXIS));
        resultContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Estimated Bill Breakdown");
        heading.setFont(Constants.FONT_HEADING);
        heading.setForeground(Constants.COLOR_TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultContainer.add(heading);
        resultContainer.add(Box.createVerticalStrut(10));

        JLabel emptyState = new JLabel("No calculation yet. Choose a consumption source above, fill in billing parameters, and click \"Calculate Bill\".");
        emptyState.setFont(Constants.FONT_LABEL);
        emptyState.setForeground(Constants.COLOR_TEXT_SECONDARY);
        emptyState.setAlignmentX(Component.LEFT_ALIGNMENT);
        emptyState.setName("emptyState");
        resultContainer.add(emptyState);

        return resultContainer;
    }

    private void renderBreakdown(BillCalculationService.BillBreakdown b) {
        resultContainer.removeAll();

        JLabel heading = new JLabel("Estimated Bill Breakdown");
        heading.setFont(Constants.FONT_HEADING);
        heading.setForeground(Constants.COLOR_TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultContainer.add(heading);
        resultContainer.add(Box.createVerticalStrut(12));

        resultContainer.add(breakdownRow("Total Units Consumed", String.format("%.2f kWh", b.totalKwh), false));
        resultContainer.add(breakdownRow("Tariff Rate", Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", b.tariff) + " / kWh", false));
        resultContainer.add(breakdownRow("Energy Cost  (kWh x Tariff)", Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", b.energyCost), false));
        resultContainer.add(breakdownRow("Fixed Charges", Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", b.fixedCharges), false));
        resultContainer.add(breakdownRow("Additional Charges", Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", b.additionalCharges), false));
        resultContainer.add(Box.createVerticalStrut(6));
        resultContainer.add(new JSeparator());
        resultContainer.add(Box.createVerticalStrut(6));
        resultContainer.add(breakdownRow("Subtotal", Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", b.subtotal), false));
        resultContainer.add(breakdownRow("Tax  (" + String.format("%.1f", b.taxPercent) + "% of Subtotal)", Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", b.taxAmount), false));
        resultContainer.add(Box.createVerticalStrut(10));
        resultContainer.add(new JSeparator());
        resultContainer.add(Box.createVerticalStrut(10));

        estimatedBillLabel = new JLabel("Estimated Bill:  " + Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", b.estimatedBill));
        estimatedBillLabel.setFont(Constants.FONT_VALUE);
        estimatedBillLabel.setForeground(Constants.COLOR_ACCENT_GREEN);
        estimatedBillLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultContainer.add(estimatedBillLabel);

        resultContainer.revalidate();
        resultContainer.repaint();
    }

    private JPanel breakdownRow(String label, String value, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel l = new JLabel(label);
        l.setFont(bold ? Constants.FONT_LABEL_BOLD : Constants.FONT_LABEL);
        l.setForeground(Constants.COLOR_TEXT_SECONDARY);

        JLabel v = new JLabel(value);
        v.setFont(bold ? Constants.FONT_LABEL_BOLD : Constants.FONT_LABEL);
        v.setForeground(Constants.COLOR_TEXT_PRIMARY);
        v.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }
}