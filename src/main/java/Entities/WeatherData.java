package Entities;

import java.util.List;

public class WeatherData {
    private List<String> times;
    private List<Double> temperatures;

    public WeatherData(List<String> times, List<Double> temperatures) {
        this.times = times;
        this.temperatures = temperatures;
    }

    public List<String> getTimes() { return times; }
    public List<Double> getTemperatures() { return temperatures; }
}
