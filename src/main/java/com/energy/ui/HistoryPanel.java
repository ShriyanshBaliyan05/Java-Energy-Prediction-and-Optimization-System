package com.energy.ui;

import com.energy.database.EnergyRecordDAO;
import com.energy.model.EnergyRecord;
import com.energy.utils.Constants;
import com.energy.utils.DateUtils;
import com.energy.utils.ValidationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class HistoryPanel extends JPanel {

    private final EnergyRecordDAO energyRecordDAO = new EnergyRecordDAO();
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JSpinner fromDateSpinner;
    private JSpinner toDateSpinner;
    private Runnable onDataChanged;

    public HistoryPanel() {
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

        JLabel title = new JLabel("Historical Energy Records");
        title.setFont(Constants.FONT_HEADING);
        title.setForeground(Constants.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("View, search, and manage all saved consumption entries");
        subtitle.setFont(Constants.FONT_BODY);
        subtitle.setForeground(Constants.COLOR_TEXT_SECONDARY);

        titleBox.add(title);
        titleBox.add(subtitle);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterPanel.setOpaque(false);

        searchField = new JTextField(12);
        fromDateSpinner = DateUtils.createDateSpinner();
        toDateSpinner = DateUtils.createDateSpinner();

        JButton filterBtn = new JButton("Apply Filter");
        styleButton(filterBtn, Constants.COLOR_SECONDARY_ACCENT);
        filterBtn.addActionListener(e -> refreshData());

        JButton clearBtn = new JButton("Clear");
        styleButton(clearBtn, Constants.COLOR_SIDEBAR);
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            fromDateSpinner.setValue(new java.util.Date(0));
            toDateSpinner.setValue(new java.util.Date());
            refreshData();
        });

        filterPanel.add(labelFor("Search:"));
        filterPanel.add(searchField);
        filterPanel.add(labelFor("From:"));
        filterPanel.add(fromDateSpinner);
        filterPanel.add(labelFor("To:"));
        filterPanel.add(toDateSpinner);
        filterPanel.add(filterBtn);
        filterPanel.add(clearBtn);

        header.add(titleBox, BorderLayout.WEST);
        header.add(filterPanel, BorderLayout.EAST);
        return header;
    }

    private JLabel labelFor(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Constants.COLOR_TEXT_SECONDARY);
        return l;
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {
                "ID", "Date", "Building", "Appliance", "Category",
                "Consumption (kWh)", "Hours", "Cost (Rs)", "Notes"
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
        String fromDate = DateUtils.getSpinnerDateAsString(fromDateSpinner);
        String toDate = DateUtils.getSpinnerDateAsString(toDateSpinner);

        SwingWorker<List<EnergyRecord>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<EnergyRecord> doInBackground() {
                return energyRecordDAO.search(keyword, fromDate, toDate);
            }

            @Override
            protected void done() {
                try {
                    List<EnergyRecord> list = get();
                    tableModel.setRowCount(0);
                    for (EnergyRecord r : list) {
                        tableModel.addRow(new Object[]{
                                r.getId(),
                                r.getRecordDate(),
                                r.getBuildingName(),
                                r.getApplianceName() != null ? r.getApplianceName() : "Manual Entry",
                                r.getCategory() != null ? r.getCategory() : "-",
                                df.format(r.getConsumptionKwh()),
                                df.format(r.getOperatingHours()),
                                df.format(r.getEstimatedCost()),
                                r.getNotes() != null ? r.getNotes() : ""
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
            JOptionPane.showMessageDialog(this, "Please select a record to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);

        SwingWorker<EnergyRecord, Void> finder = new SwingWorker<>() {
            @Override
            protected EnergyRecord doInBackground() {
                List<EnergyRecord> all = energyRecordDAO.getAll();
                for (EnergyRecord r : all) {
                    if (r.getId() == id) return r;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    EnergyRecord record = get();
                    if (record != null) showEditDialog(record);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        finder.execute();
    }

    private void showEditDialog(EnergyRecord record) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Edit Energy Record", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 460);
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
        gbc.gridx = 0;

        int y = 0;

        JSpinner dateSpinner = DateUtils.createDateSpinner();
        dateSpinner.setValue(DateUtils.parseDate(record.getRecordDate()));
        addRow(form, gbc, y, "Record Date", dateSpinner); y += 2;

        JTextField buildingField = new JTextField(record.getBuildingName());
        addRow(form, gbc, y, "Building / Room", buildingField); y += 2;

        JTextField consumptionField = new JTextField(String.valueOf(record.getConsumptionKwh()));
        addRow(form, gbc, y, "Consumption (kWh)", consumptionField); y += 2;

        JTextField hoursField = new JTextField(String.valueOf(record.getOperatingHours()));
        addRow(form, gbc, y, "Operating Hours", hoursField); y += 2;

        JTextField tariffField = new JTextField(String.valueOf(record.getTariff()));
        addRow(form, gbc, y, "Tariff (Rs/kWh)", tariffField); y += 2;

        JTextArea notesArea = new JTextArea(record.getNotes() != null ? record.getNotes() : "", 3, 20);
        notesArea.setLineWrap(true);
        addRow(form, gbc, y, "Notes", new JScrollPane(notesArea)); y += 2;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Constants.COLOR_CARD);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton updateBtn = new JButton("Update Record");
        styleButton(updateBtn, Constants.COLOR_PRIMARY);
        updateBtn.addActionListener(e -> {
            String building = buildingField.getText().trim();
            String consumptionText = consumptionField.getText().trim();
            String hoursText = hoursField.getText().trim();
            String tariffText = tariffField.getText().trim();

            StringBuilder errors = new StringBuilder();
            if (!ValidationUtils.isNotEmpty(building)) errors.append("- Building name is required.\n");
            if (!ValidationUtils.isValidPositiveDouble(consumptionText)) errors.append("- Consumption must be positive.\n");
            if (!ValidationUtils.isValidNonNegativeDouble(hoursText)) errors.append("- Hours must be valid.\n");
            if (!ValidationUtils.isValidPositiveDouble(tariffText)) errors.append("- Tariff must be positive.\n");

            if (errors.length() > 0) {
                JOptionPane.showMessageDialog(dialog, errors.toString(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            record.setRecordDate(DateUtils.getSpinnerDateAsString(dateSpinner));
            record.setBuildingName(building);
            record.setConsumptionKwh(Double.parseDouble(consumptionText));
            record.setOperatingHours(Double.parseDouble(hoursText));
            record.setTariff(Double.parseDouble(tariffText));
            record.setNotes(notesArea.getText().trim());

            dialog.dispose();

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return energyRecordDAO.update(record);
                }

                @Override
                protected void done() {
                    refreshData();
                    notifyDataChanged();
                    JOptionPane.showMessageDialog(HistoryPanel.this,
                            "Record updated successfully.", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            };
            worker.execute();
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(updateBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int y, String labelText, JComponent field) {
        gbc.gridy = y;
        gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(Constants.FONT_SMALL);
        label.setForeground(Constants.COLOR_TEXT_SECONDARY);
        form.add(label, gbc);

        gbc.gridy = y + 1;
        form.add(field, gbc);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this record?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return energyRecordDAO.delete(id);
                }

                @Override
                protected void done() {
                    refreshData();
                    notifyDataChanged();
                    JOptionPane.showMessageDialog(HistoryPanel.this,
                            "Record deleted successfully.", "Deleted",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            };
            worker.execute();
        }
    }
}