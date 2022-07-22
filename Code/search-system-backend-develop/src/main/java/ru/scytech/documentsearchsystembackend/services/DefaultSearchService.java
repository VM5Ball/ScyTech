package ru.scytech.documentsearchsystembackend.services;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import ru.scytech.documentsearchsystembackend.dao.DocIndexDao;
import ru.scytech.documentsearchsystembackend.dao.PageIndexDao;
import ru.scytech.documentsearchsystembackend.model.QueryDTO;
import ru.scytech.documentsearchsystembackend.model.elastic.ElasticDoc;
import ru.scytech.documentsearchsystembackend.model.filesystem.DocData;
import ru.scytech.documentsearchsystembackend.model.results.PageSearchResult;
import ru.scytech.documentsearchsystembackend.model.results.PhraseSuggestResult;
import ru.scytech.documentsearchsystembackend.model.results.SearchResult;
import ru.scytech.documentsearchsystembackend.model.results.TitleSearchResult;
import ru.scytech.documentsearchsystembackend.services.interfaces.DocumentAccessService;
import ru.scytech.documentsearchsystembackend.utils.EncodeUtils;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DefaultSearchService {
    private DocumentAccessService documentAccessService;
    private FileSeparateService fileSeparateService;
    private DocIndexDao docIndexDao;
    private PageIndexDao pageIndexDao;
    private Pattern quotePattern = Pattern.compile("^\"(.*)\"$");

    public DefaultSearchService(DocumentAccessService documentAccessService,
                                FileSeparateService fileSeparateService,
                                DocIndexDao docIndexDao,
                                PageIndexDao pageIndexDao) throws IOException {
        this.documentAccessService = documentAccessService;
        this.fileSeparateService = fileSeparateService;
        this.docIndexDao = docIndexDao;
        this.pageIndexDao = pageIndexDao;
        indexRootDirectory();
    }

    private void indexRootDirectory() throws IOException {
        var rootDirectory = documentAccessService.loadDocsRepository(false);
        var docs = docIndexDao.getAllDocs();
        for (var entry : rootDirectory.entrySet()) {
            String domain = entry.getKey();
            for (var docName : entry.getValue()) {
                if (docIndexDao.getDoc(domain, docName).isPresent()) {
                    docIndexDao.deleteDoc(domain, docName);
                    pageIndexDao.deleteDocPages(domain, docName);
                }
                byte[] bytes = documentAccessService.getFile(docName, domain);

                Optional<String> typeOptional = documentAccessService.getFileContentType(docName, domain);
                MediaType mediaType = toMediaType(typeOptional);
                var pagedFile = fileSeparateService.separateFile(docName, domain, mediaType, bytes);
                ElasticDoc elasticDoc = new ElasticDoc(docName, domain, pagedFile.getTitle(),
                        EncodeUtils.sha256HexHash(bytes), new String[]{});
                docIndexDao.indexDoc(elasticDoc);
                pageIndexDao.indexPages(pagedFile.getPages());
            }
        }

        for (var doc : docs) {
            if (!rootDirectory.containsKey(doc.getDomain()) || !rootDirectory.get(doc.getDomain()).contains(doc.getDocName())) {
                docIndexDao.deleteDoc(doc.getDomain(), doc.getDocName());
                pageIndexDao.deleteDocPages(doc.getDomain(), doc.getDocName());
            }
        }
    }

    public void indexDoc(String docName, String domain, List<String> tags, InputStream inputStream)
            throws IOException {
        if (docIndexDao.getDoc(domain, docName).isPresent()) {
            throw new IllegalArgumentException(
                    String.format("File \"%s\" in domain \"%s\" already exists", docName, domain)
            );
        }
        byte[] bytes = inputStream.readAllBytes();
//        var docWithEqualHash = docsDBService.findDocByHash(bytes);
//        if (docWithEqualHash.isPresent()) {
//            throw new IllegalArgumentException(
//                    String.format("File with equal content already exists, see %s/%s ", domain, fileName)
//            );
//        }
        documentAccessService.saveFile(docName, domain, bytes);

        Optional<String> typeOptional = documentAccessService.getFileContentType(docName, domain);
        MediaType mediaType = toMediaType(typeOptional);
        var pagedFile = fileSeparateService.separateFile(docName, domain, mediaType, bytes);
        ElasticDoc elasticDoc = new ElasticDoc(docName, domain, pagedFile.getTitle(), EncodeUtils.sha256HexHash(bytes), new String[]{});
        docIndexDao.indexDoc(elasticDoc);
        pageIndexDao.indexPages(pagedFile.getPages());
    }

    public void deleteDoc(String domain, String fileName) throws IOException {
        if (docIndexDao.getDoc(domain, fileName).isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("File \"%s\" in domain \"%s\" not found", fileName, domain)
            );
        }

        documentAccessService.deleteFile(fileName, domain);
        pageIndexDao.deleteDocPages(domain, fileName);
        docIndexDao.deleteDoc(domain, fileName);
    }


    public void updateDoc(String fileName, String domain, List<String> tags, InputStream inputStream) throws IOException {
        deleteDoc(domain, fileName);
        indexDoc(fileName, domain, tags, inputStream);
    }

    public DocData loadDoc(String domain, String fileName) throws IOException {
        byte[] bytes = documentAccessService.getFile(fileName, domain);
        Optional<String> typeOptional = documentAccessService.getFileContentType(fileName, domain);
        return new DocData(fileName, domain, toMediaType(typeOptional), bytes);
    }

    public MediaType toMediaType(Optional<String> typeOptional) {
        return typeOptional.map(MediaType::valueOf).orElse(MediaType.TEXT_PLAIN);
    }

    public List<? extends SearchResult> searchIntoPageContent(QueryDTO query) throws IOException {
        List<? extends SearchResult> result = new ArrayList<>();
        Matcher matcher = quotePattern.matcher(query.getQuery());

        switch (query.getQueryFlag()) {
            case WORD:
                if (!matcher.find()) {
                    result = pageIndexDao.searchWords(query.getQuery(), 2);
                } else {
                    result = pageIndexDao.searchWords(query.getQuery(), 0);
                }
                break;
            case TITLE:
                result = docIndexDao.searchTitle(query.getQuery());
                break;
            default:
                if (!matcher.find()) {
                    result = pageIndexDao.searchPhrase(query.getQuery(), 2);
                } else {
                    result = pageIndexDao.searchPhrase(matcher.group(1), 0);
                }
                break;
        }

        if (!query.getDomains().isEmpty()) {
            result = result.stream().filter(it -> query.getDomains().contains(it.getDomain())).collect(Collectors.toList());
        }
        if (!query.getFiles().isEmpty()) {
            result = result.stream().filter(it -> query.getFiles().contains(it.getDocName())).collect(Collectors.toList());
        }
        return result;
    }

    public List<PageSearchResult> searchPhraseIntoPageContent(String phrase) throws IOException {
        return pageIndexDao.searchPhrase(phrase, 2);
    }


    public List<PageSearchResult> searchWordIntoPageContent(String word) throws IOException {
        return pageIndexDao.searchWords(word, 2);
    }

    public List<TitleSearchResult> searchWordIntoDocTitle(String title) throws IOException {
        return docIndexDao.searchTitle(title);
    }

    public List<PhraseSuggestResult> suggestIntoPageContent(String phrase) throws IOException {
        return pageIndexDao.suggestContent(phrase);
    }

    public List<PhraseSuggestResult> suggestIntoDocTitle(String title) throws IOException {
        return docIndexDao.suggestTitle(title);
    }

    public byte[] getTitleImage(String domain, String docName) throws IOException, OperationNotSupportedException {
        if (docIndexDao.getDoc(domain, docName).isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("File \"%s\" in domain \"%s\" already exists", docName, domain)
            );
        }
        byte[] bytes = documentAccessService.getFile(docName, domain);
        Optional<String> typeOptional = documentAccessService.getFileContentType(docName, domain);
        MediaType mediaType = toMediaType(typeOptional);
        return fileSeparateService.getTitleImage(docName, domain, mediaType, bytes);
    }


}
