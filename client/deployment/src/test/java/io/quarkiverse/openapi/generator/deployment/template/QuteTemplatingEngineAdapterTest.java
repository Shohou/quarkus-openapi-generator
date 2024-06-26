package io.quarkiverse.openapi.generator.deployment.template;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;

import io.quarkiverse.openapi.generator.deployment.wrapper.QuarkusCodegenConfigurator;

public class QuteTemplatingEngineAdapterTest {
    @Test
    void checkTemplateGenerator() throws IOException {
        final String petstoreOpenApi = requireNonNull(this.getClass().getResource("/openapi/petstore-openapi.json")).getPath();
        // getResource puts a leading slash before the disk name on Windows, thus we wrap it into a file object
        final File petstoreOpenApiFile = new File(petstoreOpenApi);
        final DefaultGenerator generator = new DefaultGenerator();
        final CodegenConfigurator configurator = new QuarkusCodegenConfigurator();
        final File apiFile = File.createTempFile("api", ".java");
        apiFile.deleteOnExit();
        configurator.setInputSpec(petstoreOpenApiFile.getAbsolutePath());
        generator.opts(configurator.toClientOptInput());

        final File writtenFile = generator.getTemplateProcessor().write(Collections.singletonMap("name", "Jack"), "hello.qute",
                apiFile);
        if (writtenFile != null) {
            writtenFile.deleteOnExit();
            assertEquals("Hello! My name is Jack", new String(Files.readAllBytes(writtenFile.toPath())));
        } else {
            fail("Template failed to write to the file");
        }
    }

    @Test
    @EnabledOnOs({ OS.WINDOWS })
    void checkOpenApiSpecIsNotAvailableOnWindows() throws IOException {
        // On Windows getResource prepends a leading slash which leads to an invalid file path
        final String petstoreOpenApi = requireNonNull(this.getClass().getResource("/openapi/petstore-openapi.json"))
                .getPath();
        final DefaultGenerator generator = new DefaultGenerator();
        final CodegenConfigurator configurator = new QuarkusCodegenConfigurator();
        final File apiFile = File.createTempFile("api", ".java");
        apiFile.deleteOnExit();
        configurator.setInputSpec(petstoreOpenApi);
        // skip message evaluation as the NPE doesn't have a message in all scenarios, seems to be JDK dependent
        assertThrows(NullPointerException.class,
                () -> generator.opts(configurator.toClientOptInput()));
    }
}
