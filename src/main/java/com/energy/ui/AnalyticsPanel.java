package com.energy.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.energy.components.RoundedPanel;
import com.energy.components.SimpleBarChart;
import com.energy.components.SimplePieChart;
import com.energy.model.DailyConsumption;
import com.energy.service.AnalyticsService;
import com.energy.utils.Constants;
import com.energy.utils.DateUtils;

public class AnalyticsPanel extends JPanel {

    private final AnalyticsService analyticsService = new AnalyticsService();

    private JSpinner fromDateSpinner;
    private JSpinner toDateSpinner;

    private JLabel totalKwhLabel;
    private JLabel avgDailyLabel;
    private JLabel peakDayLabel;
    private JLabel totalCostLabel;

    private SimplePieChart pieChart;
    private SimpleBarChart trendChart;
    private SimpleBarChart categoryBarChart;

    private DefaultTableModel loggedTableModel;
    private DefaultTableModel projectedTableModel;

    public AnalyticsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Constants.COLOR_BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(buildStatRow());
        center.add(Box.createVerticalStrut(16));
        center.add(buildChartsRow());
        center.add(Box.createVerticalStrut(16));
        center.add(buildTablesRow());

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Energy Analytics");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.COLOR_TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        fromDateSpinner = DateUtils.createDateSpinner();
        toDateSpinner = DateUtils.createDateSpinner();
        shiftSpinnerMonths(fromDateSpinner, -1);

        JButton applyBtn = new JButton("Apply Range");
        applyBtn.addActionListener(e -> refreshData());
        JButton allBtn = new JButton("All Dates");
        allBtn.addActionListener(e -> {
            fromDateSpinner.setValue(new Date(0L));
            toDateSpinner.setValue(new Date());
            refreshData();
        });

        controls.add(new JLabel("From:"));
        controls.add(fromDateSpinner);
        controls.add(new JLabel("To:"));
        controls.add(toDateSpinner);
        controls.add(applyBtn);
        controls.add(allBtn);

        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private void shiftSpinnerMonths(JSpinner spinner, int months) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, months);
        spinner.setValue(cal.getTime());
    }

    private JPanel buildStatRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        totalKwhLabel = statCard("Total Consumption", "0.00 kWh");
        avgDailyLabel = statCard("Avg / Logged Day", "0.00 kWh");
        peakDayLabel = statCard("Peak Day", "-");
        totalCostLabel = statCard("Energy Cost", Constants.CURRENCY_SYMBOL + " 0.00");

        row.add(wrapStat(totalKwhLabel, "Total Consumption"));
        row.add(wrapStat(avgDailyLabel, "Avg / Logged Day"));
        row.add(wrapStat(peakDayLabel, "Peak Day"));
        row.add(wrapStat(totalCostLabel, "Energy Cost"));
        return row;
    }

    private JLabel statCard(String unused, String initial) {
        JLabel v = new JLabel(initial);
        v.setFont(Constants.FONT_VALUE.deriveFont(18f));
        v.setForeground(Constants.COLOR_ACCENT);
        return v;
    }

    private RoundedPanel wrapStat(JLabel valueLabel, String title) {
        RoundedPanel p = new RoundedPanel(16, Constants.COLOR_CARD_BG);
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JLabel t = new JLabel(title);
        t.setFont(Constants.FONT_SMALL);
        t.setForeground(Constants.COLOR_TEXT_SECONDARY);
        p.add(t, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildChartsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        row.setPreferredSize(new Dimension(100, 280));

        pieChart = new SimplePieChart();
        categoryBarChart = new SimpleBarChart();
        trendChart = new SimpleBarChart();

        row.add(chartCard("Share by Category (logged kWh)", pieChart));
        row.add(chartCard("Category Totals", categoryBarChart));
        row.add(chartCard("Daily Trend (logged)", trendChart));
        return row;
    }

    private RoundedPanel chartCard(String heading, JComponent chart) {
        RoundedPanel p = new RoundedPanel(16, Constants.COLOR_CARD_BG);
        p.setLayout(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JLabel h = new JLabel(heading);
        h.setFont(Constants.FONT_HEADING);
        h.setForeground(Constants.COLOR_TEXT_PRIMARY);
        p.add(h, BorderLayout.NORTH);
        p.add(chart, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildTablesRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setPreferredSize(new Dimension(100, 280));

        loggedTableModel = new DefaultTableModel(new String[]{"Appliance", "Category", "Logged kWh", "Cost"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        projectedTableModel = new DefaultTableModel(new String[]{"Appliance", "Category", "Monthly kWh", "Cost"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        row.add(tableCard("Top consumers (from logged energy records)", loggedTableModel));
        row.add(tableCard("Top consumers (projected from registered appliances)", projectedTableModel));
        return row;
    }

    private RoundedPanel tableCard(String heading, DefaultTableModel model) {
        RoundedPanel p = new RoundedPanel(16, Constants.COLOR_CARD_BG);
        p.setLayout(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JLabel h = new JLabel(heading);
        h.setFont(Constants.FONT_HEADING);
        h.setForeground(Constants.COLOR_TEXT_PRIMARY);
        p.add(h, BorderLayout.NORTH);
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(Constants.FONT_LABEL);
        table.getTableHeader().setFont(Constants.FONT_LABEL_BOLD);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    public void refreshData() {
        String from = DateUtils.getSpinnerDateAsString(fromDateSpinner);
        String to = DateUtils.getSpinnerDateAsString(toDateSpinner);
        if (from.compareTo(to) > 0) {
            JOptionPane.showMessageDialog(this, "'From Date' must not be after 'To Date'.", "Invalid Range", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<AnalyticsService.Snapshot, Void> worker = new SwingWorker<>() {
            @Override
            protected AnalyticsService.Snapshot doInBackground() {
                return analyticsService.buildSnapshot(from, to);
            }

            @Override
            protected void done() {
                try {
                    render(get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void render(AnalyticsService.Snapshot s) {
        totalKwhLabel.setText(String.format("%.2f kWh", s.totalKwh));
        avgDailyLabel.setText(String.format("%.2f kWh", s.avgDailyKwh));
        if (s.dayCount == 0) {
            peakDayLabel.setText("No logged days");
        } else {
            peakDayLabel.setText(s.peakDay + "  (" + String.format("%.2f", s.peakDayKwh) + " kWh)");
        }
        totalCostLabel.setText(Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", s.totalCost));

        List<String> catLabels = new ArrayList<>();
        List<Double> catValues = new ArrayList<>();
        for (AnalyticsService.CategoryShare c : s.categoryShares) {
            catLabels.add(c.category);
            catValues.add(c.kwh);
        }
        pieChart.setData(catLabels, catValues);
        categoryBarChart.setData(catLabels, catValues, -1, "kWh");

        List<String> dayLabels = new ArrayList<>();
        List<Double> dayValues = new ArrayList<>();
        int n = s.dailySeries.size();
        int start = Math.max(0, n - 14);
        for (int i = start; i < n; i++) {
            DailyConsumption d = s.dailySeries.get(i);
            dayLabels.add(d.getDate());
            dayValues.add(d.getTotalKwh());
        }
        trendChart.setData(dayLabels, dayValues, -1, "kWh");

        loggedTableModel.setRowCount(0);
        int loggedLimit = 0;
        for (AnalyticsService.ApplianceRank r : s.topLogged) {
            if (loggedLimit++ >= 10) break;
            loggedTableModel.addRow(new Object[]{
                    r.name, r.category,
                    String.format("%.2f", r.kwh),
                    Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", r.cost)
            });
        }

        projectedTableModel.setRowCount(0);
        int projLimit = 0;
        for (AnalyticsService.ApplianceRank r : s.topProjected) {
            if (projLimit++ >= 10) break;
            projectedTableModel.addRow(new Object[]{
                    r.name, r.category,
                    String.format("%.2f", r.kwh),
                    Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", r.cost)
            });
        }
    }
}