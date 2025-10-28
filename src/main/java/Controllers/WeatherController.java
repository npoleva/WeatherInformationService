package Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import Services.WeatherService;
import Models.WeatherResponseDto;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class WeatherController {
    private final WeatherService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public WeatherController(WeatherService service) {
        System.out.println("In constructor!!!");
        this.service = service;
    }

    public void handle(HttpExchange exchange) {
        try {
            URI uri = exchange.getRequestURI();
            System.out.println("Before getting query!!!");
            String query = uri.getQuery();
            if (query == null || !query.contains("city=")) {
                send(exchange, 400, "Missing ?city= parameter");
                return;
            }
            String city = query.split("=")[1];

            System.out.println("Before calling service!!!");

            WeatherResponseDto dto = service.getWeather(city);

            System.out.println("DTO: " + dto);

            String html = "<html><head><meta charset='utf-8'></head><body>" +
                    "<h2>Погода в " + city + "</h2>" +
                    "<img src='data:image/png;base64," + dto.getChartBase64() + "'><br><br>" +
                    "<pre>" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto) + "</pre>" +
                    "</body></html>";

            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            send(exchange, 500, "Ошибка: " + e.getMessage());
        }
    }

    private void send(HttpExchange ex, int code, String msg) {
        try {
            byte[] b = msg.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(code, b.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(b); }
        } catch (Exception ignored) {}
    }
}
