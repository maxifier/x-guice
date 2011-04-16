package com.magenta.guice.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ResourceUtils {

    private static final Logger log = LoggerFactory.getLogger(ResourceUtils.class);

    public static final String CLASSPATH_PREFIX = "classpath://";
    public static final String URL_PREFIX = "url://";
    public static final String FILE_PREFIX = "file://";


    private ResourceUtils() {
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static boolean hasResourcePrefix(String resourcePath) {
        return resourcePath != null &&
                (resourcePath.startsWith(CLASSPATH_PREFIX) ||
                        resourcePath.startsWith(URL_PREFIX) ||
                        resourcePath.startsWith(FILE_PREFIX));
    }


    public static InputStream getInputStreamForPath(String resourcePath) throws IOException {
        InputStream is;
        if (resourcePath.startsWith(CLASSPATH_PREFIX)) {
            is = loadFromClassPath(stripPrefix(resourcePath));

        } else if (resourcePath.startsWith(URL_PREFIX)) {
            is = loadFromUrl(stripPrefix(resourcePath));

        } else if (resourcePath.startsWith(FILE_PREFIX)) {
            is = loadFromFile(stripPrefix(resourcePath));

        } else {
            is = loadFromFile(resourcePath);
        }
        if (is == null) {
            throw new IOException("Resource [" + resourcePath + "] could not be found.");
        }
        return is;
    }

    private static InputStream loadFromFile(String path) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Opening file [" + path + "]...");
        }
        return new FileInputStream(path);
    }

    private static InputStream loadFromUrl(String urlPath) throws IOException {
        log.debug("Opening url {}", urlPath);
        URL url = new URL(urlPath);
        return url.openStream();
    }

    private static InputStream loadFromClassPath(String path) {
        log.debug("Opening resource from class path [{}]", path);
        return ClassUtils.getResourceAsStream(path);
    }

    private static String stripPrefix(String resourcePath) {
        return resourcePath.substring(resourcePath.indexOf("://") + 3);
    }


    public static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                log.warn("Error closing input stream.", e);
            }
        }
    }
}

