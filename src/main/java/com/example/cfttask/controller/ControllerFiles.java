package com.example.cfttask.controller;


import com.example.cfttask.service.FilesService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.attribute.standard.Media;
import java.io.File;
import java.io.IOException;


@Slf4j
@RestController
@AllArgsConstructor
public class ControllerFiles {
    private final String PATH = "/tmp";
    private final FilesService filesService;

    @GetMapping("/files/all")
    public ResponseEntity<String> getAllFilesFromPath() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(filesService.getAllFileNamesAndHash(PATH));
    }

    @GetMapping("/files")
    public ResponseEntity<Resource> getFileByName(@RequestParam(value = "filename") String filename) {
        Resource resource = new FileSystemResource(filesService.getFileByName(filename, PATH));
        MediaType mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }

    @PutMapping("/files")
    public ResponseEntity<String> putFileToServer(HttpServletRequest request) {
        String fileName = request.getHeader("fileName");
        long fileSize = Long.parseLong(request.getHeader("fileSize"));
        System.out.println(fileName + " " + fileSize);

        try {
            if (filesService.putFileToPath(request.getInputStream(), fileName, fileSize, PATH)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body("OK");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("NOT OK");
        }


        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("NOT OK");
    }

//    @PostMapping("/files")
//    public ResponseEntity<String> updateTheFile(@RequestParam(value="file") MultipartFile updatedFile) {
//        if (filesService.updateFile(updatedFile, PATH)) {
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body("OK");
//        }
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body("NOT OK");
//    }

    @DeleteMapping("/files")
    public ResponseEntity<String> deleteTheFile(@RequestParam(value = "filename") String filename) {
        if (filesService.deleteFile(filename, PATH)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body("OK");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("NOT OK");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handle(IllegalArgumentException err) {
        log.error(err.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("NOT OK");
    }
}
