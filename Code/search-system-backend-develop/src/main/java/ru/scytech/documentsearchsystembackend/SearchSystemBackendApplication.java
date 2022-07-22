package ru.scytech.documentsearchsystembackend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.scytech.documentsearchsystembackend.dao.DocIndexDao;
import ru.scytech.documentsearchsystembackend.dao.PageIndexDao;
import ru.scytech.documentsearchsystembackend.services.ElasticRestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class SearchSystemBackendApplication {


    public static void main(String[] args) {
        SpringApplication.run(SearchSystemBackendApplication.class, args);
    }

    @Bean
    public ElasticRestClient getElasticRestClient(@Value("${application.elastic.server.host}") String host,
                                                  @Value("${application.elastic.server.port}") int port,
                                                  @Value("${application.elastic.docs.index}") String docsIndex,
                                                  @Value("${application.elastic.pages.index}") String pagesIndex,
                                                  @Value("${application.elastic.pipeline.name}") String pipelineName,
                                                  @Value("${application.elastic.docs.config}") String docsConfig,
                                                  @Value("${application.elastic.pages.config}") String pagesConfig,
                                                  @Value("${application.elastic.pipeline.config}") String pipelineConfig)
            throws IOException {
        ElasticRestClient elasticRestClient = new ElasticRestClient(host, port);
        byte[] docsIndexBody = Files.readAllBytes(Paths.get(docsConfig));
        byte[] pagesIndexBody = Files.readAllBytes(Paths.get(pagesConfig));
        byte[] pipelineBytes = Files.readAllBytes(Paths.get(pipelineConfig));
        elasticRestClient.createPipelineIfNotExists(pipelineName, pipelineBytes);
        elasticRestClient.createIndexIfNotExists(docsIndex, docsIndexBody);
        elasticRestClient.createIndexIfNotExists(pagesIndex, pagesIndexBody);
        return elasticRestClient;
    }

    @Bean
    public PageIndexDao createPageIndex(ElasticRestClient elasticRestClient,
                                        @Value("${application.elastic.pipeline.name}") String pipelineName,
                                        @Value("${application.elastic.pages.index}") String pagesIndex,
                                        @Value("${application.elastic.pages.docNameField}") String docNameFiled,
                                        @Value("${application.elastic.pages.domainField}") String domainField,
                                        @Value("${application.elastic.pages.pageField}") String pageField,
                                        @Value("${application.elastic.pages.contentField}") String contentField,
                                        @Value("${application.elastic.pages.attachmentField}") String attachmentField,
                                        @Value("${application.elastic.pages.highLighterType}") String highLighterType,
                                        @Value("${application.elastic.pages.phraseSuggester}") String phraseSuggester
    ) {
        return new PageIndexDao(elasticRestClient,
                pagesIndex,
                pipelineName,
                docNameFiled,
                domainField,
                pageField,
                contentField,
                attachmentField,
                phraseSuggester,
                highLighterType);
    }

    @Bean
    public DocIndexDao createDocIndex(ElasticRestClient elasticRestClient,
                                      @Value("${application.elastic.docs.index}") String docsIndex,
                                      @Value("${application.elastic.docs.docNameField}") String docNameField,
                                      @Value("${application.elastic.docs.domainField}") String domainField,
                                      @Value("${application.elastic.docs.titleField}") String titleField,
                                      @Value("${application.elastic.docs.keywordsField}") String keywordsField,
                                      @Value("${application.elastic.docs.hashField}") String hashField,
                                      @Value("${application.elastic.docs.phraseSuggester}") String phraseSuggester


    ) {
        return new DocIndexDao(elasticRestClient,
                docsIndex,
                docNameField,
                domainField,
                titleField,
                keywordsField,
                hashField,
                phraseSuggester);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
