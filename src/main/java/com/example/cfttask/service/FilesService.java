package com.example.cfttask.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.security.MessageDigest;

@Service
public class FilesService {

    /**
     * Calculate hash
     * @param file
     * @return The calculated hash of the file
     */
    private String getFileChecksum(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            fis.close();
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
                result.append(file.getName())
                        .append('\t')
                        .append(getFileChecksum(file))
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

    public File putFileToPath(File newFile, String path) {
        File findFile = new File(path + '/' + newFile.getName());
        if (findFile.exists()) {
            throw new IllegalArgumentException("The file already exists");
        }
        if (!findFile.isFile()) {
            throw new IllegalArgumentException("The received data is not a file)");
        }
        return findFile;
    }

    public void updateFile(File updatedFile, String path) {
        File fileToUpdate = new File(path + '/' + updatedFile.getName());
        if (!updatedFile.exists() || !updatedFile.isFile()) {
            // #TODO throw exception. file not found
        }
        if (getFileChecksum(updatedFile).equals(getFileChecksum(fileToUpdate))) {
            // #TODO throw exception. file without changes
        }

        try {
            FileInputStream inputStream = new FileInputStream(updatedFile);
            FileOutputStream outputStream = new FileOutputStream(fileToUpdate);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
