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
 * Le contrat publié doit décrire tout ce que le serveur expose.
 *
 * La spécification que consomme {@code iam-client} est tenue à la main — son pom porte d'ailleurs
 * toujours le {@code TODO: Swagger file should be generated during build}. Rien ne relie donc un
 * endpoint ajouté dans {@link CasController} à sa déclaration dans le fichier : l'endpoint existe côté
 * serveur, le client généré l'ignore, et le serveur d'authentification ne peut pas l'appeler sans que
 * quoi que ce soit échoue.
 *
 * Ce test est ce lien. Il ne vérifie pas la forme des schémas, seulement qu'aucune opération ne
 * manque — c'est la dérive qui coûte, et la seule qui passe aujourd'hui inaperçue.
 */
class CasContractPublicationTest {

    private static final String OPERATION_ID_PREFIX = "operationId:";

    private static final Path SPECIFICATION = Path.of("..", "iam-client", "src", "main", "resources", "swagger.yaml");

    @Test
    @DisplayName("chaque opération du contrôleur d'authentification figure dans la spécification publiée")
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
                "une opération exposée par CasController mais absente de swagger.yaml reste invisible du " +
                "client généré : le serveur d'authentification ne peut pas l'appeler"
            )
            .containsAll(declaredOperations);
    }

    /**
     * Les identifiants d'opération déclarés dans la spécification, comparés en entier — un identifiant
     * dont un autre est le préfixe ne doit pas passer pour lui.
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
