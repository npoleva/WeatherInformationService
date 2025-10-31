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
                    "HIT"
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

        WeatherResponseDto dto = new WeatherResponseDto(city, list, chartBase64, "MISS");

        cache.save(key, mapper.writeValueAsString(dto));
        return dto;
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