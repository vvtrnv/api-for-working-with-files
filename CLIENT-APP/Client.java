import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Scanner;

public class Client {
    private static final String ENDPOINT_URL = "http://%s:%d/files";
    private static final String FILE_URL_FORMAT = "http://%s:%d/files/%s";

    private static boolean checkingIsFile(File file) {
        if (!file.isFile()) {
            return false;
        }
        return true;
    }

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
                uploadFile(endpointUrl, fileName);
                break;
            case 4:
                System.out.println("Введите имя файла:");
                fileName = scanner.nextLine();
                updateFile(endpointUrl, fileName);
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

    private static String getAllFiles(String endpointUrl) throws IOException {
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
        connection.disconnect();
        return sb.toString();
    }

    private static void getFile(String endpointUrl,
                                String fileName,
                                String path) throws IOException{
        String fileUrl = endpointUrl + "?filename=" + fileName;

        URL url = new URL(fileUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = con.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(path + '/' + fileName);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);

            }
            outputStream.close();
            inputStream.close();
            System.out.println("OK");
        } else {
            System.out.println("Не удалось получить файл. Код ошибки: " + responseCode);
        }
        con.disconnect();
    }

    private static void uploadFile(String endpointUrl,
                                   String pathToFile) throws IOException {
        String fileUrl = endpointUrl;
        URL url = new URL(fileUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setDoOutput(true);

        File file = new File(pathToFile);
        System.out.println(file.getName());
        if (!checkingIsFile(file)) {
            System.out.println("Проверьте корректность. Указанного файла не существует");
            return;
        }

        byte[] fileContent = Files.readAllBytes(file.toPath());

        con.setRequestProperty("Content-Type", "application/octet-stream");
        con.setRequestProperty("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        con.setRequestProperty("fileName", file.getName() );
        con.setRequestProperty("fileSize", String.valueOf(fileContent.length));

        OutputStream outputStream = con.getOutputStream();
        outputStream.write(fileContent);
        outputStream.flush();
        outputStream.close();

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("OK");
        } else {
            System.out.println("Не удалось записать файл на сервер. Код ошибки: " + responseCode);
        }
        con.disconnect();
    }


    private static void updateFile(String endpointUrl,
                                   String pathToFile) throws IOException {
        String fileUrl = endpointUrl;
        URL url = new URL(fileUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        File file = new File(pathToFile);

        if (!checkingIsFile(file)) {
            System.out.println("Проверьте корректность. Указанного файла не существует");
            return;
        }

        byte[] fileContent = Files.readAllBytes(file.toPath());

        con.setRequestProperty("Content-Type", "application/octet-stream");
        con.setRequestProperty("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        con.setRequestProperty("fileName", file.getName());
        con.setRequestProperty("fileSize", String.valueOf(fileContent.length));

        OutputStream outputStream = con.getOutputStream();
        outputStream.write(fileContent);
        outputStream.flush();
        outputStream.close();

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("OK");
        } else if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
            System.out.println("Не требуется изменений");
        }
        else {
            System.out.println("Не удалось обновить файл на сервере. Код ошибки: " + responseCode);
        }

    }

    private static void deleteFile(String endpointUrl,
                                   String fileName) throws IOException {
        if (fileName.contains("../")) {
            System.out.println("Параметр " + fileName + " содержит \'../\', что не допустимо.");
            return;
        }

        String fileUrl = endpointUrl + "?filename=" + fileName;
        URL url = new URL(fileUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("OK");
        } else {
            System.out.println("Не удалось удалить файл " + fileName + ", код ответа: " + responseCode);
        }
        con.disconnect();
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
            System.out.println(endpointUrl);
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
