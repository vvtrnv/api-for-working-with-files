import picocli.CommandLine;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;

@CommandLine.Command(
        name = "Client",
        description = "The client part for work with files on server"
)
public class Client implements Runnable{

    @CommandLine.Option(names = "--ip",
        description = "Server IP address")
    private String addressFromCMD;

    @CommandLine.Option(names = "--port",
            description = "Server PORT")
    private String portFromCMD;

    @CommandLine.Option(names = "--all",
        description = "List all files on server")
    private boolean isGetAllFilesFromServer;

    @CommandLine.Option(names = "--get",
        description = "Get file from server by filename. Required a -f and path to file on server.")
    private boolean isGetOneFileFromServer;

    @CommandLine.Option(names = "--put",
            description = "Put file to server. Required a -f and path to file on client.")
    private boolean isUploadFileToServer;

    @CommandLine.Option(names = "--update",
            description = "Update file on server. Required a -f and path to file on client.")
    private boolean isUpdateFileOnServer;

    @CommandLine.Option(names = "--delete",
            description = "Delete file on server. Required a -f and path to file on server.")
    private boolean isDeleteFileOnServer;

    @CommandLine.Option(names={"-f", "--file"},
        description = "Path to file and filename. Example: /home/user/test.txt")
    private String filenameFromCMD;

    @CommandLine.Option(names = {"-d", "--directory"},
        description = "Directory to download the file to. Only for --get.")
    private String pathToDownloadToClient;


    private final String ENDPOINT_URL = "http://%s:%d/files";
    private String endpointUrl;

    private boolean checkingIsFile(File file) {
        if (!file.isFile()) {
            return false;
        }
        return true;
    }

    private String getAllFiles(String endpointUrl) throws IOException {
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

    private void getFile(String endpointUrl) throws IOException{
        String fileUrl = endpointUrl + "?filename=" + filenameFromCMD;

        URL url = new URL(fileUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = con.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(pathToDownloadToClient + '/' + filenameFromCMD);
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

    private void uploadFile(String endpointUrl) throws IOException {
        String fileUrl = endpointUrl;
        URL url = new URL(fileUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setDoOutput(true);

        File file = new File(filenameFromCMD);

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

    private void updateFile(String endpointUrl) throws IOException {
        String fileUrl = endpointUrl;
        URL url = new URL(fileUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        File file = new File(filenameFromCMD);

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

    private void deleteFile(String endpointUrl) throws IOException {
        if (filenameFromCMD.contains("../")) {
            System.out.println("Параметр " + filenameFromCMD + " содержит \'../\', что не допустимо.");
            return;
        }

        String fileUrl = endpointUrl + "?filename=" + filenameFromCMD;
        URL url = new URL(fileUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("OK");
        } else {
            System.out.println("Не удалось удалить файл " + filenameFromCMD + ", код ответа: " + responseCode);
        }
        con.disconnect();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Client()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        try {
            // Load props and command line
            Properties props = new Properties();

            if (addressFromCMD != null && portFromCMD != null) {
                props.setProperty("address", addressFromCMD);
                props.setProperty("port", portFromCMD);
            } else {
                props.load(new FileInputStream("config.properties"));
            }

            // Forming url
            String address = props.getProperty("address");
            int port = Integer.parseInt(props.getProperty("port"));
            endpointUrl = String.format(ENDPOINT_URL, address, port);
            System.out.println(endpointUrl);

            if (isGetAllFilesFromServer) {
                System.out.println("Files on server:");
                System.out.println(getAllFiles(endpointUrl + "/all"));
            } else if (isGetOneFileFromServer && filenameFromCMD != null && pathToDownloadToClient != null) {
                getFile(endpointUrl);
            } else if (isUploadFileToServer && filenameFromCMD != null) {
                uploadFile(endpointUrl);
            } else if (isUpdateFileOnServer && filenameFromCMD != null) {
                updateFile(endpointUrl);
            } else if (isDeleteFileOnServer && filenameFromCMD != null) {
                deleteFile(endpointUrl);
            }
        } catch (IOException ex) {
            System.out.println("Error on client");
            ex.printStackTrace();
        }
    }
}
