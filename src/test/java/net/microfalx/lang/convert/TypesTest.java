package net.microfalx.lang.convert;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypesTest {

    @Test
    void zonedDateTime() {
        assertEquals(ZonedDateTime.parse("2023-09-03T04:00:00.000Z"),Types.from("2023-09-03T04:00:00.000Z", ZonedDateTime.class));
        assertEquals(ZonedDateTime.parse("2023-09-03T04:00:00.000Z"),Types.from("2023-09-03T04:00:00Z", ZonedDateTime.class));
        assertEquals(ZonedDateTime.parse("2023-09-03T04:00:00.000Z"),Types.from("2023-09-03T04:00Z", ZonedDateTime.class));
        assertEquals(ZonedDateTime.parse("2023-09-03T00:00-04:00[America/New_York]"),Types.from("2023-09-03", ZonedDateTime.class));
    }

    @Test
    void offsetDateTime() {
        assertEquals(OffsetDateTime.parse("2023-09-03T04:00:00.000Z"),Types.from("2023-09-03T04:00:00.000Z", OffsetDateTime.class));
        assertEquals(OffsetDateTime.parse("2023-09-03T04:00:00.000Z"),Types.from("2023-09-03T04:00:00Z", OffsetDateTime.class));
        assertEquals(OffsetDateTime.parse("2023-09-03T04:00:00.000Z"),Types.from("2023-09-03T04:00Z", OffsetDateTime.class));
        assertEquals(OffsetDateTime.parse("2023-09-03T00:00-04:00"),Types.from("2023-09-03", OffsetDateTime.class));
    }

    @Test
    void string() {
        assertEquals("",Types.from("", String.class));
        assertEquals("1",Types.from(1, String.class));
        assertEquals("1",Types.from(1L, String.class));
        assertEquals("[ \"1\", \"2\" ]",Types.from(new String[]{"1", "2"}, String.class));
        assertEquals("[ 1, 2 ]",Types.from(new int[]{1, 2}, String.class));
        assertEquals("[ \"a\", 1 ]",Types.from(Set.of(1, "a"), String.class));
        Assertions.assertThat(Types.from(Map.of("a", 1), String.class)).isEqualToNormalizingNewlines("""
                {
                  "a" : 1
                }""");
    }

    @Test
    void map() {
        Map<?, ?> map = Types.asMap("{}");
        assertEquals(0, map.size());
        map = Types.asMap(null);
        assertEquals(0, map.size());
        map = Types.asMap("");
        assertEquals(0, map.size());
        map = Types.asMap("{a:1, b :{c:1}}");
        assertEquals(2, map.size());
    }

    @Test
    void collection() {
        Collection<?> collection = Types.asCollection("[]");
        assertEquals(0, collection.size());
        collection = Types.asCollection(null);
        assertEquals(0, collection.size());
        collection = Types.asCollection("");
        assertEquals(0, collection.size());
        collection = Types.asCollection("['a',1]");
        assertEquals(2, collection.size());
    }

    @Test
    void set() {
        Set<?> set = Types.asSet("[]");
        assertEquals(0, set.size());
        set = Types.asSet(null);
        assertEquals(0, set.size());
        set = Types.asSet("");
        assertEquals(0, set.size());
        set = Types.asSet("['a',1, 1]");
        assertEquals(2, set.size());
    }

    @Test
    void asString() {
        String value = Types.asString(new Model1().setName("name1").setDescription("description1"));
        org.assertj.core.api.Assertions.assertThat(value).isEqualToNormalizingNewlines("""
                {
                  "name": "name1",
                  "description": "description1"
                }""");
        value = Types.asString(List.of(new Model1().setName("name1").setDescription("description1")));
        org.assertj.core.api.Assertions.assertThat(value).isEqualToNormalizingNewlines("""
                [
                  {
                    "name": "name1",
                    "description": "description1"
                  }
                ]""");
        value = Types.asString(new String[]{"a", "1"});
        org.assertj.core.api.Assertions.assertThat(value).isEqualToNormalizingNewlines("""
                [
                  "a",
                  "1"
                ]""");
        value = Types.asString(Map.of("a", 1));
        org.assertj.core.api.Assertions.assertThat(value).isEqualToNormalizingNewlines("""
                {
                  "a": 1
                }""");


    }

    @Test
    void asCollection() {
        Collection<Model1> models = Types.asCollection("[{\"name\":\"name1\",\"description\":\"description1\"},{\"name\":\"name2\",\"description\":\"description2\"}]",
                Model1.class);
        assertEquals(2, models.size());
        assertModel1(models.iterator().next());
    }

    @Test
    void asSet() {
        Set<Model1> models = Types.asSet("[{\"name\":\"name1\",\"description\":\"description1\"},{\"name\":\"name2\",\"description\":\"description2\"}]",
                Model1.class);
        assertEquals(2, models.size());
        assertModel2(models.iterator().next());
    }

    @Test
    void asObject() {
        Model1 model = Types.asObject("{\"name\":\"name1\",\"description\":\"description1\", dummy : 1}",
                Model1.class);
        assertModel1(model);
    }

    @Test
    void resource() {
        Model1 model = Types.asObject("{\"name\":\"name1\",\"description\":\"description1\", dummy : 1}",
                Model1.class);
        assertModel1(model);
    }

    private void assertModel1(Model1 model) {
        assertEquals("name1", model.getName());
        assertEquals("description1", model.getDescription());
    }

    private void assertModel2(Model1 model) {
        assertEquals("name2", model.getName());
        assertEquals("description2", model.getDescription());
    }

    @Getter
    @Setter
    @ToString
    static class Model1 {

        private String name;
        private String description;
    }

}