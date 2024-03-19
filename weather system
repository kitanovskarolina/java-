#server class
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class LocalWeatherInfo {

    private static final int PORT = 12345;
    private static final String DATA_FILE_PATH = "weather_data.txt";
    private static final String END_OF_MESSAGE = "__END_OF_MESSAGE__";

    private static Map<String, String> weatherData = new HashMap<>();

    public static void main(String[] args) {
        loadWeatherData();

        WeatherDataServer server = new WeatherDataServer();
        server.start();
    }

    private static void loadWeatherData() {
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    weatherData.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class WeatherDataServer extends Thread {
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server is running...");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandler.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String cityName;
                while ((cityName = reader.readLine()) != null) {
                    if (cityName.equals("EXIT")) {
                        break;
                    }
                    String weatherInfo = weatherData.getOrDefault(cityName, "Weather information not available.");
                    writer.println(weatherInfo + " " + END_OF_MESSAGE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


#client class
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WeatherInfoClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private static final String END_OF_MESSAGE = "__END_OF_MESSAGE__";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to the server.");

            String city;
            while (true) {
                System.out.print("Enter the city name (or 'EXIT' to quit): ");
                city = userInput.readLine();
                if (city.equalsIgnoreCase("EXIT")) {
                    writer.println("EXIT");
                    break;
                }
                writer.println(city);
                String response;
                while ((response = serverInput.readLine()) != null) {
                    if (response.equals(END_OF_MESSAGE)) {
                        break;
                    }
                    System.out.println("Weather information: " + response);
                }
            }
            System.out.println("Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
