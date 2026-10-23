package com.energy.service;

import java.util.List;
import java.util.stream.Collectors;

import com.energy.database.EnergyRecordDAO;
import com.energy.database.SettingsDAO;
import com.energy.model.DailyConsumption;

/**

 * Algorithm selection (automatic, based on available historical data points):
 *   - 1 data point      -> value used as-is (flagged low-confidence)
 *   - 2-6 data points   -> Simple Moving Average (avg of up to last 7 days)
 *   - 7+ data points    -> Linear Regression (Least Squares trend line)
 *
 * Accuracy is evaluated via leave-last-out backtesting: the most recent known
 * day is held out, predicted from everything before it using the same logic,
 * and compared to the real value. This is a standard, auditable statistical
 * validation technique - not a guessed confidence score.
 */
public class PredictionService {

    public static final int MIN_POINTS_FOR_REGRESSION = 7;
    public static final int MOVING_AVERAGE_MAX_WINDOW = 7;

    public static final String PERIOD_DAILY = "Daily";
    public static final String PERIOD_WEEKLY = "Weekly";
    public static final String PERIOD_MONTHLY = "Monthly";

    private final EnergyRecordDAO energyRecordDAO = new EnergyRecordDAO();
    private final SettingsDAO settingsDAO = new SettingsDAO();

    public static class PredictionResult {
        public String category;
        public String period;
        public int dataPointsUsed;
        public String algorithm;
        public double predictedDailyKwh;
        public double predictedConsumption;
        public double predictedCost;
        public Double rSquared;
        public Double backtestAccuracyPercent;
        public List<DailyConsumption> series;
        public String limitationsNote;
    }

    private static class PredictedValue {
        final double value;
        final String algorithm;
        final Double rSquared;
        PredictedValue(double value, String algorithm, Double rSquared) {
            this.value = value;
            this.algorithm = algorithm;
            this.rSquared = rSquared;
        }
    }

    private static class LinearRegressionResult {
        final double slope, intercept, rSquared;
        LinearRegressionResult(double slope, double intercept, double rSquared) {
            this.slope = slope;
            this.intercept = intercept;
            this.rSquared = rSquared;
        }
    }

    /** Returns null if there is no historical data at all for the given category filter. */
    public PredictionResult predict(String category, String period) {
        List<DailyConsumption> series = energyRecordDAO.getDailySeries(category);
        if (series.isEmpty()) return null;

        List<Double> values = series.stream()
                .map(DailyConsumption::getTotalKwh)
                .collect(Collectors.toList());

        PredictedValue pv = predictNext(values);

        double multiplier;
        switch (period) {
            case PERIOD_WEEKLY: multiplier = 7; break;
            case PERIOD_MONTHLY: multiplier = 30; break;
            default: multiplier = 1; break;
        }

        PredictionResult result = new PredictionResult();
        result.category = category;
        result.period = period;
        result.dataPointsUsed = values.size();
        result.algorithm = pv.algorithm;
        result.predictedDailyKwh = pv.value;
        result.predictedConsumption = pv.value * multiplier;
        result.predictedCost = result.predictedConsumption * settingsDAO.getTariff();
        result.rSquared = pv.rSquared;
        result.backtestAccuracyPercent = backtestAccuracy(values);
        result.series = series;
        result.limitationsNote = buildLimitationsNote(values.size());
        return result;
    }

    private PredictedValue predictNext(List<Double> values) {
        int n = values.size();

        if (n == 1) {
            return new PredictedValue(Math.max(0, values.get(0)),
                    "Single Data Point (insufficient history - value used as-is)", null);
        }

        if (n < MIN_POINTS_FOR_REGRESSION) {
            int window = Math.min(n, MOVING_AVERAGE_MAX_WINDOW);
            List<Double> last = values.subList(n - window, n);
            double avg = last.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            return new PredictedValue(Math.max(0, avg),
                    "Simple Moving Average (last " + window + " data point" + (window > 1 ? "s" : "") + ")", null);
        }

        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = i;
            ys[i] = values.get(i);
        }
        LinearRegressionResult lr = computeLinearRegression(xs, ys);
        double predicted = lr.intercept + lr.slope * n;
        return new PredictedValue(Math.max(0, predicted), "Linear Regression (Least Squares Trend)", lr.rSquared);
    }

    /** Standard least-squares formulas: slope = (nΣxy - ΣxΣy) / (nΣx² - (Σx)²), intercept = (Σy - slope·Σx) / n */
    private LinearRegressionResult computeLinearRegression(double[] xs, double[] ys) {
        int n = xs.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            sumX += xs[i];
            sumY += ys[i];
            sumXY += xs[i] * ys[i];
            sumXX += xs[i] * xs[i];
        }
        double denom = (n * sumXX) - (sumX * sumX);
        double slope = denom == 0 ? 0 : ((n * sumXY) - (sumX * sumY)) / denom;
        double intercept = (sumY - slope * sumX) / n;

        double meanY = sumY / n;
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < n; i++) {
            double predicted = intercept + slope * xs[i];
            ssRes += Math.pow(ys[i] - predicted, 2);
            ssTot += Math.pow(ys[i] - meanY, 2);
        }
        double rSquared = ssTot == 0 ? 1.0 : 1 - (ssRes / ssTot);
        return new LinearRegressionResult(slope, intercept, rSquared);
    }

    /**
     * Leave-last-out backtest: trains on all but the most recent data point, predicts
     * that point with the same algorithm-selection logic, and compares to the real value.
     * Returns null if there isn't enough data to validate (need at least 3 points).
     */
    private Double backtestAccuracy(List<Double> values) {
        int n = values.size();
        if (n < 3) return null;
        List<Double> train = values.subList(0, n - 1);
        double actual = values.get(n - 1);
        if (actual == 0) return null;
        PredictedValue pv = predictNext(train);
        double errorPct = Math.abs(actual - pv.value) / actual * 100.0;
        return Math.max(0, 100 - errorPct);
    }

    private String buildLimitationsNote(int n) {
        if (n == 1) {
            return "Only one historical data point is available. This prediction simply repeats that value "
                    + "and carries very low confidence. Add more energy records for a meaningful trend.";
        } else if (n < MIN_POINTS_FOR_REGRESSION) {
            return "Based on only " + n + " data points, a Simple Moving Average is used instead of trend regression. "
                    + "With fewer than " + MIN_POINTS_FOR_REGRESSION + " points, a statistically reliable trend line "
                    + "cannot be fitted. Accuracy will improve as more daily energy records are logged.";
        } else {
            return "Linear Regression assumes consumption changes at a roughly constant rate over time (a straight-line "
                    + "trend). It does not account for seasonal effects, sudden appliance changes, or irregular usage "
                    + "spikes. Treat this as a statistical estimate, not a guarantee.";
        }
    }
}