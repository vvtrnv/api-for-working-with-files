package com.example.cfttask.controller;


import com.example.cfttask.service.FilesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class ControllerFiles {
    private final String PATH = "/tmp";
    private final FilesService filesService;

    @GetMapping("/allfiles")
    public String getAllFilesFromPath() {
        return "PATH: \'" + PATH + '\'' + "\nFILENAME\tHASH\n" + filesService.getAllFileNamesAndHash(PATH);
    }

    @GetMapping("/downloadFile")
    public String getFileByName(@RequestParam("filename") String filename,
                                @RequestParam("clientpath") String clientPath) {
        return "filename: " + filename + "clientpath: " + clientPath;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handle(IllegalArgumentException err) {
        log.error(err.getMessage());
        return "NOT OK";
    }
}
