package Main;

import com.sun.net.httpserver.HttpServer;
import Controllers.WeatherController;
import Repositories.WeatherCacheRepository;
import Services.*;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        WeatherCacheRepository cache = new WeatherCacheRepository();
        WeatherApiClient api = new WeatherApiClient();
        WeatherService service = new WeatherService(cache, api);
        WeatherController controller = new WeatherController(service);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/weather", controller::handle);
        server.start();

        System.out.println("Server started on http://localhost:8080/weather?city=Moscow");
    }
}

