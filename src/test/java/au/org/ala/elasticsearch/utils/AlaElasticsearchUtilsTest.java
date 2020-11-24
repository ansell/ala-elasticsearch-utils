/**
 * 
 */
package au.org.ala.elasticsearch.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.elasticsearch.client.RestHighLevelClient;
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
    private static String testDestinationIndex = "example-destination-index-utils-test";

    /**
     * @throws java.lang.Exception
     */
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        String esHostname = "localhost";
        int esPort = 9200;
        String esScheme = "http";
        testESClient = AlaElasticsearchUtils.newElasticsearchClient(esHostname, esPort, esScheme);

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
        AlaElasticsearchTestUtils.deleteAndRecreateIndexes(testESClient, testSourceIndex,
                testDestinationIndex);

        //Thread.sleep(10000);

        AlaElasticsearchUtils.listIndexes(testESClient);
        
        AlaElasticsearchUtils.indexInfo(testESClient, testSourceIndex);
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
        AlaElasticsearchUtils.asyncReindex(testESClient, testSourceIndex, testDestinationIndex);
    }

}
