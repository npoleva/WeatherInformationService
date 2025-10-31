package Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import Services.WeatherService;
import Models.WeatherResponseDto;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WeatherController {
    private final WeatherService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public WeatherController(WeatherService service) {
        this.service = service;
    }

    public void handle(HttpExchange exchange) {
        try {
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/") || path.equals("/static/index.html")) {
                serveStaticFile(exchange, "src/main/resources/static/index.html");
                return;
            }

            if (path.equals("/weather")) {
                handleWeatherApi(exchange);
                return;
            }

            send(exchange, 404, "Not Found");

        } catch (Exception e) {
            send(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleWeatherApi(HttpExchange exchange) throws Exception {
        URI uri = exchange.getRequestURI();
        String query = uri.getQuery();

        if (query == null || !query.contains("city=")) {
            sendJson(exchange, 400, "{\"error\": \"Missing ?city= parameter\"}");
            return;
        }

        String city = query.split("=")[1];
        WeatherResponseDto dto = service.getWeather(city);

        String jsonResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
        sendJson(exchange, 200, jsonResponse);
    }

    private void serveStaticFile(HttpExchange exchange, String filePath) throws Exception {
        System.out.println("=== DEBUG: Serving static file ===");
        System.out.println("Requested path: " + filePath);

        Path path = Paths.get(filePath);
        System.out.println("Absolute path: " + path.toAbsolutePath());
        System.out.println("File exists: " + Files.exists(path));

        if (!Files.exists(path)) {
            System.out.println("ERROR: File not found!");
            String errorHtml = "<html><body><h1>Test Page</h1><p>File not found, but server works</p></body></html>";
            byte[] bytes = errorHtml.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            return;
        }

        try {
            byte[] fileBytes = Files.readAllBytes(path);
            System.out.println("File size: " + fileBytes.length + " bytes");

            String contentPreview = new String(fileBytes, StandardCharsets.UTF_8);
            System.out.println("Content preview: " + contentPreview.substring(0, Math.min(200, contentPreview.length())));

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, fileBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
            System.out.println("File served successfully");

        } catch (Exception e) {
            System.out.println("ERROR reading file: " + e.getMessage());
            e.printStackTrace();
            send(exchange, 500, "Error reading file");
        }
    }

    private void sendJson(HttpExchange ex, int code, String json) throws Exception {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
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