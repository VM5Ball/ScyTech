package ru.scytech.documentsearchsystembackend.model.filesystem;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.MediaType;

import java.io.InputStream;

@Data
@AllArgsConstructor
public class PageData {
    private String docName;
    private String domain;
    private int page;
    private byte[] content;
    private MediaType type;
}
