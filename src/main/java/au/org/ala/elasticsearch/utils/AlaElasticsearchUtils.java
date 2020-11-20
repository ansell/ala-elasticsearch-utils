/**
 * 
 */
package au.org.ala.elasticsearch.utils;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksRequest;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.tasks.TaskSubmissionResponse;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.tasks.TaskInfo;

/**
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class AlaElasticsearchUtils {

    /**
     * Asynchronously trigger a reindex of the given source index to the given
     * destination index.
     * 
     * @param client
     *            The {@link RestHighLevelClient} to use to reindex.
     * @param sourceIndex
     *            The source index.
     * @param destinationIndex
     *            The destination index.
     * @return The task ID of the reindex task that was asynchronously run.
     * @throws IOException
     *             If communication with the server had an issue.
     */
    public static String asyncReindex(RestHighLevelClient client, final String sourceIndex,
            final String destinationIndex) throws IOException {
        // Reference:
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-document-reindex.html
        ReindexRequest reindexRequest = new ReindexRequest();
        reindexRequest.setSourceIndices(sourceIndex);
        reindexRequest.setDestIndex(destinationIndex);
        reindexRequest.setDestVersionType(VersionType.EXTERNAL);
        reindexRequest.setDestOpType("index");
        // Very small sample size while creating the algorithm
        reindexRequest.setMaxDocs(10);
        reindexRequest.setRefresh(true);
    
        TaskSubmissionResponse reindexSubmission = client.submitReindexTask(reindexRequest,
                RequestOptions.DEFAULT);
        String taskId = reindexSubmission.getTask();
        System.out.println(String.format("Task created with id: %s", taskId));
        return taskId;
    }

    /**
     * Wait for task to complete.
     * 
     * @param client
     *            The {@link RestHighLevelClient} to use to check the task
     *            status
     * @param taskId
     *            The task id to find and wait for completion before returning
     * @throws InterruptedException
     *             If waiting was interrupted.
     * @throws IOException
     *             If communication with the server had an issue.
     */
    public static void waitForTask(RestHighLevelClient client, String taskId)
            throws InterruptedException, IOException {
        org.elasticsearch.tasks.TaskId parentTaskId = new org.elasticsearch.tasks.TaskId(taskId);
    
        boolean subTasksRunning = true;
        while (subTasksRunning) {
            ListTasksRequest listTasksRequest = new ListTasksRequest();
            listTasksRequest.setDetailed(true);
            ListTasksResponse listTasksResponse = client.tasks().list(listTasksRequest,
                    RequestOptions.DEFAULT);
            if (listTasksResponse.getTasks().isEmpty()) {
                subTasksRunning = false;
            } else {
                boolean foundMatchingRunningTask = false;
                for (TaskInfo taskInfo : listTasksResponse.getTasks()) {
                    if (taskInfo.getTaskId().equals(parentTaskId)) {
                        System.out.println("Matching task: " + taskInfo.toString());
                        foundMatchingRunningTask = true;
                    } else {
                        System.out.println("Non-matching task: " + taskInfo.toString());
                    }
                }
                subTasksRunning = foundMatchingRunningTask;
            }
            if (subTasksRunning) {
                System.out.println("Task still running, waiting again...");
                // Wait before checking again
                Thread.sleep(100);
            }
        }
    }

    /**
     * @param esHostname
     * @param esPort
     * @param esScheme
     * @return
     */
    public static RestHighLevelClient newElasticsearchClient(final String esHostname,
            final int esPort, final String esScheme) {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(esHostname, esPort, esScheme)));
        return client;
    }

    /**
     * @param client
     * @param indexToDelete
     * @throws IOException
     */
    public static void deleteIndex(RestHighLevelClient client, final String indexToDelete)
            throws IOException {
        DeleteIndexRequest deleteDestinationRequest = new DeleteIndexRequest(
                indexToDelete);
        client.indices().delete(deleteDestinationRequest, RequestOptions.DEFAULT);
    }

}
