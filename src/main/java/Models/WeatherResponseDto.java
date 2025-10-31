package Models;

import java.util.List;

public class WeatherResponseDto {
    private String city;
    private List<WeatherPointDto> hourly;
    private String chartBase64;
    private String cacheStatus;

    public WeatherResponseDto(String city, List<WeatherPointDto> hourly, String chartBase64, String cacheStatus) {
        this.city = city;
        this.hourly = hourly;
        this.chartBase64 = chartBase64;
        this.cacheStatus = cacheStatus;
    }

    public String getCity() { return city; }
    public List<WeatherPointDto> getHourly() { return hourly; }
    public String getChartBase64() { return chartBase64; }
    public String getCacheStatus() { return cacheStatus; }
}
