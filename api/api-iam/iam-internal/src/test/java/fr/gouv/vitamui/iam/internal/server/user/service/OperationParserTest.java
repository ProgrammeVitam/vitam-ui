package fr.gouv.vitamui.iam.internal.server.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.extension.ExtendWith;

class OperationParserTest {

    private final OperationParser operationParser = new OperationParser(new ObjectMapper(), new TranslateService());

    @ParameterizedTest
    @MethodSource("parseOldValuesParameters")
    void should_parse_old_values(String json, String expectedValue) {
        assertThat(operationParser.parseOldValues(json)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @MethodSource("parseNewValuesParameters")
    void should_parse_new_values(String json, String expectedValue) {
        assertThat(operationParser.parseNewValues(json)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @MethodSource("parseUserIdParameters")
    void should_parse_user_id(String evIdAppSession, String expectedValue) {
        assertThat(operationParser.parseUserId(evIdAppSession)).isEqualTo(expectedValue);
    }

    private static Stream<Arguments> parseOldValuesParameters() {
        return Stream.of(Arguments.of("{\"diff\":{\"-Otp\":\"oui\",\"+Otp\":\"non\"},\"Date d'opération\":\"2023-05-26T12:53:04.273\"}", "Otp:oui"), Arguments.of("{\"diff\":{\"-Nom\":\"-\",\"+Nom\":\"-\",\"-Email\":\"-\",\"+Email\":\"-\",\"-Nom de la rue\":\"-\",\"+Nom de la rue\":\"-\",\"-Code postal\":\"-\",\"+Code postal\":\"-\",\"-Ville\":\"-\",\"+Ville\":\"-\",\"-Pays\":\"-\",\"+Pays\":\"-\",\"-Numéro mobile\":\"-\",\"+Numéro mobile\":\"-\",\"-Numéro fixe\":\"-\",\"+Numéro fixe\":\"-\",\"-Statut\":\"inactif\",\"+Statut\":\"supprimé\",\"-Date de suppression\":\"\",\"+Date de suppression\":\"2023-06-06T12:04:17.510768+02:00\",\"-Groupe de profils\":\"135\",\"+Groupe de profils\":\"Optional.empty\",\"-Prénom\":\"-\",\"+Prénom\":\"-\",\"-Code du site\":\"\",\"+Code du site\":\"\",\"-Code interne\":\"\",\"+Code interne\":\"\"},\"Date d'opération\":\"2023-06-06T10:04:17.513\"}", "Nom:,Email:,Nom de la rue:,Code postal:,Ville:,Pays:,Numéro mobile:,Numéro fixe:,Statut:inactif,Date de suppression:,Groupe de profils:135,Prénom:,Code du site:,Code interne:"), Arguments.of("{\"Langue\":\"FRENCH\",\"Date d'opération\":\"2023-03-31T15:31:26.892\"}", ""));
    }

    private static Stream<Arguments> parseNewValuesParameters() {
        return Stream.of(Arguments.of("{\"diff\":{\"-Otp\":\"oui\",\"+Otp\":\"non\"},\"Date d'opération\":\"2023-05-26T12:53:04.273\"}", "Otp:non"), Arguments.of("{\"diff\":{\"-Nom\":\"-\",\"+Nom\":\"-\",\"-Email\":\"-\",\"+Email\":\"-\",\"-Nom de la rue\":\"-\",\"+Nom de la rue\":\"-\",\"-Code postal\":\"-\",\"+Code postal\":\"-\",\"-Ville\":\"-\",\"+Ville\":\"-\",\"-Pays\":\"-\",\"+Pays\":\"-\",\"-Numéro mobile\":\"-\",\"+Numéro mobile\":\"-\",\"-Numéro fixe\":\"-\",\"+Numéro fixe\":\"-\",\"-Statut\":\"inactif\",\"+Statut\":\"supprimé\",\"-Date de suppression\":\"\",\"+Date de suppression\":\"2023-06-06T12:04:17.510768+02:00\",\"-Groupe de profils\":\"135\",\"+Groupe de profils\":\"Optional.empty\",\"-Prénom\":\"-\",\"+Prénom\":\"-\",\"-Code du site\":\"\",\"+Code du site\":\"\",\"-Code interne\":\"\",\"+Code interne\":\"\"},\"Date d'opération\":\"2023-06-06T10:04:17.513\"}", "Nom:-,Email:-,Nom de la rue:-,Code postal:-,Ville:-,Pays:-,Numéro mobile:-,Numéro fixe:-,Statut:supprimé,Date de suppression:2023-06-06T12:04:17.51076802:00,Groupe de profils:Optional.empty,Prénom:-,Code du site:,Code interne:"));
    }

    private static Stream<Arguments> parseUserIdParameters() {
        return Stream.of(Arguments.of("CUSTOMERS_APP:02e97361-9ea0-403b-a208-ec7fc8672d73:Contexte UI Identity:101:-:1", "101"), Arguments.of("CUSTOMERS_APP:02e97361-9ea0-403b-a208-ec7fc8672d73:Contexte UI Identity", null), Arguments.of("CUSTOMERS_APP:02e97361-9ea0-403b-a208-ec7fc8672d73:Contexte UI Identity:101", "101"), Arguments.of("CUSTOMERS_APP", null), Arguments.of(null, null));
    }
}
