package jmat.javapocs.embeddedzookeeperandkafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class App {

    public static final String MESSAGE = "Hello Kafka #";

    public static void main(final String[] args) {
        final SparkConf sparkConf = new SparkConf()
            .setAppName("Embedded Zookeeper and Kafka POC");

        try (final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf)) {
            final String topic = sparkContext.getConf().get("spark.jmat.javapocs.kafka.topic");
            final String endpoints = sparkContext.getConf().get("spark.jmat.javapocs.kafka.endpoints");

            final Properties configuration = new Properties();
            configuration.put(ProducerConfig.ACKS_CONFIG, "all");
            configuration.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, endpoints);
            configuration.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
            configuration.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

            final KafkaProducer<byte[], byte[]> localProducer = new KafkaProducer<>(configuration);

            localProducer.send(
                new ProducerRecord<>(topic, (MESSAGE + 0).getBytes()),
                (metadata, exception) -> {
                    throw new RuntimeException(
                        "An error occured sending to topic: " + topic + ", Meta data: " + metadata,
                        exception
                    );
                }
            );

            localProducer.flush();

            final JavaRDD<Integer> ints = sparkContext.parallelize(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

            ints.foreachPartition(iterator -> {
                final KafkaProducer<byte[], byte[]> remoteProducer = new KafkaProducer<>(configuration);

                while (iterator.hasNext()) {
                    final int i = iterator.next();
                    remoteProducer.send(
                        new ProducerRecord<>(topic, (MESSAGE + i).getBytes()),
                        (metadata, exception) -> {
                            System.out.printf(
                                "An error occured sending to topic: %s, Meta data: %s, Exception: %s",
                                topic, metadata, exception
                            );
                        }
                    );
                }

                remoteProducer.flush();
            });

            final JavaRDD<Integer> mappedInts = ints.mapPartitions(iterator -> {
                final ArrayList<Integer> processedInts = new ArrayList<>();
                final KafkaProducer<byte[], byte[]> remoteProducer = new KafkaProducer<>(configuration);

                while (iterator.hasNext()) {
                    final int i = iterator.next() + 10;
                    remoteProducer.send(
                        new ProducerRecord<>(topic, (MESSAGE + i).getBytes()),
                        (metadata, exception) -> {
                            System.out.printf(
                                "An error occured sending to topic: %s, Meta data: %s, Exception: %s",
                                topic, metadata, exception
                            );
                        }
                    );
                    processedInts.add(i);
                }
                remoteProducer.flush();

                return processedInts.iterator();
            });

            System.out.printf("%n%nMapped Ints: %s%n%n", mappedInts.collect());
        }
    }
}
