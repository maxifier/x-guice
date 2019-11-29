package com.maxifier.guice.property;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class HierarchyRegistryTest {

    private static final String CONF_DIR = "src/test/resources/data/conf/";

    @DataProvider(name = "files")
    public Object[][] getFiles() {
        return new Object[][]{
                {"defaults", "defaults", ImmutableMap.of("dd","ddvalue", "d1","d1base", "d2","d2base", "od","odbase", "o1","o1base")},
                {"defaults", "client1", ImmutableMap.of("dd","ddvalue", "d1","d1value", "d2","d2base", "od","odbase", "o1","o1base")},
                {"defaults", "client2", ImmutableMap.of("dd","ddvalue", "d1","d1base", "d2","d2value", "od","odbase", "o1","o1base")},
                {"override", "defaults", ImmutableMap.of("dd","ddvalue", "d1","d1base", "d2","d2base", "od","odvalue", "o1","o1base")},
                {"override", "client1", ImmutableMap.of("dd","ddvalue", "d1","d1value", "d2","d2base", "od","odvalue", "o1","o1value")},
                {"override", "client2", ImmutableMap.of("dd","ddvalue", "d1","d1base", "d2","d2value", "od","odvalue", "o1","o1base")},
        };
    }

    @Test(dataProvider = "files")
    public void testFile(String environment, String client, Map<String, String> expectedValues) throws IOException {
        Properties properties = new Properties();
        new HierarchyRegistry(properties, CONF_DIR, ArrayListMultimap.create()).
                loadHierarchy(environment, client);
        for (Map.Entry<String, String> expectedValue : expectedValues.entrySet()) {
            Assert.assertEquals(properties.getProperty(expectedValue.getKey()), expectedValue.getValue());
        }
    }

    @Test
    public void testRegistry() throws Exception {
        System.setProperty("from.system", "svalue");
        System.setProperty("from.consul", "System value override Consul value");

        HierarchyRegistry registry = new HierarchyRegistry(
                new Properties(), CONF_DIR, ArrayListMultimap.create());
        registry.overrideBySystem();
        registry.overrideByMap(ImmutableMap.of("from.consul", "cvalue"));
        registry.loadRootFile(CONF_DIR + "root.properties");
        String environment = registry.getOrDefault("environment", "local");
        String client = registry.getOrDefault("client", registry.get("import"));
        registry.loadHierarchy(environment, client);

        Assert.assertEquals(registry.get("dd"),"ddvalue");
        Assert.assertEquals(registry.get("d1"),"d1value");
        Assert.assertEquals(registry.get("d2"),"d2base");
        Assert.assertEquals(registry.get("od"),"odvalue");
        Assert.assertEquals(registry.get("o1"),"o1value");

        Assert.assertEquals(registry.get("from.system"),"svalue");
        Assert.assertEquals(registry.get("from.consul"),"cvalue");
        Assert.assertEquals(registry.get("from.root"),"rvalue");
        Assert.assertEquals(registry.get("from-root.two"),"rvalue2");
        Assert.assertEquals(registry.get("from.root_tbd"),"base", "TBD override value");
        Assert.assertEquals(registry.get("hibernate.connection.username"),"hvalue");
        Assert.assertEquals(registry.get("hibernate.connection.password"),"base", "TBD override value");
    }

}
