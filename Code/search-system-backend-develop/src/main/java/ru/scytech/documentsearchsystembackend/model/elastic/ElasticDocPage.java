package ru.scytech.documentsearchsystembackend.model.elastic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ElasticDocPage {
    private String id;
    private String docName;
    private String domain;
    private long page;
    private String content;
}
