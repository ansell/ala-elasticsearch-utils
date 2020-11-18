/**
 * 
 */
package au.org.ala.elasticsearchreindex;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.tasks.TaskSubmissionResponse;
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
            // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-document-reindex.html
            ReindexRequest request = new ReindexRequest();
            request.setSourceIndices("filebeat-nginx-2020.11.18");
            request.setDestIndex("filebeat-www-2020.11.18");
            request.setDestVersionType(VersionType.EXTERNAL);
            request.setDestOpType("index");
            // Very small sample size while creating the algorithm
            request.setMaxDocs(10);
            request.setRefresh(true);

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
            TaskSubmissionResponse reindexSubmission = client.submitReindexTask(request,
                    RequestOptions.DEFAULT);
            String taskId = reindexSubmission.getTask();
            System.out.println(String.format("Task created with id: {}", taskId));
        }
    }

}
