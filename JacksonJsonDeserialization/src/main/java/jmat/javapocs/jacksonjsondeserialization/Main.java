package jmat.javapocs.jacksonjsondeserialization;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static final String JSON = ("{"
        + "'type':'Some Type',"
        + "'sub-type':{'type':'Some Sub Type'}"
        + "}").replace('\'', '"');

    public static void main(final String[] args) throws Exception {
        final ObjectMapper parser = new ObjectMapper();

        final Object subClassWithADeserializer = parser.readValue(JSON, SubClassWithADeserializer.class);
        System.out.println(subClassWithADeserializer);

        final Object subClassWithAnnotations = parser.readValue(JSON, SubClassWithAnnotations.class);
        System.out.println(subClassWithAnnotations);

        final Object classWithDynamicProperties = parser.readValue(JSON, ClassWithDynamicProperties.class);
        System.out.println(classWithDynamicProperties);
    }

    private static class BaseClassWithNoKnowldgeOfJackson {

        private final String type;

        public BaseClassWithNoKnowldgeOfJackson(final String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "BaseClassWithNoKnowldgeOfJackson{type=" + this.type + "}";
        }
    }

    @JsonDeserialize(using = SubClassDeserializer.class)
    private static class SubClassWithADeserializer extends BaseClassWithNoKnowldgeOfJackson {

        public SubClassWithADeserializer(final String type) {
            super(type);
        }

        @Override
        public String toString() {
            return super.toString() + " (SubClassWithADeserializer)";
        }
    }

    private static class SubClassDeserializer extends JsonDeserializer<SubClassWithADeserializer> {

        @Override
        public SubClassWithADeserializer deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
            final ObjectCodec oc = parser.getCodec();
            final JsonNode node = oc.readTree(parser);
            return new SubClassWithADeserializer(node.get("type").textValue());
        }
    }

    private static class SubClassWithAnnotations extends BaseClassWithNoKnowldgeOfJackson {

        private final SubType subType;

        public SubClassWithAnnotations(
            @JsonProperty("type") final String type,
            @JsonProperty("sub-type") final SubType subType
        ) {
            super(type);
            this.subType = subType;
        }

        @Override
        public String toString() {
            return super.toString() + " (SubClassWithAnnotations) " + this.subType;
        }
    }

    private static class SubType {

        private final String type;

        public SubType(@JsonProperty("type") final String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "SubType{type=" + this.type + "}";
        }
    }

    private static class ClassWithDynamicProperties {

        private final String type;
        private final Map<String, Object> dynamicProperties = new HashMap<>();

        public ClassWithDynamicProperties(@JsonProperty("type") final String type) {
            this.type = type;
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return this.dynamicProperties;
        }

        @JsonAnySetter
        public void set(final String name, final Object value) {
            this.dynamicProperties.put(name, value);
        }

        @Override
        public String toString() {
            return "ClassWithDynamicProperties{type=" + this.type + ", dynamicProperties=" + this.dynamicProperties + "}";
        }
    }
}
