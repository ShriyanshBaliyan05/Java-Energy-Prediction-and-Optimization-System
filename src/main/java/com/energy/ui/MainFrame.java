package com.energy.ui;

import com.energy.components.Sidebar;
import com.energy.utils.Constants;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_ENERGY_INPUT = "ENERGY_INPUT";
    public static final String CARD_APPLIANCES = "APPLIANCES";
    public static final String CARD_PREDICTION = "PREDICTION";
    public static final String CARD_ANALYTICS = "ANALYTICS";
    public static final String CARD_BILL_CALCULATOR = "BILL_CALCULATOR";
    public static final String CARD_OPTIMIZATION = "OPTIMIZATION";
    public static final String CARD_HISTORY = "HISTORY";
    public static final String CARD_REPORTS = "REPORTS";
    public static final String CARD_SETTINGS = "SETTINGS";
    public static final String CARD_ABOUT = "ABOUT";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private Sidebar sidebar;

    private DashboardPanel dashboardPanel;
    private EnergyInputPanel energyInputPanel;
    private AppliancePanel appliancePanel;
    private PredictionPanel predictionPanel;
    private AnalyticsPanel analyticsPanel;
    private BillCalculatorPanel billCalculatorPanel;
    private OptimizationPanel optimizationPanel;
    private HistoryPanel historyPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;
    private AboutPanel aboutPanel;

    public MainFrame() {
        setTitle("Energy Consumption Prediction and Optimization System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 720));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Constants.COLOR_BG_DARK);
        setLayout(new BorderLayout());

        initPanels();
        initSidebar();
        initContentArea();

        cardLayout.show(contentPanel, CARD_DASHBOARD);
        dashboardPanel.refreshData();
    }

    private void initPanels() {
        dashboardPanel = new DashboardPanel();
        energyInputPanel = new EnergyInputPanel();
        appliancePanel = new AppliancePanel();
        predictionPanel = new PredictionPanel();
        analyticsPanel = new AnalyticsPanel();
        billCalculatorPanel = new BillCalculatorPanel();
        optimizationPanel = new OptimizationPanel();
        historyPanel = new HistoryPanel();
        reportsPanel = new ReportsPanel();
        settingsPanel = new SettingsPanel();
        aboutPanel = new AboutPanel();

        billCalculatorPanel.setOnSettingsChanged(() -> {
            energyInputPanel.refreshTariffDefault();
            dashboardPanel.refreshData();
        });

        settingsPanel.setOnSettingsChanged(() -> {
            energyInputPanel.refreshTariffDefault();
            dashboardPanel.refreshData();
        });
    }

    private void initSidebar() {

        sidebar = new Sidebar();
        sidebar.setNavigationListener(this::navigateTo);
        add(sidebar, BorderLayout.WEST);
    }

    private void initContentArea() {
        contentPanel.setBackground(Constants.COLOR_BG_DARK);

        contentPanel.add(dashboardPanel, CARD_DASHBOARD);
        contentPanel.add(energyInputPanel, CARD_ENERGY_INPUT);
        contentPanel.add(appliancePanel, CARD_APPLIANCES);
        contentPanel.add(predictionPanel, CARD_PREDICTION);
        contentPanel.add(analyticsPanel, CARD_ANALYTICS);
        contentPanel.add(billCalculatorPanel, CARD_BILL_CALCULATOR);
        contentPanel.add(optimizationPanel, CARD_OPTIMIZATION);
        contentPanel.add(historyPanel, CARD_HISTORY);
        contentPanel.add(reportsPanel, CARD_REPORTS);
        contentPanel.add(settingsPanel, CARD_SETTINGS);
        contentPanel.add(aboutPanel, CARD_ABOUT);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void navigateTo(String cardName) {
        cardLayout.show(contentPanel, cardName);

        switch (cardName) {
            case CARD_DASHBOARD:
                dashboardPanel.refreshData();
                break;
            case CARD_APPLIANCES:
                appliancePanel.refreshData();
                break;
            case CARD_PREDICTION:
                predictionPanel.refreshData();
                break;
            case CARD_ANALYTICS:
                analyticsPanel.refreshData();
                break;
            case CARD_OPTIMIZATION:
                optimizationPanel.refreshData();
                break;
            case CARD_HISTORY:
                historyPanel.refreshData();
                break;
            case CARD_SETTINGS:
                settingsPanel.refreshData();
                break;
            case CARD_ENERGY_INPUT:
                energyInputPanel.refreshTariffDefault();
                break;
            default:
            
                break;
        }
    }
}
