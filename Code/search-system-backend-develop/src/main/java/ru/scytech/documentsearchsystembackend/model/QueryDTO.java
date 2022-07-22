package ru.scytech.documentsearchsystembackend.model;

import lombok.Data;

import java.util.Set;

@Data
public class QueryDTO {
    private String query;
    private QueryFlag queryFlag;
    private Set<String> domains;
    private Set<String> files;
}
