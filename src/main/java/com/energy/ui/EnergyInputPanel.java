package com.energy.ui;

import com.energy.database.ApplianceDAO;
import com.energy.database.EnergyRecordDAO;
import com.energy.database.SettingsDAO;
import com.energy.model.Appliance;
import com.energy.model.EnergyRecord;
import com.energy.utils.Constants;
import com.energy.utils.DateUtils;
import com.energy.utils.ValidationUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class EnergyInputPanel extends JPanel {

    private final ApplianceDAO applianceDAO = new ApplianceDAO();
    private final EnergyRecordDAO energyRecordDAO = new EnergyRecordDAO();
    private final SettingsDAO settingsDAO = new SettingsDAO();

    private JSpinner dateSpinner;
    private JTextField buildingField;
    private JComboBox<String> applianceBox;
    private JComboBox<String> categoryBox;
    private JTextField consumptionField;
    private JTextField hoursField;
    private JTextField meterField;
    private JTextField tariffField;
    private JTextArea notesArea;

    private List<Appliance> appliances;
    private Runnable onDataChanged;

    public EnergyInputPanel() {
        setLayout(new BorderLayout());
        setBackground(Constants.COLOR_BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Energy Consumption Input");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.COLOR_TEXT_PRIMARY);
        add(title, BorderLayout.NORTH);

        add(buildForm(), BorderLayout.CENTER);

        reloadAppliances();
    }

    public void setOnDataChanged(Runnable r) {
        this.onDataChanged = r;
    }

    private JScrollPane buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridx = 0;

        dateSpinner = DateUtils.createDateSpinner();
        buildingField = new JTextField("Main Building");
        applianceBox = new JComboBox<>();
        categoryBox = new JComboBox<>(Constants.APPLIANCE_CATEGORIES);
        consumptionField = new JTextField();
        JButton autoCalcBtn = new JButton("Auto-Calculate from Appliance");
        hoursField = new JTextField();
        meterField = new JTextField();
        tariffField = new JTextField(String.valueOf(settingsDAO.getTariff()));
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        int y = 0;
        y = addFormRow(form, gbc, y, "Date *", dateSpinner);
        y = addFormRow(form, gbc, y, "Building / Location", buildingField);
        y = addFormRow(form, gbc, y, "Appliance (optional)", applianceBox);
        y = addFormRow(form, gbc, y, "Category *", categoryBox);

        JPanel consumptionPanel = new JPanel(new BorderLayout(10, 0));
        consumptionPanel.setOpaque(false);
        consumptionPanel.add(consumptionField, BorderLayout.CENTER);
        consumptionPanel.add(autoCalcBtn, BorderLayout.EAST);
        y = addFormRow(form, gbc, y, "Consumption (kWh) *", consumptionPanel);

        y = addFormRow(form, gbc, y, "Operating Hours *", hoursField);
        y = addFormRow(form, gbc, y, "Meter Reading (optional)", meterField);
        y = addFormRow(form, gbc, y, "Tariff (" + Constants.CURRENCY_SYMBOL + "/kWh) *", tariffField);
        y = addFormRow(form, gbc, y, "Notes", new JScrollPane(notesArea));

        applianceBox.addActionListener(e -> {
            Appliance selected = getSelectedAppliance();
            if (selected != null) categoryBox.setSelectedItem(selected.getCategory());
        });

        autoCalcBtn.addActionListener(e -> {
            Appliance selected = getSelectedAppliance();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select an appliance first.", "No Appliance Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            consumptionField.setText(String.format("%.2f", selected.getDailyConsumptionKwh()));
            hoursField.setText(String.valueOf(selected.getDailyHours()));
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);
        JButton saveBtn = new JButton("Save Record");
        JButton resetBtn = new JButton("Reset");
        saveBtn.addActionListener(e -> saveRecord());
        resetBtn.addActionListener(e -> resetForm());
        buttonPanel.add(saveBtn);
        buttonPanel.add(resetBtn);

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        form.add(buttonPanel, gbc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private Appliance getSelectedAppliance() {
        int idx = applianceBox.getSelectedIndex();
        if (idx <= 0 || appliances == null || idx - 1 >= appliances.size()) return null;
        return appliances.get(idx - 1);
    }

    public void reloadAppliances() {
        SwingWorker<List<Appliance>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Appliance> doInBackground() {
                return applianceDAO.getAll();
            }

            @Override
            protected void done() {
                try {
                    appliances = get();
                    applianceBox.removeAllItems();
                    applianceBox.addItem("None (Manual Entry)");
                    for (Appliance a : appliances) applianceBox.addItem(a.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    /** Called when the default tariff is changed elsewhere (e.g. Bill Calculator) so this form stays in sync. */
    public void refreshTariffDefault() {
        tariffField.setText(String.valueOf(settingsDAO.getTariff()));
    }

    private void saveRecord() {
        String date = DateUtils.getSpinnerDateAsString(dateSpinner);
        String building = buildingField.getText().trim();
        String category = (String) categoryBox.getSelectedItem();
        String consumptionStr = consumptionField.getText().trim();
        String hoursStr = hoursField.getText().trim();
        String meterStr = meterField.getText().trim();
        String tariffStr = tariffField.getText().trim();
        String notes = notesArea.getText().trim();

        StringBuilder errors = new StringBuilder();
        if (!ValidationUtils.isValidPositiveDouble(consumptionStr)) errors.append("- Consumption must be a positive number.\n");
        if (!ValidationUtils.isValidPositiveDouble(hoursStr)) {
            errors.append("- Operating hours must be a positive number.\n");
        } else {
            double h = Double.parseDouble(hoursStr);
            if (h > 24) errors.append("- Operating hours cannot exceed 24.\n");
        }
        if (!ValidationUtils.isValidPositiveDouble(tariffStr)) errors.append("- Tariff must be a positive number.\n");
        if (!meterStr.isEmpty() && !ValidationUtils.isValidNonNegativeDouble(meterStr))
            errors.append("- Meter reading must be a non-negative number.\n");

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, "Please fix the following:\n" + errors, "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        EnergyRecord r = new EnergyRecord();
        r.setRecordDate(date);
        r.setBuildingName(building);
        Appliance selected = getSelectedAppliance();
        r.setApplianceId(selected != null ? selected.getId() : null);
        r.setCategory(category);
        r.setConsumptionKwh(Double.parseDouble(consumptionStr));
        r.setOperatingHours(Double.parseDouble(hoursStr));
        r.setMeterReading(ValidationUtils.parseDoubleOrNull(meterStr));
        r.setTariff(Double.parseDouble(tariffStr));
        r.setNotes(notes);

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                return energyRecordDAO.insert(r);
            }

            @Override
            protected void done() {
                try {
                    int id = get();
                    if (id > 0) {
                        JOptionPane.showMessageDialog(EnergyInputPanel.this, "Energy record saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        resetForm();
                        if (onDataChanged != null) onDataChanged.run();
                    } else {
                        JOptionPane.showMessageDialog(EnergyInputPanel.this, "Failed to save record.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void resetForm() {
        dateSpinner.setValue(new Date());
        buildingField.setText("Main Building");
        applianceBox.setSelectedIndex(0);
        categoryBox.setSelectedIndex(0);
        consumptionField.setText("");
        hoursField.setText("");
        meterField.setText("");
        tariffField.setText(String.valueOf(settingsDAO.getTariff()));
        notesArea.setText("");
    }

    private int addFormRow(JPanel form, GridBagConstraints gbc, int y, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(Constants.FONT_LABEL_BOLD);
        form.add(label, gbc);

        gbc.gridy = y + 1;
        gbc.weightx = 1;
        form.add(field, gbc);

        return y + 2;
    }
}