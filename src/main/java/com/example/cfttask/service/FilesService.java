package com.example.cfttask.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Service
public class FilesService {

    private File createFileFromRequest(InputStream fileContent,
                                       String pathToFile) throws IOException {
        // Сохраняем файл на сервере
        FileOutputStream outputStream = new FileOutputStream(pathToFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fileContent.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);

        }
        outputStream.close();
        fileContent.close();

        return new File(pathToFile);
    }

    private void copyContent(File from, File to) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     * Calculate hash
     * @param file
     * @return The calculated hash of the file
     */
    private String getFileChecksum(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] hashBytes = md.digest(fileBytes);
            StringBuffer sb = new StringBuffer("");
            for (int i = 0; i < hashBytes.length; i++) {
                sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAllFileNamesAndHash(String path) {
        if (path == "") {
            throw new IllegalArgumentException("Path is empty");
        }

        File folder = new File(path);
        File[] files = folder.listFiles();

        StringBuilder result = new StringBuilder();

        for (File file : files) {
            if (file.isFile()) {
                result.append(getFileChecksum(file))
                        .append('\t')
                        .append(file.getName())
                        .append('\n');
            }
        }

        return result.toString();
    }

    public File getFileByName(String filename, String path) {
        File findFile = new File(path + '/' + filename);
        if (!findFile.exists()) {
            throw new IllegalArgumentException("The file does not exists");
        }
        if (!findFile.isFile()) {
            throw new IllegalArgumentException("The data you are looking for is not a file");
        }

        return findFile;
    }

    public boolean putFileToPath(InputStream contentFile, String filename, String path){
        String pathToFile = path + '/' + filename;
        System.out.println(pathToFile);
        File findFile = new File(pathToFile);
        if (findFile.exists()) {
            System.out.println("COMING TO EXISTS");
            throw new IllegalArgumentException("The file already exists");
        }

        try {
            File uploadedFile = createFileFromRequest(contentFile, pathToFile);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Error when copy content");
        }

        return true;
    }

    public boolean updateFile(InputStream contentFile, String filename, String path) {
        try {
            String pathToFile = path + '/' + filename;
            File fileToUpdate = new File(pathToFile);
            File updatedFile = createFileFromRequest(contentFile, path + "/tempcatalog/" + filename);

            if (!fileToUpdate.exists() || !fileToUpdate.isFile()) {
                throw new IllegalArgumentException("Error when update file");
            }
            System.out.println("OLD:\t" + getFileChecksum(fileToUpdate));
            System.out.println("UPDATED:\t" + getFileChecksum(updatedFile));
            if (Objects.equals(getFileChecksum(updatedFile), getFileChecksum(fileToUpdate))) {
                return false;
            }

            copyContent(updatedFile, fileToUpdate);
//            updatedFile.delete();
            return true;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Error when update the file");
        }

    }

    public boolean deleteFile(String filename, String path) {
        File toDeleteFile = new File(path + '/' + filename);

        if (!toDeleteFile.delete()) {
            throw new IllegalArgumentException("Error when delete file");
        }

        return true;
    }

}
