package fr.gouv.vitamui.iam.server.rest;

import io.swagger.v3.oas.annotations.Operation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The published contract must describe everything the server exposes.
 *
 * The specification {@code iam-client} consumes is maintained by hand — its pom still carries the
 * {@code TODO: Swagger file should be generated during build}. Nothing therefore ties an endpoint added
 * to {@link CasController} to its declaration in the file: the endpoint exists on the server, the
 * generated client ignores it, and the authentication server cannot call it, without anything failing.
 *
 * This test is that link. It does not check the shape of the schemas, only that no operation is
 * missing — that is the drift that costs, and the only one that currently goes unnoticed.
 */
class CasContractPublicationTest {

    private static final String OPERATION_ID_PREFIX = "operationId:";

    private static final Path SPECIFICATION = Path.of("..", "iam-client", "src", "main", "resources", "swagger.yaml");

    @Test
    @DisplayName("every operation of the authentication controller appears in the published specification")
    void everyOperationIsPublished() throws IOException {
        final List<String> declaredOperations = Arrays.stream(CasController.class.getDeclaredMethods())
            .map(method -> method.getAnnotation(Operation.class))
            .filter(Objects::nonNull)
            .map(Operation::operationId)
            .filter(operationId -> !operationId.isBlank())
            .sorted()
            .toList();

        assertThat(declaredOperations).isNotEmpty();
        assertThat(publishedOperations())
            .as(
                "an operation exposed by CasController but missing from swagger.yaml stays invisible to the " +
                "generated client: the authentication server cannot call it"
            )
            .containsAll(declaredOperations);
    }

    /**
     * The operation identifiers declared in the specification, compared whole — an identifier that is a
     * prefix of another must not pass for it.
     */
    private Set<String> publishedOperations() throws IOException {
        try (var lines = Files.lines(SPECIFICATION)) {
            return lines
                .map(String::trim)
                .filter(line -> line.startsWith(OPERATION_ID_PREFIX))
                .map(line -> line.substring(OPERATION_ID_PREFIX.length()).trim())
                .collect(Collectors.toSet());
        }
    }
}
