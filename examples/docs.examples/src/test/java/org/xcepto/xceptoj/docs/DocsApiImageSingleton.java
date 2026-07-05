package org.xcepto.xceptoj.docs;

import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DocsApiImageSingleton {

    private static ImageFromDockerfile image;
    private static final Object lock = new Object();

    public static ImageFromDockerfile getImage() {
        synchronized (lock) {
            if (image == null) {
                Path jarPath = Paths.get("").toAbsolutePath()
                        .getParent()
                        .resolve("docs.api/build/libs/docs.api.jar");

                image = new ImageFromDockerfile("xcepto-docs-api:test", false)
                        .withDockerfileFromBuilder(builder -> builder
                                .from("eclipse-temurin:21-jre")
                                .workDir("/app")
                                .copy("app.jar", "app.jar")
                                .entryPoint("java", "-jar", "app.jar")
                                .build())
                        .withFileFromPath("app.jar", jarPath);
            }
            return image;
        }
    }
}
