package ru.scytech.documentsearchsystembackend.model.elastic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ElasticDoc {
    private String docName;
    private String domain;
    private String title;
    private String hash;
    private String[] keywords;
}
