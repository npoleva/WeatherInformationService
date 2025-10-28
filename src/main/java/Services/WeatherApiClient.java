package Services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import Entities.*;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class WeatherApiClient {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public Coordinates getCoordinates(String city) throws Exception {
        String url = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                URLEncoder.encode(city, StandardCharsets.UTF_8);
        Request req = new Request.Builder().url(url).build();
        try (Response r = client.newCall(req).execute()) {
            var node = mapper.readTree(r.body().string());
            var results = node.get("results");
            System.out.println("Results:" + results);
            if (results == null || results.isEmpty()) throw new Exception("City not found");
            var first = results.get(0);
            System.out.println("Latitude:" + first.get("latitude").asDouble());
            return new Coordinates(first.get("latitude").asDouble(), first.get("longitude").asDouble());
        }
    }

    public WeatherData getWeather(double lat, double lon) throws Exception {
        System.out.println("Before request to weather in client!!!");

        System.out.println("Latitude:" + lat);
        System.out.println("Longitude:" + lon);


        String url = String.format(
                java.util.Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.2f&longitude=%.2f&hourly=temperature_2m",
                lat, lon
        );
        Request req = new Request.Builder().url(url).build();
        try (Response r = client.newCall(req).execute()) {
            System.out.println("After request to weather in client!!!");
            System.out.println("URL: " + url);
            var node = mapper.readTree(r.body().string());
            System.out.println("Node: " + node);
            var hourly = node.get("hourly");
            List times = mapper.convertValue(hourly.get("time"), List.class);
            List<Double> temps = mapper.convertValue(hourly.get("temperature_2m"), List.class);
            return new WeatherData(times, temps);
        }
    }
}
