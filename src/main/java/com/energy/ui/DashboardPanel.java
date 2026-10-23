package com.energy.ui;

import com.energy.components.StatCard;
import com.energy.service.DashboardSummaryService;
import com.energy.utils.Constants;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private final DashboardSummaryService summaryService = new DashboardSummaryService();
    private final StatCard appliancesCard;
    private final StatCard recordsCard;
    private final StatCard consumptionCard;
    private final StatCard costCard;

    public DashboardPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Constants.COLOR_BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.COLOR_TEXT_PRIMARY);
        add(title, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 20, 20));
        cardsPanel.setOpaque(false);

        appliancesCard = new StatCard("\u25A0", "Total Appliances", "0", Constants.COLOR_ACCENT);
        recordsCard = new StatCard("\u25AA", "Energy Records", "0", Constants.COLOR_ACCENT_GREEN);
        consumptionCard = new StatCard("\u25C6", "Total Consumption (kWh)", "0.00", Constants.COLOR_ACCENT_ORANGE);
        costCard = new StatCard("\u25CF", "Energy Cost (kWh x Tariff)", Constants.CURRENCY_SYMBOL + " 0.00", Constants.COLOR_ACCENT_RED);

        cardsPanel.add(appliancesCard);
        cardsPanel.add(recordsCard);
        cardsPanel.add(consumptionCard);
        cardsPanel.add(costCard);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(cardsPanel, BorderLayout.NORTH);

        JLabel note = new JLabel("Note: Energy Cost shown here excludes fixed charges and taxes. Use the Bill Calculator for a full estimated bill.");
        note.setFont(Constants.FONT_SMALL);
        note.setForeground(Constants.COLOR_TEXT_SECONDARY);
        note.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        wrapper.add(note, BorderLayout.SOUTH);

        add(wrapper, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        SwingWorker<DashboardSummaryService.Summary, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardSummaryService.Summary doInBackground() {
                return summaryService.getSummary();
            }

            @Override
            protected void done() {
                try {
                    DashboardSummaryService.Summary s = get();
                    appliancesCard.setValue(String.valueOf(s.totalAppliances));
                    recordsCard.setValue(String.valueOf(s.totalRecords));
                    consumptionCard.setValue(String.format("%.2f", s.totalConsumptionKwh));
                    costCard.setValue(Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", s.energyCost));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}