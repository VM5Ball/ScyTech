package ru.scytech.documentsearchsystembackend.dao;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import ru.scytech.documentsearchsystembackend.model.QueryFlag;
import ru.scytech.documentsearchsystembackend.model.elastic.ElasticDoc;
import ru.scytech.documentsearchsystembackend.model.results.PhraseSuggestResult;
import ru.scytech.documentsearchsystembackend.model.results.TitleSearchResult;
import ru.scytech.documentsearchsystembackend.services.ElasticRestClient;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class DocIndexDao {
    private ElasticRestClient elasticRestClient;
    private final String DOCS_INDEX;
    private final String DOCNAME_FIELD;
    private final String DOMAIN_FIELD;
    private final String TITLE_FIELD;
    private final String KEYWORDS_FIELD;
    private final String HASH_FIELD;
    private final String PHRASE_SUGGESTER;

    public DocIndexDao(ElasticRestClient elasticRestClient,
                       String docsIndex,
                       String docNameField,
                       String domainField,
                       String titleField,
                       String keywordsField,
                       String hashField,
                       String phraseSuggester) {
        this.elasticRestClient = elasticRestClient;
        DOCS_INDEX = docsIndex;
        DOCNAME_FIELD = docNameField;
        DOMAIN_FIELD = domainField;
        TITLE_FIELD = titleField;
        KEYWORDS_FIELD = keywordsField;
        HASH_FIELD = hashField;
        PHRASE_SUGGESTER = phraseSuggester;
    }

    public void deleteAllDocs() throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest();
        deleteByQueryRequest.indices(DOCS_INDEX);
        elasticRestClient.deleteByQuery(deleteByQueryRequest);
    }

    public void deleteDoc(String domain, String docName) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest();
        deleteByQueryRequest.indices(DOCS_INDEX);
        deleteByQueryRequest.setQuery(QueryBuilders
                .boolQuery()
                .must(QueryBuilders.matchQuery(DOMAIN_FIELD, domain))
                .must(QueryBuilders.matchQuery(DOCNAME_FIELD, docName)));
        elasticRestClient.deleteByQuery(deleteByQueryRequest);
    }

    public Optional<String> indexDoc(ElasticDoc doc) throws IOException {
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.index(DOCS_INDEX);
        indexRequest.id(UUID.randomUUID().toString());
        String keywords = Arrays.stream(doc.getKeywords()).reduce("", (a, b) -> a + ";" + b);
        indexRequest.source(
                DOCNAME_FIELD, doc.getDocName(),
                DOMAIN_FIELD, doc.getDomain(),
                TITLE_FIELD, doc.getTitle(),
                HASH_FIELD, doc.getHash(),
                KEYWORDS_FIELD, keywords);
        var response = elasticRestClient.index(indexRequest);
        if (response == null) {
            return Optional.empty();
        }
        return Optional.of(response.getId());
    }

    public List<ElasticDoc> getAllDocs() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(DOCS_INDEX);
        SearchResponse response = elasticRestClient.search(searchRequest);
        List<ElasticDoc> result = new ArrayList<>();
        if (response == null) {
            return result;
        }
        for (var hit : response.getHits()) {
            String docName = hit.getSourceAsMap().get(DOCNAME_FIELD).toString();
            String domain = hit.getSourceAsMap().get(DOMAIN_FIELD).toString();
            String title = hit.getSourceAsMap().get(TITLE_FIELD).toString();
            String keywordsRaw = hit.getSourceAsMap().get(KEYWORDS_FIELD).toString();
            String hash = hit.getSourceAsMap().get(HASH_FIELD).toString();
            String[] keywords = keywordsRaw.split(";");
            result.add(new ElasticDoc(docName, domain, title, hash, keywords));
        }
        return result;
    }

    public Optional<ElasticDoc> getDoc(String domain, String documentName) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(DOCS_INDEX);
        SearchSourceBuilder sourceBuilder =
                new SearchSourceBuilder().query(
                        QueryBuilders
                                .boolQuery()
                                .must(QueryBuilders.matchQuery(DOMAIN_FIELD, domain).fuzziness(0))
                                .must(QueryBuilders.matchQuery(DOCNAME_FIELD, documentName).fuzziness(0)));
        searchRequest.source(sourceBuilder);
        SearchResponse response = elasticRestClient.search(searchRequest);
        List<ElasticDoc> result = new ArrayList<>();
        if (response == null) {
            return Optional.empty();
        }
        for (var hit : response.getHits()) {
            String title = hit.getSourceAsMap().get(TITLE_FIELD).toString();
            String keywordsRaw = hit.getSourceAsMap().get(KEYWORDS_FIELD).toString();
            String hash = hit.getSourceAsMap().get(HASH_FIELD).toString();
            String[] keywords = keywordsRaw.split(";");
            result.add(new ElasticDoc(documentName, domain, title, hash, keywords));
        }
        if (result.size() > 1) {
            throw new Error(
                    String.format("index files have several subscriptions with domain and name: %s %s",
                            domain, documentName));
        } else if (result.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    public List<PhraseSuggestResult> suggestTitle(String phrase) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(DOCS_INDEX);
        PhraseSuggestionBuilder phraseSuggestionBuilder = new PhraseSuggestionBuilder(TITLE_FIELD);
        phraseSuggestionBuilder.text(phrase);
        phraseSuggestionBuilder.size(10);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion(PHRASE_SUGGESTER, phraseSuggestionBuilder);
        SearchSourceBuilder sourceBuilder =
                new SearchSourceBuilder().suggest(suggestBuilder);
        searchRequest.source(sourceBuilder);
        SearchResponse response = elasticRestClient.search(searchRequest);
        if (response != null) {
            return response
                    .getSuggest()
                    .getSuggestion(PHRASE_SUGGESTER)
                    .getEntries()
                    .stream()
                    .flatMap(it ->
                            it.getOptions().stream())
                    .map(it -> new PhraseSuggestResult(it.getText().toString(), it.getScore()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public List<TitleSearchResult> searchTitle(String titleQuery) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(DOCS_INDEX);
        SearchSourceBuilder sourceBuilder =
                new SearchSourceBuilder().query(QueryBuilders
                        .matchPhraseQuery(TITLE_FIELD, titleQuery));
        searchRequest.source(sourceBuilder);
        SearchResponse response = elasticRestClient.search(searchRequest);
        List<TitleSearchResult> result = new ArrayList<>();
        if (response == null) {
            return result;
        }
        for (var hit : response.getHits()) {
            String docName = hit.getSourceAsMap().get(DOCNAME_FIELD).toString();
            String domain = hit.getSourceAsMap().get(DOMAIN_FIELD).toString();
            String title = hit.getSourceAsMap().get(TITLE_FIELD).toString();
            result.add(new TitleSearchResult(domain, docName, title, hit.getScore(), QueryFlag.TITLE));
        }
        return result;
    }
}
