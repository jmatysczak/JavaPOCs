### Proof of concepts/examples/investigations in Java.

#### BitSetVsHashSetSizeAndSpeed
Compare the size and speed of BitSet, HashMap, and HashSet for a specific use case. Also demonstrates how to get the
actual size of an object graph as well as how to specify a Java Agent when running via Maven.

#### EmbeddedElasticsearch
Example of using an embedded Elasticsearch instance.

#### EmbeddedZookeeperAndKafka
Example of embedding Zookeeper and Kafka in an integration test. This also shows how to write to Kafka from Spark.

#### GuiceBindingOverriding
Two ways to override/replace an implementation in Guice.

#### JacksonJsonDeserialization
Various examples of deserializing JSON to Java objects.

#### JavaFinagleHTTP
Example of creating a HTTP client and server using Finagle.

#### JavaFinagleHTTPZK
Example of creating a HTTP client and server using Finagle where the client finds the server via Zookeeper.

#### JavaFinagleThrift
Example of creating a thrift client and server using Finagle.

#### JDKHttpServer
Example of using the HTTP server that comes with the JDK (com.sun.net.httpserver.HttpServer).

#### MongoSparkConnector
Example of how to unit/integration test the Mongo Spark Connector.

#### PrestoSqlParser
Example of using Facebook's Presto SQL parser to parse create table statements.

#### ServiceLoader
Example of how to use java.util.ServiceLoader to dynamicly load "service" classes that adhere to an interface.
<strong>ServiceInterfaceExample:</strong> Defines a service interface.<br/>
<strong>ServiceLoaderExample:</strong> Loads all services that implement the service interface and executes them.<br/>
<strong>ServiceProviderExample:</strong> Implements the service interface.<br/>

#### WalkFileTree
Example of various java.nio.file.Files file tree walking methods.
