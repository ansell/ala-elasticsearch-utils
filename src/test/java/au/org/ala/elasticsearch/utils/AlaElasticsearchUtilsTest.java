/**
 * 
 */
package au.org.ala.elasticsearch.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.elasticsearch.client.RestHighLevelClient;
import org.graalvm.compiler.nodes.extended.GetClassNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AlaElasticsearchUtils}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class AlaElasticsearchUtilsTest {

    private static RestHighLevelClient testESClient;

    private static String testSourceIndex = "example-source-index-utils-test";
    private static String testSourceIndexTemplate = "au.org.ala.elasticsearch.utils.test.index-template-source-1.json";
    private static String testSourceIndexTemplateJSON;
    
    private static String testDestinationIndex = "example-destination-index-utils-test";
    private static String testDestinationIndexTemplate = "au.org.ala.elasticsearch.utils.test.index-template-destination-1.json";
    private static String testDestinationIndexTemplateJSON;
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        String esHostname = "localhost";
        int esPort = 9200;
        String esScheme = "http";
        testESClient = AlaElasticsearchUtils.newElasticsearchClient(esHostname, esPort, esScheme);

        testSourceIndexTemplateJSON = AlaElasticsearchUtils.class.getResourceAsStream(testSourceIndexTemplate);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterAll
    static void tearDownAfterClass() throws Exception {
        testESClient.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {
        AlaElasticsearchUtils.putTemplate(testESClient, testSourceIndex,
                testSourceIndexTemplateJSON);
        AlaElasticsearchUtils.putTemplate(testESClient, testDestinationIndex,
                testDestinationIndexTemplateJSON);

        AlaElasticsearchTestUtils.deleteAndRecreateIndexes(testESClient, testSourceIndex,
                testDestinationIndex);

        // Thread.sleep(10000);

        AlaElasticsearchUtils.listIndexes(testESClient);

        AlaElasticsearchUtils.indexInfo(testESClient, testSourceIndex);
        AlaElasticsearchUtils.indexInfo(testESClient, testDestinationIndex);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterEach
    void tearDown() throws Exception {
        try {
            AlaElasticsearchUtils.deleteIndex(testESClient, testSourceIndex);
        } finally {
            AlaElasticsearchUtils.deleteIndex(testESClient, testDestinationIndex);
        }
    }

    /**
     * Test method for
     * {@link au.org.ala.elasticsearch.utils.AlaElasticsearchUtils#asyncReindex(org.elasticsearch.client.RestHighLevelClient, java.lang.String, java.lang.String)}.
     */
    @Test
    final void testAsyncReindex() throws Exception {
        String reindexTaskId = AlaElasticsearchUtils.asyncReindex(testESClient, testSourceIndex,
                testDestinationIndex);
        AlaElasticsearchUtils.waitForTask(testESClient, reindexTaskId);
    }

}
