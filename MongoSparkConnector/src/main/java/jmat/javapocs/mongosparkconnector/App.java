package jmat.javapocs.mongosparkconnector;

import com.google.common.collect.Lists;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.spark.MongoConnector;
import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.config.WriteConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.bson.Document;

public class App {

    public static void main(final String[] args) {
        final SparkConf sparkConf = new SparkConf()
            .setAppName("Mongo Spark Connector POC");

        try (final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf)) {
            final JavaRDD<Document> tasks = MongoSpark.load(sparkContext)
                .withPipeline(Arrays.asList(Document.parse("{$match: {$and: [{run:true}, {'attributes.type':'runnable'}]}}")));

            final JavaRDD<Document> executions = tasks.map(task -> new Document()
                .append("taskId", task.get("_id"))
                .append("ranAt", new Date())
            );

            final WriteConfig createExecutionWriteConfig = WriteConfig.create(sparkContext)
                .withOption("collection", "executions");
            MongoSpark.save(executions, createExecutionWriteConfig);

            final JavaRDD<UpdateOneModel<Document>> updates = tasks.map(task -> new UpdateOneModel<>(
                new Document("_id", task.get("_id")),
                new Document()
                .append("$set", new Document("lastRanAt", new Date()))
                .append("$inc", new Document("executionCount", 1))
            ));

            final WriteConfig updateTaskWriteConfig = WriteConfig.create(sparkContext);
            final MongoConnector connector = MongoConnector.create(sparkContext);
            updates.foreachPartition(iterator -> {
                connector.withCollectionDo(
                    updateTaskWriteConfig,
                    Document.class,
                    collection -> {
                        final ArrayList<UpdateOneModel<Document>> partition = Lists.newArrayList(iterator);
                        if (!partition.isEmpty()) {
                            collection.bulkWrite(partition);
                        }
                        return "";
                    }
                );
            });
        }
    }
}
