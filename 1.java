
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TrafficDataServer {
    private static final int PORT = 8888;
    private static final String FILE_NAME = "traffic_data.txt";
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Map<String, String> trafficData = new HashMap<>();

    public static void main(String[] args) {
        loadTrafficDataFromFile();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Traffic Data Server is running...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String request;
                while ((request = reader.readLine()) != null) {
                    String[] parts = request.split(":", 2);
                    if (parts.length == 2) {
                        String command = parts[0];
                        String route = parts[1];
                        if (command.equals("GET_ROUTE")) {
                            String trafficStatus = getTrafficStatus(route);
                            writer.println(trafficStatus);
                        } else if (command.equals("UPDATE_ROUTE")) {
                            String updateResult = updateTrafficStatus(route);
                            writer.println(updateResult);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client request: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String getTrafficStatus(String route) {
            lock.readLock().lock();
            try {
                return trafficData.getOrDefault(route, "Route not found");
            } finally {
                lock.readLock().unlock();
            }
        }

        private String updateTrafficStatus(String route) {
            String[] parts = route.split(":", 2);
            if (parts.length != 2) {
                return "UPDATE_FAILED:Invalid request format";
            }
            String routeName = parts[0];
            String newStatus = parts[1];
            lock.writeLock().lock();
            try {
                trafficData.put(routeName, newStatus);
                saveTrafficDataToFile();
                return "UPDATE_SUCCESS:" + routeName;
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    private static void loadTrafficDataFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    trafficData.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading traffic data from file: " + e.getMessage());
        }
    }

    private static void saveTrafficDataToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, String> entry : trafficData.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving traffic data to file: " + e.getMessage());
        }
    }
}




import java.io.*;
import java.net.*;

public class TrafficInfoClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 8888;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to Traffic Data Server.");
            String userInput;
            while ((userInput = reader.readLine()) != null) {
                writer.println(userInput);
                if (userInput.equalsIgnoreCase("QUIT")) {
                    break;
                }
                String response = serverReader.readLine();
                System.out.println("Server response: " + response);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}


