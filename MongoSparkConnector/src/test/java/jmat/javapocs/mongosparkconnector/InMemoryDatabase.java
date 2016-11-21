package jmat.javapocs.mongosparkconnector;

import de.bwaldvogel.mongo.MongoBackend;
import de.bwaldvogel.mongo.MongoCollection;
import de.bwaldvogel.mongo.backend.Utils;
import de.bwaldvogel.mongo.backend.memory.MemoryDatabase;
import de.bwaldvogel.mongo.bson.Document;
import de.bwaldvogel.mongo.exception.MongoServerException;
import io.netty.channel.Channel;
import java.util.ArrayList;

public class InMemoryDatabase extends MemoryDatabase {

    public InMemoryDatabase(final MongoBackend backend, final String databaseName) throws MongoServerException {
        super(backend, databaseName);
    }

    @Override
    public Document handleCommand(final Channel channel, final String command, final Document query) throws MongoServerException {
        System.out.printf("[InMemoryDatabase.handleCommand] command: %s, query: %s%n%n", command, query);

        if (command.equals("find")) {
            return this.handleFind(query.get(command).toString(), query);
        } else if (command.equals("aggregate")) {
            return this.handleAggregate(query.get(command).toString(), query);
        } else if (command.equals("getMore")) {
            return this.handleGetMore();
        }

        return super.handleCommand(channel, command, query);
    }

    private Document handleFind(final String collectionName, final Document query) throws MongoServerException {
        final Document realQuery = (Document) query.get("filter");
        final MongoCollection<Integer> documents = this.resolveCollection(collectionName, true);

        final ArrayList<Document> results = new ArrayList<>();
        documents.handleQuery(realQuery, 0, Integer.MAX_VALUE, null).forEach(results::add);

        final Document response = new Document()
            .append("cursor", new Document()
                .append("id", Long.MAX_VALUE)
                .append("ns", "todo.tasks")
                .append("firstBatch", results)
            );
        Utils.markOkay(response);
        return response;
    }

    private Document handleAggregate(final String collectionName, final Document query) throws MongoServerException {
        final MongoCollection<Integer> documents = this.resolveCollection(collectionName, true);

        final ArrayList<Document> pipeline = (ArrayList<Document>) query.get("pipeline");
        System.out.printf("[InMemoryDatabase.handleAggregate] pipeline: %s%n%n", pipeline);
        final Document realMatch = pipeline.get(1);
        System.out.printf("[InMemoryDatabase.handleAggregate] realMatch: %s%n%n", realMatch);
        final Document realQuery = (Document) realMatch.get("$match");
        System.out.printf("[InMemoryDatabase.handleAggregate] realQuery: %s%n%n", realQuery);

        final ArrayList<Document> results = new ArrayList<>();
        documents.handleQuery(realQuery, 0, Integer.MAX_VALUE, null).forEach(results::add);

        final Document response = new Document()
            .append("cursor", new Document()
                .append("id", Long.MAX_VALUE)
                .append("ns", "todo.tasks")
                .append("firstBatch", results)
            );
        Utils.markOkay(response);
        return response;
    }

    private Document handleGetMore() throws MongoServerException {
        final Document response = new Document()
            .append("cursor", new Document()
                .append("id", 0L)
                .append("ns", "todo.tasks")
                .append("nextBatch", new ArrayList<>())
            );
        Utils.markOkay(response);
        return response;
    }
}
