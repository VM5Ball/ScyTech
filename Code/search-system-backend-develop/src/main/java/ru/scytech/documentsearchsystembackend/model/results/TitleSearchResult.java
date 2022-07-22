package ru.scytech.documentsearchsystembackend.model.results;

import lombok.Data;
import ru.scytech.documentsearchsystembackend.model.QueryFlag;

@Data
public class TitleSearchResult extends SearchResult {
    private String title;

    public TitleSearchResult(String domain, String docName, String title, float score, QueryFlag queryFlag) {
        super(domain, docName, score, queryFlag);
        this.title = title;
    }
}
