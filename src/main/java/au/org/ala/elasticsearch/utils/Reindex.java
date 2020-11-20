/**
 * 
 */
package au.org.ala.elasticsearch.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class Reindex {

    public static void main(String[] args) throws Exception {
        final OptionParser parser = new OptionParser();

        final OptionSpec<Void> help = parser.accepts("help").forHelp();
        final OptionSpec<String> sourceOption = parser.accepts("source").withRequiredArg()
                .ofType(String.class).required()
                .describedAs("The elasticsearch index to use as the source for the reindex");
        final OptionSpec<String> destinationOption = parser.accepts("destination").withRequiredArg()
                .ofType(String.class).required()
                .describedAs("The elasticsearch index to use as the destination for the reindex");

        OptionSet options = null;

        try {
            options = parser.parse(args);
        } catch (final OptionException e) {
            System.out.println(e.getMessage());
            parser.printHelpOn(System.out);
            throw e;
        }

        if (options.has(help)) {
            parser.printHelpOn(System.out);
            return;
        }

        // final String sourceIndex = "example-filebeat-nginx-2020.11.18";
        // final String destinationIndex = "example-filebeat-www-2020.11.18";

        String sourceIndex = sourceOption.value(options);
        String destinationIndex = destinationOption.value(options);

        // Reference:
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-getting-started-initialization.html
        final String esHostname = "localhost";
        final int esPort = 9200;
        final String esScheme = "http";
        try (RestHighLevelClient client = AlaElasticsearchUtils.newElasticsearchClient(esHostname,
                esPort, esScheme);) {

            // deleteAndRecreateIndexes(client, sourceIndex, destinationIndex);

            String taskId = AlaElasticsearchUtils.asyncReindex(client, sourceIndex,
                    destinationIndex);

            AlaElasticsearchUtils.waitForTask(client, taskId);
        }
    }

}
