package com.example.cfttask.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

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
        System.out.println("createFileFromRequest\t OKOKOK");
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

    public boolean putFileToPath(InputStream contentFile, String filename, long sizeOfContent, String path){
        String pathToFile = path + '/' + filename;
        System.out.println(pathToFile);
        File findFile = new File(pathToFile);
        if (findFile.exists()) {
            System.out.println("COMING TO EXISTS");
            throw new IllegalArgumentException("The file already exists");
        }
//        if (!findFile.isFile()) {
//            System.out.println("COMING TO NOT FILE");
//            throw new IllegalArgumentException("The name of the file is repeated with name of the directory");
//        }

        try {
            File uploadedFile = createFileFromRequest(contentFile, pathToFile);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Error when copy content");
        }

        return true;
    }

//    public boolean updateFile(MultipartFile multipartFile, String path) {
//        try {
//            File updatedFile = convertMultipartToFile(multipartFile);
//            File fileToUpdate = new File(path + '/' + updatedFile.getName());
//
//            if (!fileToUpdate.exists() || !fileToUpdate.isFile()) {
//                throw new IllegalArgumentException("Error when update file");
//            }
//            if (getFileChecksum(updatedFile).equals(getFileChecksum(fileToUpdate))) {
//                return false;
//            }
//
//            copyContent(updatedFile, fileToUpdate);
//            return true;
//        } catch (IOException ex) {
//            throw new IllegalArgumentException("Error when update the file");
//        }
//
//    }

    public boolean deleteFile(String filename, String path) {
        File toDeleteFile = new File(path + '/' + filename);

        if (!toDeleteFile.delete()) {
            throw new IllegalArgumentException("Error when delete file");
        }

        return true;
    }

}
