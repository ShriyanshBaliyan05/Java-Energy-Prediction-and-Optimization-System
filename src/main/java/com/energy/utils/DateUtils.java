package com.energy.utils;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static String formatDate(Date date) {
        if (date == null) return "";
        return ISO_FORMAT.format(date);
    }

    public static String today() {
        return formatDate(new Date());
    }

    public static Date parseDate(String text) {
        try {
            return ISO_FORMAT.parse(text);
        } catch (Exception e) {
            return new Date();
        }
    }

    /** Creates a ready-to-use date picker spinner defaulted to today's date. */
    public static JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        spinner.setValue(new Date());
        return spinner;
    }

    public static String getSpinnerDateAsString(JSpinner spinner) {
        Date date = (Date) spinner.getValue();
        return formatDate(date);
    }

    private DateUtils() {
    }
}
