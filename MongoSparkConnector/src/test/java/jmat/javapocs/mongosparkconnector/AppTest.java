package jmat.javapocs.mongosparkconnector;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import de.bwaldvogel.mongo.MongoServer;
import java.util.function.Consumer;
import org.bson.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AppTest {

    private MongoServer server;

    @Before
    public void setUp() {
        System.setProperty("spark.master", "local[*]");
        System.setProperty("spark.mongodb.input.uri", "mongodb://127.0.0.1:27017/todo.tasks?readPreference=primaryPreferred");
        System.setProperty("spark.mongodb.output.uri", "mongodb://127.0.0.1:27017/todo.tasks");

        this.server = new MongoServer(new InMemoryBackend());
        this.server.bind("127.0.0.1", 27017);

        try (final MongoClient client = new MongoClient("127.0.0.1", 27017)) {
            final MongoCollection<Document> tasks = client.getDatabase("todo").getCollection("tasks");
            tasks.insertOne(new Document()
                .append("title", "Task 1")
                .append("run", true)
                .append("attributes", new Document("type", "runnable"))
                .append("executionCount", 0)
            );
            tasks.insertOne(new Document()
                .append("title", "Task 2")
                .append("run", true)
                .append("attributes", new Document("type", "not runnable"))
                .append("executionCount", 0)
            );
            tasks.insertOne(new Document()
                .append("title", "Task 3")
                .append("run", false)
                .append("attributes", new Document("type", "not runnable"))
                .append("executionCount", 0)
            );
            tasks.insertOne(new Document()
                .append("title", "Task 4")
                .append("run", false)
                .append("attributes", new Document("type", "runnable"))
                .append("executionCount", 0)
            );
            tasks.insertOne(new Document()
                .append("title", "Task 5")
                .append("run", true)
                .append("attributes", new Document("type", "runnable"))
                .append("executionCount", 0)
            );
        }
    }

    @After
    public void tearDown() {
        if (this.server != null) {
            this.server.shutdown();
        }
    }

    @Test
    public void ensure_that_tasks_are_updated_and_exections_are_created() {
        App.main(new String[]{});

        try (final MongoClient client = new MongoClient("127.0.0.1", 27017)) {
            final MongoCollection<Document> tasks = client.getDatabase("todo").getCollection("tasks");

            Assert.assertEquals("The number of tasks is not correct.", 5, tasks.count());

            tasks.find().forEach(new Consumer<Document>() {

                @Override
                public void accept(final Document task) {
                    System.out.printf("Task: %s%n%n", task);
                    if (task.getBoolean("run") && task.get("attributes", Document.class).getString("type").equals("runnable")) {
                        Assert.assertEquals("The task execution count is not correct.", 1, task.getInteger("executionCount", -1));
                    } else {
                        Assert.assertEquals("The task execution count is not correct.", 0, task.getInteger("executionCount", -1));
                    }
                }
            });

            final MongoCollection<Document> executions = client.getDatabase("todo").getCollection("executions");

            Assert.assertEquals("The number of executions is not correct.", 2, executions.count());

            executions.find().forEach(new Consumer<Document>() {

                @Override
                public void accept(final Document execution) {
                    System.out.printf("Execution: %s%n%n", execution);
                }
            });
        }
    }
}
