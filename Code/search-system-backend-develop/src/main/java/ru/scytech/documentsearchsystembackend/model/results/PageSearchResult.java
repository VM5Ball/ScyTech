package ru.scytech.documentsearchsystembackend.model.results;

import lombok.Data;
import ru.scytech.documentsearchsystembackend.model.Highlight;
import ru.scytech.documentsearchsystembackend.model.QueryFlag;

import java.util.List;

@Data
public class PageSearchResult extends SearchResult {
    private int page;

    private List<Highlight> highlightList;

    public PageSearchResult(String domain, String docName, int page, float score, List<Highlight> highlightList,
                            QueryFlag queryFlag) {
        super(domain, docName, score, queryFlag);
        this.page = page;
        this.highlightList = highlightList;
    }
}
