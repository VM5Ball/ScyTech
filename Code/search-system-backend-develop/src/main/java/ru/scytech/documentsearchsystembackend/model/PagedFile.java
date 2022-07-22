package ru.scytech.documentsearchsystembackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.scytech.documentsearchsystembackend.model.filesystem.PageData;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedFile {
    private String fileName;
    private String domain;
    private String title;
    private List<PageData> pages;
}
