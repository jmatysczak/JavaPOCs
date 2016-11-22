package jmat.javapocs.embeddedzookeeperandkafka;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;
import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.FetchResponse;
import kafka.consumer.SimpleConsumer;
import kafka.message.ByteBufferMessageSet;
import kafka.message.MessageAndOffset;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.commons.io.FileUtils;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.runtime.AbstractFunction1;

public class AppTest {

    private static final int KAFKA_PORT = 3333;
    private static final int ZOOKEEPER_PORT = 3334;

    private static final String KAFKA_TOPIC = "messages";

    private TestingServer zookeeperServer;
    private KafkaServerStartable kafkaServer;

    @Before
    public void setUp() throws Exception {
        this.setUpSystemProperties();
        this.setUpZookeeper();
        this.setUpKafka();
    }

    private void setUpSystemProperties() {
        System.setProperty("spark.master", "local[*]");
        System.setProperty("spark.jmat.javapocs.kafka.topic", KAFKA_TOPIC);
        System.setProperty("spark.jmat.javapocs.kafka.endpoints", "localhost:" + KAFKA_PORT);
    }

    private void setUpZookeeper() throws Exception {
        this.zookeeperServer = new TestingServer(ZOOKEEPER_PORT);
    }

    private void setUpKafka() throws Exception {
        final Path kafkaLogDirectory = Files.createTempDirectory("kafka-logs");

        final Properties kafkaConfig = new Properties();
        kafkaConfig.put("port", String.valueOf(KAFKA_PORT));
        kafkaConfig.put("log.dirs", kafkaLogDirectory.toAbsolutePath().toString());
        kafkaConfig.put("zookeeper.connect", this.zookeeperServer.getConnectString());
        this.kafkaServer = new KafkaServerStartable(new KafkaConfig(kafkaConfig));
        this.kafkaServer.startup();

        final ZkClient zkClient = new ZkClient(this.zookeeperServer.getConnectString(), 15 * 1000, 10 * 1000, ZKStringSerializer$.MODULE$);
        final ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(this.zookeeperServer.getConnectString()), false);
        AdminUtils.createTopic(zkUtils, KAFKA_TOPIC, 1, 1, new Properties(), RackAwareMode.Disabled$.MODULE$);
    }

    @After
    public void tearDown() throws Exception {
        if (this.kafkaServer != null) {
            this.kafkaServer.shutdown();

            this.kafkaServer.serverConfig().logDirs().foreach(new AbstractFunction1<String, String>() {
                @Override
                public String apply(final String logDirectory) {
                    try {
                        FileUtils.deleteDirectory(new File(logDirectory));
                    } catch (final Exception ex) {
                        System.out.println(ex);
                    }
                    return "";
                }
            });
        }

        if (this.zookeeperServer != null) {
            this.zookeeperServer.close();
        }
    }

    @Test
    public void kafka_can_be_written_to() {
        App.main(null);

        this.ensure_messages_are_written();
    }

    private void ensure_messages_are_written() {
        final ArrayList<byte[]> messages = this.getMessagesFromKafka(KAFKA_TOPIC);

        Assert.assertEquals("The number of messages is not correct.", 21, messages.size());

        for (final byte[] message : messages) {
            Assert.assertEquals("The message is not correct.", App.MESSAGE, new String(message).substring(0, App.MESSAGE.length()));
        }
    }

    private ArrayList<byte[]> getMessagesFromKafka(final String topic) {
        final SimpleConsumer consumer = new SimpleConsumer("localhost", KAFKA_PORT, 100000, 1024 * 1024, "test-client");

        try {
            final FetchRequest request = new FetchRequestBuilder()
                .clientId("test-client")
                .addFetch(topic, 0, 0, 10000000)
                .build();
            final FetchResponse response = consumer.fetch(request);

            Assert.assertFalse("Could not retrieve messages from Kafka.", response.hasError());

            final ArrayList<byte[]> messages = new ArrayList<>();

            final ByteBufferMessageSet messageSet = response.messageSet(topic, 0);
            messageSet.foreach(new AbstractFunction1<MessageAndOffset, String>() {
                @Override
                public String apply(final MessageAndOffset messageAndOffset) {
                    final ByteBuffer payload = messageAndOffset.message().payload();
                    final byte[] message = new byte[payload.limit()];
                    payload.get(message);
                    messages.add(message);
                    return "";
                }
            });

            return messages;
        } finally {
            consumer.close();
        }
    }
}
