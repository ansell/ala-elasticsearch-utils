/**
 * 
 */
package au.org.ala.elasticsearch.utils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * Example code. Do NOT run in production.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class AlaElasticsearchExamples {

    /**
     * @param client
     * @param sourceIndex
     * @param destinationIndex
     * @throws IOException
     */
    public static void deleteAndRecreateIndexes(RestHighLevelClient client,
            final String sourceIndex, final String destinationIndex) throws IOException {
        // Reference:
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-indices-exists.html
        GetIndexRequest getSourceRequest = new GetIndexRequest(sourceIndex);
    
        if (client.indices().exists(getSourceRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest deleteSourceRequest = new DeleteIndexRequest(sourceIndex);
            client.indices().delete(deleteSourceRequest, RequestOptions.DEFAULT);
        }
    
        GetIndexRequest getDestinationRequest = new GetIndexRequest(destinationIndex);
    
        if (client.indices().exists(getDestinationRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest deleteDestinationRequest = new DeleteIndexRequest(
                    destinationIndex);
            client.indices().delete(deleteDestinationRequest, RequestOptions.DEFAULT);
        }
        
        // Reference:
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-create-index.html
        CreateIndexRequest createSourceRequest = new CreateIndexRequest(sourceIndex);
    
        createSourceRequest.settings(Settings.builder().put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2));
    
        createSourceRequest.mapping(
                "{\"properties\": { \"message\": { \"type\": \"text\" }, \"postDate\": { \"type\": \"text\" } } }",
                XContentType.JSON);
    
        CreateIndexResponse createSourceIndexResponse = client.indices()
                .create(createSourceRequest, RequestOptions.DEFAULT);
        System.out.println(
                String.format("Created source index. acknowledged=%s shardsAcknowledged=%s",
                        createSourceIndexResponse.isAcknowledged(),
                        createSourceIndexResponse.isShardsAcknowledged()));
    
        // Reference:
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-create-index.html
        CreateIndexRequest createDestinationRequest = new CreateIndexRequest(destinationIndex);
    
        createDestinationRequest.settings(Settings.builder().put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2));
    
        // Example: Change the postDate type to date
        createDestinationRequest.mapping(
                "{\"properties\": { \"message\": { \"type\": \"text\" }, \"postDate\": { \"type\": \"date\" } } }",
                XContentType.JSON);
    
        CreateIndexResponse createDestinationIndexResponse = client.indices()
                .create(createDestinationRequest, RequestOptions.DEFAULT);
        System.out.println(String.format(
                "Created destination index. acknowledged=%s shardsAcknowledged=%s",
                createDestinationIndexResponse.isAcknowledged(),
                createDestinationIndexResponse.isShardsAcknowledged()));
    
        String indexRequestId = "1";
        String currentDateTime = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                .format(LocalDateTime.now());
        String jsonString = "{ \"postDate\": \"" + currentDateTime
                + "\", \"message\": \"Testing reindex process\" }";
        System.out.println(jsonString);
    
        IndexRequest indexRequest = new IndexRequest("posts");
        indexRequest.id(indexRequestId);
        indexRequest.source(jsonString, XContentType.JSON);
    }

}
