package ru.scytech.documentsearchsystembackend.dao;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import ru.scytech.documentsearchsystembackend.model.Highlight;
import ru.scytech.documentsearchsystembackend.model.QueryFlag;
import ru.scytech.documentsearchsystembackend.model.TaggedSubstring;
import ru.scytech.documentsearchsystembackend.model.elastic.ElasticDocPage;
import ru.scytech.documentsearchsystembackend.model.filesystem.PageData;
import ru.scytech.documentsearchsystembackend.model.results.PageSearchResult;
import ru.scytech.documentsearchsystembackend.model.results.PhraseSuggestResult;
import ru.scytech.documentsearchsystembackend.services.ElasticRestClient;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ru.scytech.documentsearchsystembackend.utils.EncodeUtils.toBase64;

public class PageIndexDao {
    private ElasticRestClient elasticRestClient;
    private HighlightBuilder highlightBuilder;
    private final String DOC_NAME_FIELD;
    private final String DOMAIN_NAME_FIELD;
    private final String PAGE_FIELD;
    private final String CONTENT_FIELD;
    private final String ATTACHMENT_FIELD;
    private final String PAGES_INDEX;
    private final String PIPELINE_NAME;
    private final String HIGHLIGHTER_TYPE;
    private final String PHRASE_SUGGESTER;

    private String contentFieldName() {
        return ATTACHMENT_FIELD + "." + CONTENT_FIELD;
    }

    public PageIndexDao(ElasticRestClient elasticRestClient,
                        String pagesIndex,
                        String pipeLineName,
                        String docNameField,
                        String domainField,
                        String pageField,
                        String contentField,
                        String attachmentField,
                        String phraseSuggester,
                        String highlighterType) {
        PAGES_INDEX = pagesIndex;
        PIPELINE_NAME = pipeLineName;
        DOC_NAME_FIELD = docNameField;
        DOMAIN_NAME_FIELD = domainField;
        PAGE_FIELD = pageField;
        CONTENT_FIELD = contentField;
        ATTACHMENT_FIELD = attachmentField;
        PHRASE_SUGGESTER = phraseSuggester;
        HIGHLIGHTER_TYPE = highlighterType;
        this.elasticRestClient = elasticRestClient;
        this.highlightBuilder = new HighlightBuilder().field(contentFieldName()).highlighterType(HIGHLIGHTER_TYPE);
    }

    public List<PageSearchResult> searchWords(String words, int fuzziness) throws IOException {
        return searchAndGetHighlights(QueryBuilders
                        .matchQuery(contentFieldName(), words).operator(Operator.AND).fuzziness(fuzziness),
                QueryFlag.WORD);
    }

    public List<PageSearchResult> searchPhrase(String phrase, int slop) throws IOException {
        return searchAndGetHighlights(QueryBuilders
                        .matchPhraseQuery(contentFieldName(), phrase)
                        .slop(slop),
                QueryFlag.PHRASE);
    }

    public List<IndexResponse> indexPages(List<PageData> pages) throws IOException {
        List<IndexResponse> responses = new ArrayList<>();
        for (PageData pageData : pages) {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.index(PAGES_INDEX);
            indexRequest.id(UUID.randomUUID().toString());
            indexRequest.setPipeline(PIPELINE_NAME);
            indexRequest.source(
                    DOMAIN_NAME_FIELD, pageData.getDomain(),
                    PAGE_FIELD, pageData.getPage(),
                    DOC_NAME_FIELD, pageData.getDocName(),
                    "data", toBase64(pageData.getContent()));
            IndexResponse indexResponse = elasticRestClient.index(indexRequest);
            responses.add(indexResponse);
        }
        return responses;
    }

    public List<ElasticDocPage> getPages(String domain, String documentName, boolean withContent) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(PAGES_INDEX);
        SearchSourceBuilder sourceBuilder =
                new SearchSourceBuilder().query(
                        QueryBuilders
                                .boolQuery()
                                .must(QueryBuilders.matchQuery(DOMAIN_NAME_FIELD, domain))
                                .must(QueryBuilders.matchQuery(DOC_NAME_FIELD, documentName)));
        if (withContent) {
            sourceBuilder = sourceBuilder.fetchSource(new String[]{"*"}, new String[]{"data"});
        } else {
            sourceBuilder = sourceBuilder.fetchSource(new String[]{"*"}, new String[]{"data", "attachment"});
        }

        searchRequest.source(sourceBuilder);
        var response = elasticRestClient.search(searchRequest);
        List<ElasticDocPage> results = new ArrayList<>();
        if (response != null) {
            for (var hit : response.getHits()) {
                var id = hit.getId();
                var docName = hit.getSourceAsMap().get(DOC_NAME_FIELD).toString();
                var domainName = hit.getSourceAsMap().get(DOMAIN_NAME_FIELD).toString();
                var page = (int) hit.getSourceAsMap().get(PAGE_FIELD);
                var content = (String) ((Map) hit.getSourceAsMap().getOrDefault(ATTACHMENT_FIELD, Collections.emptyMap()))
                        .get(CONTENT_FIELD);
                results.add(new ElasticDocPage(id, docName, domainName, page, content));
            }
        }
        return results;
    }

    public long deleteDocPages(String domain, String documentName) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest();
        deleteByQueryRequest.indices(PAGES_INDEX);
        deleteByQueryRequest.setQuery(QueryBuilders
                .boolQuery()
                .must(QueryBuilders.matchQuery(DOMAIN_NAME_FIELD, domain))
                .must(QueryBuilders.matchQuery(DOC_NAME_FIELD, documentName)));

        var response = elasticRestClient.deleteByQuery(deleteByQueryRequest);
        return response.getDeleted();
    }

    private List<PageSearchResult> searchAndGetHighlights(QueryBuilder queryBuilders, QueryFlag queryFlag) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(PAGES_INDEX);
        SearchSourceBuilder sourceBuilder =
                new SearchSourceBuilder().query(queryBuilders)
                        .highlighter(highlightBuilder);
        sourceBuilder = sourceBuilder.fetchSource(new String[]{"*"}, new String[]{"data", "attachment"});
        searchRequest.source(sourceBuilder);
        SearchResponse response = elasticRestClient.search(searchRequest);
        List<PageSearchResult> pageSearchResults = new ArrayList<>();
        if (response != null) {
            for (var hit : response.getHits()) {
                var fileName = hit.getSourceAsMap().get(DOC_NAME_FIELD).toString();
                var domain = hit.getSourceAsMap().get(DOMAIN_NAME_FIELD).toString();
                var page = Integer.parseInt(hit.getSourceAsMap().get(PAGE_FIELD).toString());
                List<Highlight> highLightList = new ArrayList<>();
                var highLightFields = hit.getHighlightFields();
                if (!highLightFields.containsKey(contentFieldName())) {
                    continue;
                }
                for (var text : highLightFields.get(contentFieldName()).getFragments()) {
                    highLightList.add(parseHighLight(text.string(), "em"));
                }
                pageSearchResults.add(new PageSearchResult(domain, fileName, page, hit.getScore(), highLightList, queryFlag));
            }
        }
        return pageSearchResults;
    }

    private Highlight parseHighLight(String highlightString, String tag) {
        List<TaggedSubstring> matchedSubstrings = new ArrayList<>();
        StringBuilder filteredHighlightStringBuilder = new StringBuilder();
        Pattern pattern = Pattern.compile(String.format("(<%s>(.*?)</%s>)", tag, tag));
        int previousGroupEnd = 0;
        Matcher matcher = pattern.matcher(highlightString);
        while (matcher.find()) {
            var matchedString = matcher.group(2);
            filteredHighlightStringBuilder.append(highlightString, previousGroupEnd, matcher.start(1));

            matchedSubstrings.add(new TaggedSubstring(
                    filteredHighlightStringBuilder.length(),
                    filteredHighlightStringBuilder.length() + matchedString.length(),
                    matchedString));
            filteredHighlightStringBuilder.append(matchedString);
            previousGroupEnd = matcher.end(1);
        }
        filteredHighlightStringBuilder.append(highlightString.substring(previousGroupEnd));

        return new Highlight(filteredHighlightStringBuilder.toString(), matchedSubstrings);
    }

    public List<PhraseSuggestResult> suggestContent(String phrase) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(PAGES_INDEX);
        PhraseSuggestionBuilder phraseSuggestionBuilder = new PhraseSuggestionBuilder(contentFieldName());
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

}
