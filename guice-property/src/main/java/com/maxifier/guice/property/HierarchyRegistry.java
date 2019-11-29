package com.maxifier.guice.property;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class HierarchyRegistry implements Registry {
    private static final Logger logger = LoggerFactory.getLogger(HierarchyRegistry.class);

    public static final String DEFAULTS_FILE = "defaults";
    public static final String IMPORT_STATEMENT = "import";
    public static final String ENVIRONMENT_IMPORT_STATEMENT = "environment.import";
    public static final String PROPERTIES_SUFFIX = ".properties";

    private final Properties properties;
    private final Map<String, String> commandLineProperties;
    private final Multimap<String, String> systemToJavanamesMap;
    private final String confDir;

    public HierarchyRegistry(Properties properties, String confDir, Multimap<String, String> systemToJavanamesMap) {
        this.properties = properties;
        this.systemToJavanamesMap = systemToJavanamesMap;
        this.commandLineProperties = new HashMap<>();
        this.confDir = confDir;
    }

    public HierarchyRegistry(Properties properties, String confDir) {
        this(properties, confDir, ArrayListMultimap.create());
    }

    public String getConfDir() {
        return confDir;
    }

    public Properties getProperties() {
        return properties;
    }

    public Map<String, String> getCommandLineProperties() {
        return commandLineProperties;
    }

    public void overrideBySystem() {
        Properties systemProperties = System.getProperties();
        for (String key : systemProperties.stringPropertyNames()) {
            String value = systemProperties.getProperty(key);
            commandLineProperties.put(key, value);
        }
    }

    public void overrideByMap(Map<String, String> map) {
        commandLineProperties.putAll(map);
    }

    public Map<String, String> toMap() {
        Map<String, String> propertiesMap = new HashMap<>();
        for (String key : keys()) {
            propertiesMap.put(key, get(key));
        }
        return propertiesMap;
    }

    public Map<String, String> getCommandlineProperties() {
        Map<String, String> result = new HashMap<>();
        for (Object key : commandLineProperties.keySet()) {
            result.put((String) key, (String) commandLineProperties.get(key));
        }
        return result;
    }

    public void overrideByEnvironment() {
        //check override from Environment. Import all Environment variables
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            putProperty(commandLineProperties, entry.getKey(), entry.getValue(), "environment");
        }
    }

    @Override
    public Set<String> keys() {
        Set<String> strings = properties.stringPropertyNames();
        strings.addAll(commandLineProperties.keySet());
        strings.remove("import");
        return strings;
    }

    public void loadRootFile(String rootFileName) {
        File cfgFile = new File(rootFileName);
        if (!cfgFile.exists()) {
            return;
        }
        logger.info("Init Registry from root file: {}", rootFileName);
        try {
            try (FileInputStream in = new FileInputStream(cfgFile);
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                Properties properties = new Properties();
                properties.load(reader);
                for (String key : properties.stringPropertyNames()) {
                    putProperty(this.properties, key, properties.getProperty(key), "config file");
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File " + cfgFile.getAbsolutePath() + " not found", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadHierarchy(String environment, String fileName) throws IOException {
        String confDir = this.confDir + environment + "/";
        String nextEnvironment = loadFileAndGetNextEnvironment(confDir, fileName);
        if (nextEnvironment != null) {
            loadHierarchy(nextEnvironment, fileName);
        }
    }

    private void putProperty(Map properties, String key, String value, String sourceName) {
        if (value == null || value.isEmpty()) {
            return;
        }
        value = value.trim().replaceAll("^\"|\"$", "");
        if ("TBD".equals(value)) {
            return;
        }
        key = key.toLowerCase();
        if (systemToJavanamesMap.containsKey(key)) {
            for (String javaKeyName : systemToJavanamesMap.get(key)) {
                properties.put(javaKeyName, value);
                logger.info("Override by {}: {}({})={}", sourceName, javaKeyName, key, value);
            }
        } else {
            String javaKeyName = key
                    .replaceAll("___", "*")
                    .replaceAll("__", "-")
                    .replace('_', '.')
                    .replace('*', '_');
            properties.put(javaKeyName, value);
            logger.info("Override by {}: {}({})={}", sourceName, javaKeyName, key, value);
        }
    }

    @Override
    public String get(String key) {
        String commandValue = commandLineProperties.get(key);
        if (commandValue != null) {
            return commandValue;
        } else {
            return properties.getProperty(key);
        }
    }

    @Override
    public void set(String key, @Nullable String value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.setProperty(key, value);
        }
    }

    @Override
    public void store() {
    }

    @Nullable
    private String loadFileAndGetNextEnvironment(String confDir, String fileName) throws IOException {
        String fullFileName = confDir + fileName + PROPERTIES_SUFFIX;
        File file = new File(fullFileName);
        if (!file.exists()) {
            Preconditions.checkState(!DEFAULTS_FILE.equals(fileName),
                    "Has no " + DEFAULTS_FILE + " in " + new File(confDir).getAbsolutePath());
            return loadFileAndGetNextEnvironment(confDir, DEFAULTS_FILE);
        }
        logger.info("Loading resource: {}", fullFileName);
        String nextEnvironment = null;
        String nextFileName = DEFAULTS_FILE;
        Properties fileProperties = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            fileProperties.load(reader);
        }
        for (String key : fileProperties.stringPropertyNames()) {
            String value = fileProperties.getProperty(key);
            if (IMPORT_STATEMENT.equals(key)) {
                nextFileName = value;
            } else if (ENVIRONMENT_IMPORT_STATEMENT.equals(key)) {
                nextEnvironment = value;
            } else if (!properties.containsKey(key)) {
                properties.put(key, value);
            }
        }
        if (DEFAULTS_FILE.equals(fileName)) {
            return nextEnvironment;
        } else {
            return loadFileAndGetNextEnvironment(confDir, nextFileName);
        }
    }

}
