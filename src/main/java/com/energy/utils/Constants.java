package com.energy.utils;

import java.awt.Color;
import java.awt.Font;

public class Constants {

    public static final String APP_NAME = "Energy Consumption Prediction and Optimization System";
    public static final String APP_SHORT_NAME = "Energy Prediction";
    public static final String APP_TAGLINE = "Smarter energy, lower costs";
    public static final String APP_VERSION = "1.0.0 (Phase 3 - Calculations and Billing)";

    public static final String DB_PATH = "data/energy.db";
    public static final String DATA_DIRECTORY = "data";
    public static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    public static final Color COLOR_BG_DARK = new Color(0x1B, 0x1F, 0x2A);
    public static final Color COLOR_SIDEBAR = new Color(0x14, 0x17, 0x20);
    public static final Color COLOR_CARD_BG = new Color(0x24, 0x29, 0x38);
    public static final Color COLOR_ACCENT = new Color(0x4C, 0xC9, 0xF0);
    public static final Color COLOR_ACCENT_GREEN = new Color(0x4C, 0xE0, 0x7A);
    public static final Color COLOR_ACCENT_RED = new Color(0xE5, 0x5C, 0x5C);
    public static final Color COLOR_ACCENT_ORANGE = new Color(0xF2, 0xA6, 0x5A);
    public static final Color COLOR_TEXT_PRIMARY = new Color(0xE8, 0xEA, 0xED);
    public static final Color COLOR_TEXT_SECONDARY = new Color(0x9A, 0xA3, 0xB2);
    public static final Color COLOR_BORDER = new Color(0x34, 0x3A, 0x4A);

    // Compatibility names used by the earlier UI panels.
    public static final Color COLOR_BACKGROUND = COLOR_BG_DARK;
    public static final Color COLOR_PRIMARY = COLOR_ACCENT;
    public static final Color COLOR_SECONDARY_ACCENT = COLOR_ACCENT_GREEN;
    public static final Color COLOR_CARD = COLOR_CARD_BG;
    public static final Color COLOR_ERROR = COLOR_ACCENT_RED;

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_VALUE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_ICON = new Font("Segoe UI", Font.PLAIN, 20);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BODY = FONT_LABEL;
    public static final Font FONT_SUBHEADING = FONT_HEADING;

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

    public static final String[] APPLIANCE_CATEGORIES = {
            "Cooling", "Refrigeration", "Lighting", "Entertainment",
            "Kitchen", "Laundry", "Computing", "Heating", "Other"
    };

    public static final String[] APPLIANCE_CATEGORIES_WITH_ALL;
    static {
        APPLIANCE_CATEGORIES_WITH_ALL = new String[APPLIANCE_CATEGORIES.length + 1];
        APPLIANCE_CATEGORIES_WITH_ALL[0] = "All Categories";
        System.arraycopy(APPLIANCE_CATEGORIES, 0, APPLIANCE_CATEGORIES_WITH_ALL, 1, APPLIANCE_CATEGORIES.length);
    }

    public static final String[] EFFICIENCY_RATINGS = {
            "Not Specified", "5 Star", "4 Star", "3 Star", "2 Star", "1 Star"
    };

    public static final double DEFAULT_TARIFF = 8.0;
    public static final String CURRENCY_SYMBOL = "Rs";
}
