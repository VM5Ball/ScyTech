package ru.scytech.documentsearchsystembackend.model.filesystem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.MediaType;

@Getter
@AllArgsConstructor
public class DocData {
    private String docName;
    private String domain;
    private MediaType type;
    private byte[] data;
}
