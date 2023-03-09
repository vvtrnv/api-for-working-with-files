package com.example.cfttask.service;

import com.example.cfttask.validator.Converter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.MessageDigest;
import java.util.Objects;

@Service
public class FilesService {

    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        multipartFile.transferTo(file);
        return file;
    }

    private void copyContent(File from, File to) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(from);

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

    public boolean putFileToPath(MultipartFile multipartFile, String path){
        File findFile = new File(path + '/' + multipartFile.getOriginalFilename());
        if (findFile.exists()) {
            throw new IllegalArgumentException("The file already exists");
        }
        if (!findFile.isFile()) {
            throw new IllegalArgumentException("The name of the file is repeated with name of the directory");
        }

        try {
            File uploadedFile = convertMultipartToFile(multipartFile);
            copyContent(uploadedFile, findFile);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Error when copy content");
        }

        return true;
    }

    public boolean updateFile(MultipartFile multipartFile, String path) {
        try {
            File updatedFile = convertMultipartToFile(multipartFile);
            File fileToUpdate = new File(path + '/' + updatedFile.getName());

            if (!fileToUpdate.exists() || !fileToUpdate.isFile()) {
                throw new IllegalArgumentException("Error when update file");
            }
            if (getFileChecksum(updatedFile).equals(getFileChecksum(fileToUpdate))) {
                return false;
            }

            copyContent(updatedFile, fileToUpdate);
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
