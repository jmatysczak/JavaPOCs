package jmat.javapocs.mongosparkconnector;

import de.bwaldvogel.mongo.backend.Utils;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import de.bwaldvogel.mongo.backend.memory.MemoryDatabase;
import de.bwaldvogel.mongo.bson.Document;
import de.bwaldvogel.mongo.exception.MongoServerException;
import de.bwaldvogel.mongo.wire.BsonConstants;
import io.netty.channel.Channel;
import java.util.Arrays;
import java.util.List;

public class InMemoryBackend extends MemoryBackend {

    private static final List<Integer> VERSION = Arrays.asList(3, 2, 2);

    @Override
    public MemoryDatabase openOrCreateDatabase(final String databaseName) throws MongoServerException {
        return new InMemoryDatabase(this, databaseName);
    }

    @Override
    public Document handleCommand(final Channel channel, final String databaseName, final String command, final Document query) throws MongoServerException {
        System.out.printf("[InMemoryBackend.handleCommand] command: %s, query: %s%n%n", command, query);

        if (command.equalsIgnoreCase("buildinfo")) {
            final Document response = new Document()
                .append("version", Utils.join(VERSION, '.'))
                .append("versionArray", VERSION)
                .append("maxBsonObjectSize", BsonConstants.MAX_BSON_OBJECT_SIZE);
            Utils.markOkay(response);
            return response;
        }

        return super.handleCommand(channel, databaseName, command, query);
    }
}
