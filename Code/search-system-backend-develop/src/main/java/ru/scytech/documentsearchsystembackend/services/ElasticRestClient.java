package ru.scytech.documentsearchsystembackend.services;


import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.ingest.GetPipelineRequest;
import org.elasticsearch.action.ingest.GetPipelineResponse;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;

import java.io.Closeable;
import java.io.IOException;


public class ElasticRestClient implements Closeable {
    private RestHighLevelClient client;

    public ElasticRestClient(String host, int port) {
        client = new RestHighLevelClient(RestClient.builder(
                new HttpHost(host, port)
        ));
    }

    public SearchResponse search(SearchRequest searchRequest) throws IOException {
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

    public BulkByScrollResponse deleteByQuery(DeleteByQueryRequest deleteByQueryRequest) throws IOException {
        return client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
    }

    public IndexResponse index(IndexRequest indexRequest) throws IOException {
        return client.index(indexRequest, RequestOptions.DEFAULT);
    }


    public void createIndexIfNotExists(String indexName, byte[] config) throws IOException {
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
            client.indices().get(getIndexRequest, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException e) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            createIndexRequest.source(new BytesArray(config), XContentType.JSON);
            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            if (!createIndexResponse.isAcknowledged()) {
                throw new RuntimeException("Не удалось создать index " + indexName);
            }
        }
    }

    public void createPipelineIfNotExists(String pipelineName, byte[] config) throws IOException {
        GetPipelineRequest getPipelineRequest = new GetPipelineRequest(pipelineName);
        GetPipelineResponse getPipelineResponse = client.ingest().getPipeline(getPipelineRequest, RequestOptions.DEFAULT);
        var pipelines = getPipelineResponse.pipelines();
        if (pipelines.size() == 0) {
            PutPipelineRequest putPipelineRequest = new PutPipelineRequest(pipelineName, new BytesArray(config), XContentType.JSON);
            AcknowledgedResponse acknowledgedResponse = client.ingest().putPipeline(putPipelineRequest, RequestOptions.DEFAULT);
            if (!acknowledgedResponse.isAcknowledged()) {
                throw new RuntimeException("Не удалось создать pipeline");
            }
        }
    }

    public void close() throws IOException {
        client.close();
    }
}
