package jmat.javapocs.embeddedelasticsearch;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.io.stream.OutputStreamStreamOutput;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

public class Main {

    public static void main(final String[] args) throws Exception {
        final Settings settings = ImmutableSettings.settingsBuilder()
            .put("node.name", "node-test-" + System.currentTimeMillis())
            .put("node.data", true)
            .put("cluster.name", "cluster-test-" + NetworkUtils.getLocalAddress().getHostName())
            .put("index.store.type", "memory")
            .put("index.store.fs.memory.enabled", "true")
            .put("gateway.type", "none")
            .put("path.data", "/tmp/elasticsearch-test/data")
            .put("path.work", "/tmp/elasticsearch-test/work")
            .put("path.logs", "/tmp/elasticsearch-test/logs")
            .put("index.number_of_shards", "1")
            .put("index.number_of_replicas", "0")
            .put("cluster.routing.schedule", "50ms")
            .put("node.local", true)
            .put("http.port", 3333)
            .build();

        final Node node = NodeBuilder.nodeBuilder().settings(settings).node();

        System.out.printf("%n%npath.data: %s%n%n", node.settings().get("path.data"));

        try (final Client client = node.client()) {
            client.admin().cluster()
                .prepareHealth()
                .setWaitForYellowStatus()
                .setTimeout(TimeValue.timeValueMinutes(1))
                .get();

            IndexResponse indexResponse = client.prepareIndex("todo", "tasks")
                .setSource("{\"title\":\"Hello World!\"}")
                .setRefresh(true)
                .get();

            indexResponse.writeTo(new OutputStreamStreamOutput(System.out));

            System.out.printf(
                "%n%nNew document created: /%s/%s/%s%n%n",
                indexResponse.getIndex(), indexResponse.getType(), indexResponse.getId()
            );

            final String pathToDocument = String.join("/", indexResponse.getIndex(), indexResponse.getType(), indexResponse.getId());
            try (final InputStream in = new URL("http://localhost:3333/" + pathToDocument).openStream()) {
                final String response = IOUtils.toString(in);
                System.out.printf("%n%nResponse for /%s: %s%n%n", pathToDocument, response);
            }
        }

        try (final InputStream in = new URL("http://localhost:3333/").openStream()) {
            final String response = IOUtils.toString(in);
            System.out.printf("%n%nResponse for /: %s%n%n", response);
        }

        final String searchPath = "todo/tasks/_search";
        try (final InputStream in = new URL("http://localhost:3333/" + searchPath).openStream()) {
            final String response = IOUtils.toString(in);
            System.out.printf("%n%nResponse for /%s: %s%n%n", searchPath, response);
        }

        node.close();
        FileSystemUtils.deleteRecursively(new File("/tmp/elasticsearch-test/"), true);
    }
}
