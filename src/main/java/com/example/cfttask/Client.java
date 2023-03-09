package com.example.cfttask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

public class Client {

    private static final String ENDPOINT_URL = "http://%s:%d/files";
    private static final String FILE_URL_FORMAT = "http://%s:%d/files/%s";

    public static void printMenu() {
        System.out.println("Выберите действие:");
        System.out.println("1. Получить список файлов");
        System.out.println("2. Получить файл с сервера");
        System.out.println("3. Записать на сервер файл");
        System.out.println("4. Обновить на сервере файл");
        System.out.println("5. Удалить на сервере файл");
        System.out.println("0. Выход");
    }

    public static void choiceFromMenu(int action, Scanner scanner, String endpointUrl) throws IOException {
        switch (action) {
            case 1:
                System.out.println(getAllFiles(endpointUrl + "/all"));
                break;
            case 2:
                System.out.println("Введите имя файла:");
                String fileName = scanner.nextLine();
                System.out.println("Введите путь для сохранения файла:");
                String path = scanner.nextLine();
                getFile(endpointUrl, fileName, path);
                break;
            case 3:
                System.out.println("Введите имя файла:");
                fileName = scanner.nextLine();
                System.out.println("Введите путь к файлу на клиенте:");
                String filePath = scanner.nextLine();
                uploadFile(endpointUrl, fileName, filePath);
                break;
            case 4:
                System.out.println("Введите имя файла:");
                fileName = scanner.nextLine();
                System.out.println("Введите путь к файлу на клиенте:");
                filePath = scanner.nextLine();
                updateFile(endpointUrl, fileName, filePath);
                break;
            case 5:
                System.out.println("Введите имя файла:");
                fileName = scanner.nextLine();
                deleteFile(endpointUrl, fileName);
                break;
            case 0:
                System.out.println("До связи!");
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор действия.");
        }
    }

    public static String getAllFiles(String endpointUrl) throws IOException {
        URL url = new URL(endpointUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();

        StringBuilder sb = new StringBuilder();
        sb.append("HASH\t/\tFILENAME").append('\n');

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine).append('\n');
            }
            in.close();
        }

        return sb.toString();
    }

    private static void getFile(String endpointUrl,
                                String fileName,
                                String path) throws IOException{
        String fileUrl = String.format(FILE_URL_FORMAT, endpointUrl, fileName);
        URL url = new URL(fileUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = con.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(path);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            System.out.println("Файл успешно записан на клиент.");
        } else {
            System.out.println("Не удалось получить файл. Код ошибки: " + responseCode);
        }
    }

    private static void uploadFile(String endpointUrl,
                                   String fileName,
                                   String filePath) throws IOException {
        String fileUrl = String.format(FILE_URL_FORMAT, endpointUrl, fileName);
        URL url = new URL(fileUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setDoOutput(true);

        OutputStream outputStream = con.getOutputStream();
        FileInputStream inputStream = new FileInputStream(filePath);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Файл успешно записан на сервер.");
        } else {
            System.out.println("Не удалось записать файл на сервер. Код ошибки: " + responseCode);
        }
    }

    private static void updateFile(String endpointUrl,
                                   String fileName,
                                   String filePath) throws IOException {
        String fileUrl = String.format(FILE_URL_FORMAT, endpointUrl, fileName);
        URL url = new URL(fileUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        OutputStream outputStream = con.getOutputStream();
        FileInputStream inputStream = new FileInputStream(filePath);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Файл успешно обновлен на сервере.");
        } else {
            System.out.println("Не удалось обновить файл на сервере. Код ошибки: " + responseCode);
        }

    }

    private static void deleteFile(String endpointUrl,
                                   String fileName) throws IOException {

    }

    public static void main(String[] args) {
        Properties props = new Properties();
        try {
            // Load props and command line
            if (args.length > 0) {
                for (String arg : args) {
                    String[] keyValue = arg.split("=");
                    if (keyValue.length == 2) {
                        props.setProperty("address", keyValue[0]);
                        props.setProperty("port", keyValue[1]);
                    }
                }
            } else {
                props.load(new FileInputStream("config.properties"));
            }

            Scanner scanner = new Scanner(System.in);

            // Forming url
            String address = props.getProperty("address");
            int port = Integer.parseInt(props.getProperty("port"));
            String endpointUrl = String.format(ENDPOINT_URL, address, port);

            while (true) {
                printMenu();

                int action = scanner.nextInt();
                scanner.nextLine();

                choiceFromMenu(action, scanner, endpointUrl);
            }

        } catch (IOException e) {
            System.out.println("Error on client");
            e.printStackTrace();
        }
    }

}
