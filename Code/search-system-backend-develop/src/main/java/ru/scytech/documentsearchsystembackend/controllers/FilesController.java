package ru.scytech.documentsearchsystembackend.controllers;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.scytech.documentsearchsystembackend.services.DefaultDocumentAccessService;
import ru.scytech.documentsearchsystembackend.services.DefaultSearchService;

import javax.naming.OperationNotSupportedException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FilesController {
    private DefaultSearchService defaultSearchService;
    private DefaultDocumentAccessService defaultFileSystemService;

    public FilesController(DefaultSearchService defaultSearchService, DefaultDocumentAccessService defaultFileSystemService) {
        this.defaultSearchService = defaultSearchService;
        this.defaultFileSystemService = defaultFileSystemService;
    }

    @GetMapping("/download/{domain}/{filename}")
    public ResponseEntity<Resource> downloadDoc(@PathVariable String domain,
                                                @PathVariable String filename) throws IOException {

        var fileModel = defaultSearchService.loadDoc(domain, filename);
        byte[] bytes = fileModel.getData();
        InputStreamResource inputStreamResource = new InputStreamResource(
                new ByteArrayInputStream(bytes));
        return ResponseEntity
                .ok()
                .contentLength(bytes.length)
                .contentType(fileModel.getType())
                .body(inputStreamResource);
    }

    @PostMapping("/upload/{domain}/{filename}")
    public ResponseEntity uploadDoc(@PathVariable String domain,
                                    @PathVariable String filename,
                                    @RequestParam("file") MultipartFile file,
                                    @RequestParam("tags") List<String> tags) throws IOException {
        InputStream fileInputStream = file.getInputStream();
        try {
            defaultSearchService.indexDoc(filename, domain, tags, fileInputStream);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/update/{domain}/{filename}")
    public ResponseEntity updateDoc(@PathVariable String domain,
                                    @PathVariable String filename,
                                    @RequestParam("file") MultipartFile file,
                                    @RequestParam("tags") List<String> tags) throws IOException {
        InputStream fileInputStream = file.getInputStream();
        try {
            defaultSearchService.updateDoc(filename, domain, tags, fileInputStream);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @GetMapping(value = "/download/title/{domain}/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] getTitleImage(@PathVariable String domain,
                         @PathVariable String filename) throws IOException, OperationNotSupportedException {
        return defaultSearchService.getTitleImage(domain, filename);
    }

    @GetMapping("/domains")
    public List<String> getDomains() throws IOException {
        return defaultFileSystemService.loadDocsRepository(false).keySet().stream().collect(Collectors.toList());
    }
}
