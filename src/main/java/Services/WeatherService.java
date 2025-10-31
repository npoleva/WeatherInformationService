package Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import Models.*;
import Entities.*;
import org.knowm.xchart.*;
import Repositories.WeatherCacheRepository;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.imageio.ImageIO;
import java.util.Base64;

public class WeatherService {
    private final WeatherCacheRepository cache;
    private final WeatherApiClient api;
    private final ObjectMapper mapper = new ObjectMapper();

    public WeatherService(WeatherCacheRepository cache, WeatherApiClient api) {
        this.cache = cache;
        this.api = api;
    }

    public WeatherResponseDto getWeather(String city) throws Exception {
        String key = "weather:" + city.toLowerCase();
        String cached = cache.find(key);

        if (cached != null) {
            WeatherResponseDto dto = mapper.readValue(cached, WeatherResponseDto.class);
            return new WeatherResponseDto(
                    dto.getCity(),
                    dto.getHourly(),
                    dto.getChartBase64(),
                    "HIT",
                    dto.getAnalysis()
            );
        }

        System.out.println("Before calling api!!!");

        Coordinates c = api.getCoordinates(city);
        System.out.println("After getting the coordinates!!!");
        WeatherData w = api.getWeather(c.getLatitude(), c.getLongitude());
        System.out.println("After calling api!!!");

        List<WeatherPointDto> list = new ArrayList<>();
        for (int i = 0; i < w.getTimes().size(); i++) {
            list.add(new WeatherPointDto(w.getTimes().get(i), w.getTemperatures().get(i)));
        }

        String chartBase64 = generateChart(w.getTimes(), w.getTemperatures(), city);

        WeatherAnalysis analysis = analyzeWeatherData(list);

        WeatherResponseDto dto = new WeatherResponseDto(city, list, chartBase64, "MISS", analysis);

        cache.save(key, mapper.writeValueAsString(dto));
        return dto;
    }

    private WeatherAnalysis analyzeWeatherData(List<WeatherPointDto> data) {
        if (data.isEmpty()) {
            return new WeatherAnalysis(0, 0, 0, "Недостаточно данных", "");
        }

        double maxTemp = data.stream().mapToDouble(WeatherPointDto::getTemperature).max().orElse(0);
        double minTemp = data.stream().mapToDouble(WeatherPointDto::getTemperature).min().orElse(0);

        double firstTemp = data.get(0).getTemperature();
        double lastTemp = data.get(data.size() - 1).getTemperature();
        double trend = lastTemp - firstTemp;

        String summary = generateSummary(trend, maxTemp, minTemp);
        String recommendation = generateRecommendation(trend, maxTemp, minTemp);

        return new WeatherAnalysis(trend, maxTemp, minTemp, summary, recommendation);
    }

    private String generateSummary(double trend, double maxTemp, double minTemp) {
        StringBuilder sb = new StringBuilder();

        if (trend > 2) sb.append("Strong warming");
        else if (trend > 0) sb.append("Slight warming");
        else if (trend < -2) sb.append("Strong cooling");
        else if (trend < 0) sb.append("Slight cooling");
        else sb.append("Temperature is stable");

        sb.append(String.format(". Range: %.1f°C to %.1f°C", minTemp, maxTemp));
        return sb.toString();
    }

    private String generateRecommendation(double trend, double maxTemp, double minTemp) {
        if (minTemp < 0) return "Dress warmly! Possible ice";
        if (maxTemp > 25) return "Hot! Don't forget water and headwear";
        if (trend < -3) return "Getting colder - take a jacket";
        if (trend > 3) return "Getting warmer - you can dress lighter";
        return "Weather is stable, dress for the season";
    }

    private String generateChart(List<String> times, List<Double> temps, String city) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        List<Date> xDates = times.stream()
                .map(t -> LocalDateTime.parse(t, formatter))
                .map(ldt -> Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()))
                .toList();

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(400)
                .title("Temperature in " + city)
                .xAxisTitle("Hour")
                .yAxisTitle("°C")
                .build();

        chart.addSeries("Temp", xDates, temps);

        BufferedImage image = BitmapEncoder.getBufferedImage(chart);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}