package com.energy;
import com.energy.database.DatabaseInitializer;
import com.energy.ui.SplashScreen;
import javax.swing.*;
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            DatabaseInitializer.initialize();
            SplashScreen splash = new SplashScreen();
            splash.showSplash();
        });
    }
}
