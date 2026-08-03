package fr.gouv.vitamui.cas.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that every configuration key this module ships still designates a property that something on the
 * classpath declares.
 *
 * <p>CAS renames configuration properties freely between minor versions, and a key that no longer binds is simply
 * ignored: the server starts, and the behaviour silently reverts to the default. The authoritative list of valid
 * keys is the {@code spring-configuration-metadata.json} that CAS, Spring Boot and Spring Cloud ship inside their
 * jars, so that is what this test reads.
 *
 * <p>Only the files that live in this module are covered. The production configuration is an Ansible template,
 * {@code deployment/roles/vitamui/templates/cas-server/application.yml.j2}, and it holds keys that are not
 * expressed here — it has to be reviewed by hand when CAS is upgraded.
 */
public class ConfigurationKeysTest {

    /** Prefixes worth checking: everything else belongs to VitamUI or to the theme. */
    private static final Set<String> CHECKED_PREFIXES = Set.of("cas", "spring", "server", "management", "logging");

    private static final Pattern INDEX = Pattern.compile("\\[\\d+]");
    private static final Pattern CAMEL_BOUNDARY = Pattern.compile("(?<!^)(?=[A-Z])");

    /**
     * Keys nothing declares, and that were already inert before the CAS 7.3.8 upgrade.
     *
     * <p>{@code spring.cloud.bus.enabled} guards against a spring-cloud-bus that is not on the classpath.
     * {@code management.health.memoryHealthIndicator.enabled} names a health indicator Spring Boot does not have.
     * {@code server.host} is not a Spring property at all — the address setting is {@code server.address}.
     *
     * <p>{@code management.monitor.endpoints.endpoint.defaults.access} is different: it is a CAS key that ended up
     * nested under {@code management} in application-recette.yml, so it binds to nothing and the recette
     * environment falls back to the {@code AUTHENTICATED} set in application.properties. Moving it under
     * {@code cas} would restore what was meant — and open those endpoints — so the decision belongs to whoever
     * owns that environment rather than to this upgrade.
     *
     * <p>They are recorded rather than removed so the test stays a faithful description of the files. Removing
     * them is a separate cleanup.
     */
    private static final Set<String> KNOWN_INERT = Set.of(
        "spring.cloud.bus.enabled",
        "management.health.memoryHealthIndicator.enabled",
        "server.host",
        "management.monitor.endpoints.endpoint.defaults.access"
    );

    @Test
    public void everyConfigurationKeyIsDeclaredBySomethingOnTheClasspath() throws Exception {
        final Metadata metadata = Metadata.fromClasspath();
        final TreeMap<String, String> unknown = new TreeMap<>();

        for (final Path file : configurationFiles()) {
            final Map<String, Integer> keys = file.toString().endsWith(".properties")
                ? readProperties(file)
                : readYaml(file);
            keys.forEach((key, line) -> {
                if (!CHECKED_PREFIXES.contains(key.split("\\.")[0])) {
                    return;
                }
                if (KNOWN_INERT.contains(key) || metadata.declares(key)) {
                    return;
                }
                unknown.put(file.getFileName() + ":" + line + "  " + key, key);
            });
        }

        assertThat(unknown.keySet())
            .as(
                "Configuration keys nothing on the classpath declares. A key that does not bind is ignored in " +
                "silence and the setting reverts to its default."
            )
            .isEmpty();
    }

    private static List<Path> configurationFiles() throws IOException {
        final List<Path> files = new ArrayList<>();
        files.add(Path.of("src/main/resources/application.properties"));
        try (var stream = Files.list(Path.of("src/main/config"))) {
            stream.filter(p -> p.getFileName().toString().endsWith(".yml")).sorted().forEach(files::add);
        }
        return files;
    }

    private static Map<String, Integer> readProperties(final Path file) throws IOException {
        final Map<String, Integer> keys = new LinkedHashMap<>();
        final List<String> lines = Files.readAllLines(file);
        final Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            properties.load(in);
        }
        properties.stringPropertyNames().forEach(name -> keys.put(name, lineOf(lines, name)));
        return keys;
    }

    private static int lineOf(final List<String> lines, final String key) {
        for (int i = 0; i < lines.size(); i++) {
            final String line = lines.get(i).trim();
            if (!line.startsWith("#") && line.startsWith(key) && line.substring(key.length()).startsWith("=")) {
                return i + 1;
            }
        }
        return 0;
    }

    /** Flattens every document of a YAML file into dotted keys. Line numbers are not tracked here. */
    private static Map<String, Integer> readYaml(final Path file) throws IOException {
        final Map<String, Integer> keys = new LinkedHashMap<>();
        try (InputStream in = Files.newInputStream(file)) {
            for (final Object document : new Yaml().loadAll(in)) {
                if (document instanceof Map<?, ?> map) {
                    flatten("", map, keys);
                }
            }
        }
        return keys;
    }

    private static void flatten(final String prefix, final Map<?, ?> map, final Map<String, Integer> out) {
        map.forEach((rawKey, value) -> {
            final String key = prefix.isEmpty() ? String.valueOf(rawKey) : prefix + '.' + rawKey;
            if (value instanceof Map<?, ?> nested) {
                flatten(key, nested, out);
            } else {
                out.put(key, 0);
            }
        });
    }

    /** The union of every spring-configuration-metadata.json reachable on the classpath. */
    private record Metadata(Set<String> names, Set<String> containers, List<Pattern> templated) {
        static Metadata fromClasspath() throws IOException {
            final Set<String> names = new TreeSet<>();
            final Set<String> containers = new TreeSet<>();
            final List<Pattern> templated = new ArrayList<>();
            final ObjectMapper mapper = new ObjectMapper();

            final Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath*:META-INF/spring-configuration-metadata.json");
            for (final Resource resource : resources) {
                final JsonNode root;
                try (InputStream in = resource.getInputStream()) {
                    root = mapper.readTree(in);
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
                for (final JsonNode property : root.path("properties")) {
                    final String name = property.path("name").asText();
                    if (name.isEmpty()) {
                        continue;
                    }
                    if (name.contains("[key]")) {
                        templated.add(
                            Pattern.compile('^' + Pattern.quote(normalise(name)).replace("[key]", "\\E[^.]+\\Q") + '$')
                        );
                        continue;
                    }
                    names.add(normalise(name));
                    final String type = property.path("type").asText();
                    if (type.startsWith("java.util.Map") || type.startsWith("java.util.List")) {
                        containers.add(normalise(name));
                    }
                }
            }
            return new Metadata(names, containers, templated);
        }

        boolean declares(final String key) {
            final String candidate = normalise(key);
            if (names.contains(candidate)) {
                return true;
            }
            if (templated.stream().anyMatch(pattern -> pattern.matcher(candidate).matches())) {
                return true;
            }
            // Only Map- and List-typed properties may carry arbitrary keys underneath.
            return containers.stream().anyMatch(container -> candidate.startsWith(container + '.'));
        }

        /** Spring's relaxed binding accepts camelCase and indices; the metadata is kebab-case without them. */
        private static String normalise(final String key) {
            final String withoutIndices = INDEX.matcher(key).replaceAll("");
            final StringBuilder result = new StringBuilder();
            final String[] segments = withoutIndices.split("\\.", -1);
            for (int i = 0; i < segments.length; i++) {
                if (i > 0) {
                    result.append('.');
                }
                result.append(CAMEL_BOUNDARY.matcher(segments[i]).replaceAll("-").toLowerCase());
            }
            return result.toString();
        }
    }
}
