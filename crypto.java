#server side
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CryptoPriceServer {
    private static final int PORT = 8888;
    private static final String FILE_NAME = "crypto_data.txt";
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Map<String, Double> cryptoPrices = new HashMap<>();

    public static void main(String[] args) {
        loadCryptoPricesFromFile();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Crypto Price Server is running...");
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
                    String[] parts = request.split(":");
                    if (parts.length == 2) {
                        String command = parts[0];
                        String cryptoName = parts[1];
                        if (command.equals("GET_PRICE")) {
                            Double price = getCryptoPrice(cryptoName);
                            writer.println(cryptoName + ":" + price);
                        } else if (command.equals("UPDATE_PRICE")) {
                            String updateResult = updateCryptoPrice(cryptoName);
                            writer.println(updateResult);
                        }
                    } else if (request.equals("QUIT")) {
                        break;
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

        private Double getCryptoPrice(String cryptoName) {
            lock.readLock().lock();
            try {
                return cryptoPrices.getOrDefault(cryptoName, -1.0); // Return -1 if cryptoName not found
            } finally {
                lock.readLock().unlock();
            }
        }

        private String updateCryptoPrice(String updateInfo) {
            String[] parts = updateInfo.split(":");
            if (parts.length != 2) {
                return "UPDATE_FAILED:Invalid request format";
            }
            String cryptoName = parts[0];
            double newPrice;
            try {
                newPrice = Double.parseDouble(parts[1]);
            } catch (NumberFormatException e) {
                return "UPDATE_FAILED:Invalid price format";
            }

            lock.writeLock().lock();
            try {
                cryptoPrices.put(cryptoName, newPrice);
                saveCryptoPricesToFile();
                return "UPDATE_SUCCESS:" + cryptoName;
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    private static void loadCryptoPricesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String cryptoName = parts[0].trim();
                    double price = Double.parseDouble(parts[1].substring(1)); // Remove '$' sign
                    cryptoPrices.put(cryptoName, price);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading crypto prices from file: " + e.getMessage());
        }
    }

    private static void saveCryptoPricesToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, Double> entry : cryptoPrices.entrySet()) {
                writer.write(entry.getKey() + ":$" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving crypto prices to file: " + e.getMessage());
        }
    }
}


      #client side

      import java.io.*;
import java.net.*;

public class CryptoPriceClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 8888;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to Crypto Price Server.");

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


        #input file:
        import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadFile {
    public static void main(String[] args) {
        String fileName = "input.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
