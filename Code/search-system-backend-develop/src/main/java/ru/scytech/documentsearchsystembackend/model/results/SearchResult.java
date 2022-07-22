package ru.scytech.documentsearchsystembackend.model.results;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.scytech.documentsearchsystembackend.model.QueryFlag;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {
    protected String domain;
    protected String docName;
    protected float score;
    protected QueryFlag queryFlag;
}
