package com.example.cfttask.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class FilesService {

    private File createFileFromRequest(InputStream fileContent,
                                       String pathToFile) throws IOException {
        // Save file on server
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

    private String getHashOfArrBytes(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(content);
            StringBuffer sb = new StringBuffer("");
            for (int i = 0; i < hashBytes.length; i++) {
                sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
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

        try {
            for (File file : files) {
                if (file.isFile()) {
                    result.append(getHashOfArrBytes(Files.readAllBytes(file.toPath())))
                            .append('\t')
                            .append(file.getName())
                            .append('\n');
                }
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

            if (!fileToUpdate.exists() || !fileToUpdate.isFile()) {
                throw new IllegalArgumentException("Error when update file");
            }
            byte[] input = contentFile.readAllBytes();

            if (getHashOfArrBytes(Files.readAllBytes(fileToUpdate.toPath()))
                    .equals(getHashOfArrBytes(input))) {
                return false;
            }
            Files.write(Path.of(pathToFile), input);

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
