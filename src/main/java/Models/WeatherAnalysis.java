package Models;

public class WeatherAnalysis {
    private double trend;
    private double maxTemp;
    private double minTemp;
    private String summary;
    private String recommendation;

    public WeatherAnalysis(double trend, double maxTemp, double minTemp, String summary, String recommendation) {
        this.trend = trend;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.summary = summary;
        this.recommendation = recommendation;
    }

    public double getTrend() { return trend; }
    public double getMaxTemp() { return maxTemp; }
    public double getMinTemp() { return minTemp; }
    public String getSummary() { return summary; }
    public String getRecommendation() { return recommendation; }
}