/**
 * 
 */
package au.org.ala.elasticsearchreindex;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.tasks.TaskSubmissionResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;

/**
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class Reindex {

    public static void main(String[] args) throws Exception {
        // Reference:
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-getting-started-initialization.html
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));) {

            // Reference:
            // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-indices-exists.html
            GetIndexRequest getSourceRequest = new GetIndexRequest("example-filebeat-nginx-2020.11.18");

            if (client.indices().exists(getSourceRequest, RequestOptions.DEFAULT)) {
                DeleteIndexRequest deleteSourceRequest = new DeleteIndexRequest(
                        "example-filebeat-nginx-2020.11.18");
                client.indices().delete(deleteSourceRequest, RequestOptions.DEFAULT);
            }

            GetIndexRequest getDestinationRequest = new GetIndexRequest("example-filebeat-nginx-2020.11.18");

            if (client.indices().exists(getDestinationRequest, RequestOptions.DEFAULT)) {
                DeleteIndexRequest deleteDestinationRequest = new DeleteIndexRequest(
                        "example-filebeat-www-2020.11.18");
                client.indices().delete(deleteDestinationRequest, RequestOptions.DEFAULT);
            }
            // Reference:
            // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-create-index.html
            CreateIndexRequest createSourceRequest = new CreateIndexRequest(
                    "example-filebeat-nginx-2020.11.18");

            createSourceRequest.settings(Settings.builder().put("index.number_of_shards", 3)
                    .put("index.number_of_replicas", 2));

            createSourceRequest.mapping("{\"properties\": { \"message\": { \"type\": \"text\" }, \"postDate\": { \"type\": \"text\" } } }", XContentType.JSON);

            CreateIndexResponse createSourceIndexResponse = client.indices().create(createSourceRequest,
                    RequestOptions.DEFAULT);
            System.out.println(String.format("Created source index. acknowledged=%s shardsAcknowledged=%s",
                    createSourceIndexResponse.isAcknowledged(),
                    createSourceIndexResponse.isShardsAcknowledged()));

            // Reference:
            // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-create-index.html
            CreateIndexRequest createDestinationRequest = new CreateIndexRequest(
                    "example-filebeat-www-2020.11.18");

            createDestinationRequest.settings(Settings.builder().put("index.number_of_shards", 3)
                    .put("index.number_of_replicas", 2));

            // Example: Change the postDate type to date
            createDestinationRequest.mapping("{\"properties\": { \"message\": { \"type\": \"text\" }, \"postDate\": { \"type\": \"date\" } } }", XContentType.JSON);

            CreateIndexResponse createDestinationIndexResponse = client.indices().create(createDestinationRequest,
                    RequestOptions.DEFAULT);
            System.out.println(String.format("Created destination index. acknowledged=%s shardsAcknowledged=%s",
                    createDestinationIndexResponse.isAcknowledged(),
                    createDestinationIndexResponse.isShardsAcknowledged()));

            IndexRequest indexRequest = new IndexRequest("posts");
            indexRequest.id("1");
            String currentDateTime = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    .format(LocalDateTime.now());
            String jsonString = "{ \"postDate\": \"" + currentDateTime + "\", \"message\": \"Testing reindex process\" }";
            System.out.println(jsonString);
            indexRequest.source(jsonString, XContentType.JSON);

            // Reference:
            // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-document-reindex.html
            ReindexRequest reindexRequest = new ReindexRequest();
            reindexRequest.setSourceIndices("example-filebeat-nginx-2020.11.18");
            reindexRequest.setDestIndex("example-filebeat-www-2020.11.18");
            reindexRequest.setDestVersionType(VersionType.EXTERNAL);
            reindexRequest.setDestOpType("index");
            // Very small sample size while creating the algorithm
            reindexRequest.setMaxDocs(10);
            reindexRequest.setRefresh(true);

            // ActionListener<BulkByScrollResponse> listener = new
            // ActionListener<BulkByScrollResponse>() {
            //
            // @Override
            // public void onResponse(BulkByScrollResponse response) {
            // System.out.println("Reindex completed successfully");
            // }
            //
            // @Override
            // public void onFailure(Exception e) {
            // System.out.println("Reindex failed");
            // }
            //
            // };
            // client.reindexAsync(request, RequestOptions.DEFAULT, listener );
            TaskSubmissionResponse reindexSubmission = client.submitReindexTask(reindexRequest,
                    RequestOptions.DEFAULT);
            String taskId = reindexSubmission.getTask();
            System.out.println(String.format("Task created with id: %s", taskId));

        }
    }

}
