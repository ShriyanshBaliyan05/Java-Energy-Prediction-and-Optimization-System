package com.energy.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.energy.database.ApplianceDAO;
import com.energy.model.Appliance;
import com.energy.utils.Constants;
import com.energy.utils.ValidationUtils;

public class AppliancePanel extends JPanel {

    private final ApplianceDAO applianceDAO = new ApplianceDAO();
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private Runnable onDataChanged;

    public AppliancePanel() {
        setLayout(new BorderLayout());
        setBackground(Constants.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);

        refreshData();
    }

    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) onDataChanged.run();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Appliance Management");
        title.setFont(Constants.FONT_HEADING);
        title.setForeground(Constants.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Add and manage the appliances you want to track");
        subtitle.setFont(Constants.FONT_BODY);
        subtitle.setForeground(Constants.COLOR_TEXT_SECONDARY);

        titleBox.add(title);
        titleBox.add(subtitle);

        JButton addButton = new JButton("+ Add Appliance");
        styleButton(addButton, Constants.COLOR_PRIMARY);
        addButton.addActionListener(e -> showApplianceDialog(null));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);

        searchField = new JTextField(14);
        searchField.setToolTipText("Search by appliance name");
        searchField.addActionListener(e -> refreshData());

        categoryFilter = new JComboBox<>(Constants.APPLIANCE_CATEGORIES_WITH_ALL);
        categoryFilter.addActionListener(e -> refreshData());

        JButton searchBtn = new JButton("Search");
        styleButton(searchBtn, Constants.COLOR_SECONDARY_ACCENT);
        searchBtn.addActionListener(e -> refreshData());

        filterPanel.add(new JLabel("Search:"){{ setForeground(Constants.COLOR_TEXT_SECONDARY); }});
        filterPanel.add(searchField);
        filterPanel.add(categoryFilter);
        filterPanel.add(searchBtn);
        filterPanel.add(addButton);

        header.add(titleBox, BorderLayout.WEST);
        header.add(filterPanel, BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {
                "ID", "Name", "Category", "Power (W)", "Qty",
                "Daily Hrs", "Daily kWh", "Monthly kWh", "Location"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(Constants.FONT_BODY);
        table.setForeground(Constants.COLOR_TEXT_PRIMARY);
        table.setBackground(Constants.COLOR_CARD);
        table.setGridColor(Constants.COLOR_SIDEBAR);
        table.getTableHeader().setFont(Constants.FONT_SMALL);
        table.getTableHeader().setBackground(Constants.COLOR_SIDEBAR);
        table.getTableHeader().setForeground(Constants.COLOR_TEXT_SECONDARY);
        table.setSelectionBackground(Constants.COLOR_PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Constants.COLOR_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(Constants.COLOR_SIDEBAR));
        return scrollPane;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        bar.setOpaque(false);

        JButton editBtn = new JButton("Edit Selected");
        styleButton(editBtn, Constants.COLOR_SECONDARY_ACCENT);
        editBtn.addActionListener(e -> editSelected());

        JButton deleteBtn = new JButton("Delete Selected");
        styleButton(deleteBtn, Constants.COLOR_ERROR);
        deleteBtn.addActionListener(e -> deleteSelected());

        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn, Constants.COLOR_PRIMARY);
        refreshBtn.addActionListener(e -> refreshData());

        bar.add(editBtn);
        bar.add(deleteBtn);
        bar.add(refreshBtn);
        return bar;
    }

    private void styleButton(JButton button, Color bg) {
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void refreshData() {
        String keyword = searchField != null ? searchField.getText() : "";
        String category = categoryFilter != null ? (String) categoryFilter.getSelectedItem() : "All Categories";

        SwingWorker<List<Appliance>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Appliance> doInBackground() {
                return applianceDAO.search(keyword, category);
            }

            @Override
            protected void done() {
                try {
                    List<Appliance> list = get();
                    tableModel.setRowCount(0);
                    for (Appliance a : list) {
                        tableModel.addRow(new Object[]{
                                a.getId(),
                                a.getName(),
                                a.getCategory(),
                                df.format(a.getPowerWatts()),
                                a.getQuantity(),
                                df.format(a.getDailyHours()),
                                df.format(a.getDailyConsumptionKwh()),
                                df.format(a.getMonthlyConsumptionKwh()),
                                a.getLocation() != null ? a.getLocation() : "-"
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appliance to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        Appliance appliance = applianceDAO.getById(id);
        if (appliance != null) {
            showApplianceDialog(appliance);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appliance to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete \"" + name + "\"?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return applianceDAO.delete(id);
                }

                @Override
                protected void done() {
                    refreshData();
                    notifyDataChanged();
                    JOptionPane.showMessageDialog(AppliancePanel.this,
                            "Appliance deleted successfully.", "Deleted",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            };
            worker.execute();
        }
    }

    private void showApplianceDialog(Appliance existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Appliance" : "Add Appliance", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 520);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Constants.COLOR_CARD);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Constants.COLOR_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int y = 0;

        JTextField nameField = new JTextField(existing != null ? existing.getName() : "");
        addFormRow(form, gbc, y++, "Appliance Name *", nameField);

        JComboBox<String> categoryBox = new JComboBox<>(Constants.APPLIANCE_CATEGORIES);
        if (isEdit) categoryBox.setSelectedItem(existing.getCategory());
        addFormRow(form, gbc, y++, "Category *", categoryBox);

        JTextField powerField = new JTextField(existing != null ? String.valueOf(existing.getPowerWatts()) : "");
        addFormRow(form, gbc, y++, "Power Rating (Watts) *", powerField);

        JTextField qtyField = new JTextField(existing != null ? String.valueOf(existing.getQuantity()) : "1");
        addFormRow(form, gbc, y++, "Quantity *", qtyField);

        JTextField hoursField = new JTextField(existing != null ? String.valueOf(existing.getDailyHours()) : "");
        addFormRow(form, gbc, y++, "Daily Operating Hours *", hoursField);

        JTextField daysField = new JTextField(existing != null ? String.valueOf(existing.getDaysPerMonth()) : "30");
        addFormRow(form, gbc, y++, "Days Used Per Month *", daysField);

        JComboBox<String> efficiencyBox = new JComboBox<>(Constants.EFFICIENCY_RATINGS);
        if (isEdit && existing.getEfficiencyRating() != null) {
            efficiencyBox.setSelectedItem(existing.getEfficiencyRating());
        }
        addFormRow(form, gbc, y++, "Efficiency Rating", efficiencyBox);

        JTextField locationField = new JTextField(existing != null && existing.getLocation() != null
                ? existing.getLocation() : "");
        addFormRow(form, gbc, y++, "Location (optional)", locationField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Constants.COLOR_CARD);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton(isEdit ? "Update Appliance" : "Save Appliance");
        styleButton(saveBtn, Constants.COLOR_PRIMARY);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String category = (String) categoryBox.getSelectedItem();
            String powerText = powerField.getText().trim();
            String qtyText = qtyField.getText().trim();
            String hoursText = hoursField.getText().trim();
            String daysText = daysField.getText().trim();
            String efficiency = (String) efficiencyBox.getSelectedItem();
            String location = locationField.getText().trim();

            StringBuilder errors = new StringBuilder();
            if (!ValidationUtils.isNotEmpty(name)) errors.append("- Appliance name is required.\n");
            if (!ValidationUtils.isValidPositiveDouble(powerText)) errors.append("- Power rating must be a positive number.\n");
            if (!ValidationUtils.isValidPositiveInteger(qtyText)) errors.append("- Quantity must be a positive whole number.\n");
            if (!ValidationUtils.isValidNonNegativeDouble(hoursText) || Double.parseDouble(ValidationUtils.isValidDouble(hoursText) ? hoursText : "0") > 24)
                errors.append("- Daily operating hours must be between 0 and 24.\n");
            if (!ValidationUtils.isValidPositiveInteger(daysText) || Integer.parseInt(ValidationUtils.isValidInteger(daysText) ? daysText : "0") > 31)
                errors.append("- Days used per month must be between 1 and 31.\n");

            if (errors.length() > 0) {
                JOptionPane.showMessageDialog(dialog, errors.toString(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Appliance appliance = isEdit ? existing : new Appliance();
            appliance.setName(name);
            appliance.setCategory(category);
            appliance.setPowerWatts(Double.parseDouble(powerText));
            appliance.setQuantity(Integer.parseInt(qtyText));
            appliance.setDailyHours(Double.parseDouble(hoursText));
            appliance.setDaysPerMonth(Integer.parseInt(daysText));
            appliance.setEfficiencyRating(efficiency);
            appliance.setLocation(location.isEmpty() ? null : location);

            dialog.dispose();

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    if (isEdit) {
                        return applianceDAO.update(appliance);
                    } else {
                        return applianceDAO.insert(appliance) > 0;
                    }
                }

                @Override
                protected void done() {
                    refreshData();
                    notifyDataChanged();
                    try {
                        boolean success = get();
                        JOptionPane.showMessageDialog(AppliancePanel.this,
                                success ? ("Appliance " + (isEdit ? "updated" : "saved") + " successfully.")
                                        : "Operation failed. Please try again.",
                                success ? "Success" : "Error",
                                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            worker.execute();
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int y, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        JLabel label = new JLabel(labelText);
        label.setFont(Constants.FONT_SMALL);
        label.setForeground(Constants.COLOR_TEXT_SECONDARY);
        form.add(label, gbc);

        gbc.gridy = y + 1;
        form.add(field, gbc);
    }
}