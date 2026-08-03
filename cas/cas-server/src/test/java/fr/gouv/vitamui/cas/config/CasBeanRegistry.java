package fr.gouv.vitamui.cas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Index of the bean names declared through {@code @Bean} methods, built by reading class metadata with ASM.
 *
 * <p>No class is loaded and no application context is started: this only reads the bytecode that is on the test
 * classpath. It exists so that the tests in this package can answer one question cheaply and deterministically —
 * <em>does Apereo CAS still declare a bean under this exact name?</em>
 *
 * <p>That question matters because {@code AppConfig}, {@code WebConfig} and {@code WebflowConfig} replace roughly
 * thirty CAS beans <em>by name</em>, under {@code spring.main.allow-bean-definition-overriding=true}. When a CAS
 * upgrade renames one of them, the VitamUI definition stops replacing anything and is silently registered
 * alongside the original — no error, and the CAS behaviour we meant to neutralise comes back.
 */
public final class CasBeanRegistry {

    private static final String BEAN_ANNOTATION = Bean.class.getName();

    private static final String CAS_CLASSES = "classpath*:org/apereo/cas/**/*.class";
    private static final String VITAMUI_CONFIG_CLASSES = "classpath*:fr/gouv/vitamui/cas/config/*.class";

    /** Bean name -&gt; declared return type(s) of the CAS {@code @Bean} methods producing it. */
    private static final Map<String, Set<String>> CAS_BEANS = scan(CAS_CLASSES);

    /** Bean name -&gt; declared return type(s) of the VitamUI {@code @Bean} methods producing it. */
    private static final Map<String, Set<String>> VITAMUI_BEANS = scan(VITAMUI_CONFIG_CLASSES);

    private CasBeanRegistry() {}

    public static boolean casDeclares(final String beanName) {
        return CAS_BEANS.containsKey(beanName);
    }

    public static Set<String> casBeanNames() {
        return Collections.unmodifiableSet(CAS_BEANS.keySet());
    }

    public static Set<String> vitamuiBeanNames() {
        return Collections.unmodifiableSet(VITAMUI_BEANS.keySet());
    }

    /** Return types CAS declares for {@code beanName}; empty when CAS does not declare it at all. */
    public static Set<String> casReturnTypes(final String beanName) {
        return Collections.unmodifiableSet(CAS_BEANS.getOrDefault(beanName, Set.of()));
    }

    /** Return types VitamUI declares for {@code beanName}; empty when VitamUI does not declare it at all. */
    public static Set<String> vitamuiReturnTypes(final String beanName) {
        return Collections.unmodifiableSet(VITAMUI_BEANS.getOrDefault(beanName, Set.of()));
    }

    /** Names declared on both sides, i.e. the beans VitamUI actually takes over from CAS. */
    public static Set<String> overriddenBeanNames() {
        final Set<String> intersection = new TreeSet<>(VITAMUI_BEANS.keySet());
        intersection.retainAll(CAS_BEANS.keySet());
        return intersection;
    }

    private static Map<String, Set<String>> scan(final String locationPattern) {
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        final MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);
        final Map<String, Set<String>> index = new TreeMap<>();

        final Resource[] resources;
        try {
            resources = resolver.getResources(locationPattern);
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot scan the classpath for " + locationPattern, e);
        }

        for (final Resource resource : resources) {
            if (!resource.isReadable()) {
                continue;
            }
            final MetadataReader reader;
            try {
                reader = readerFactory.getMetadataReader(resource);
            } catch (final IOException | RuntimeException | LinkageError e) {
                // Not every entry under these packages is a class file we can parse (module-info, newer bytecode
                // levels, resources renamed to .class). Those cannot declare beans, so skipping them is safe.
                continue;
            }
            for (final MethodMetadata method : reader.getAnnotationMetadata().getAnnotatedMethods(BEAN_ANNOTATION)) {
                for (final String beanName : beanNamesOf(method)) {
                    index.computeIfAbsent(beanName, key -> new TreeSet<>()).add(method.getReturnTypeName());
                }
            }
        }
        return index;
    }

    /**
     * Bean names produced by a {@code @Bean} method: the explicit {@code name}/{@code value} attribute when set
     * (a method may register several aliases), the method name otherwise.
     */
    private static Set<String> beanNamesOf(final MethodMetadata method) {
        final Map<String, Object> attributes = method.getAnnotationAttributes(BEAN_ANNOTATION);
        if (attributes != null) {
            final Object declared = attributes.get("name");
            if (declared instanceof String[] names && names.length > 0) {
                return new LinkedHashSet<>(Arrays.asList(names));
            }
        }
        return Set.of(method.getMethodName());
    }
}
