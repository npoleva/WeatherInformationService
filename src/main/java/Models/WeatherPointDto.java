package Models;

public class WeatherPointDto {
    private String time;
    private double temperature;

    public WeatherPointDto(String time, double temperature) {
        this.time = time;
        this.temperature = temperature;
    }

    public String getTime() { return time; }
    public double getTemperature() { return temperature; }
}

