package com.maxifier.guice.property;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public interface PropertyProvider {
    String get(String key);

    Set<String> keys();

    void set(String key, String value);

    void store();

    default int getInt(String key, int defaultValue) {
        String s = get(key);
        return s != null ? Integer.parseInt(s) : defaultValue;
    }

    default double getDouble(String key, double defaultValue) {
        String s = get(key);
        return s != null ? Double.parseDouble(s) : defaultValue;
    }

    default boolean getBoolean(String key, boolean defaultValue) {
        final String v = get(key);
        if (v == null) {
            return defaultValue;
        }
        return v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes");
    }

    default String get(String key, String defaultValue)  {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    default int getInt(String key)  {
        return Integer.parseInt(get(key));
    }

    default double getDouble(String key) {
        return Double.parseDouble(get(key));
    }

    default boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    default Map<String, String> getByMask(String mask) {
        if (!mask.endsWith(".")) {
            mask += ".";
        }
        final Map<String, String> result = new HashMap<>();
        for (String name : keys()) {
            if (name.startsWith(mask)) {
                result.put(name, get(name));
            }
        }
        return result;
    }

    default boolean hasKey(String key) {
        return get(key) != null;
    }

    default void set(final Map<String, String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    default void set(Properties properties) {
        for (Object name : properties.keySet()) {
            if (name != null) {
                String key = name.toString();
                set(key, get(key));
            }
        }
    }

}
