package net.microfalx.lang.convert;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypesTest {

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