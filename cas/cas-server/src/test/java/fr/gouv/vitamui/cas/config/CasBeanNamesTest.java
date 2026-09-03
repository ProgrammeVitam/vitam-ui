package fr.gouv.vitamui.cas.config;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that every bean name written as a string literal still designates a bean that exists.
 *
 * <p>Two families of literals are covered:
 *
 * <ul>
 *   <li>{@link CasBeans} — about thirty CAS bean names injected by {@code @Qualifier}. Roughly half are plain
 *       strings, with no compiler help: {@code WebflowConfig} alone rebuilds the CAS authentication resolver chain
 *       from fourteen of them. A qualifier pointing at a name CAS no longer declares fails at context startup, in
 *       production, not at build time.
 *   <li>The action bean names passed to {@code createActionState} / {@code createEvaluateAction} by the webflow
 *       configurers. These are resolved lazily, when a user walks through that part of the flow — a typo or a
 *       rename in {@code WebflowConfig} stays invisible until someone hits the state.
 * </ul>
 */
public class CasBeanNamesTest {

    /**
     * CAS bean names that CAS does not declare through a plain {@code @Bean} method, so {@link CasBeanRegistry}
     * cannot see them (both are present in the CAS jars, registered another way).
     *
     * <p>Both are derived from CAS constants — {@code LogoutManager.DEFAULT_BEAN_NAME} and
     * {@code SingleLogoutRequestExecutor.BEAN_NAME} — so a rename breaks the compilation instead of the runtime.
     * That is why leaving them unchecked here is acceptable.
     */
    private static final Set<String> NOT_DECLARED_VIA_BEAN_METHOD = Set.of(
        "logoutManager",
        "defaultSingleLogoutRequestExecutor"
    );

    /** Action beans referenced by name from {@code VitamLoginWebflowConfigurer} and {@code VitamMfaWebflowConfigurer}. */
    private static final List<String> WEBFLOW_ACTION_BEANS = List.of(
        "checkSubrogationAction",
        "triggerChangePasswordAction",
        "dispatcherAction",
        "listCustomersAction",
        "customerSelectedAction",
        "checkMfaTokenAction"
    );

    @Test
    public void everyQualifierConstantDesignatesAnExistingBean() throws Exception {
        final TreeMap<String, String> unresolved = new TreeMap<>();

        for (final Field field : CasBeans.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != String.class) {
                continue;
            }
            field.setAccessible(true);
            final String beanName = (String) field.get(null);
            final boolean known =
                CasBeanRegistry.casDeclares(beanName) ||
                CasBeanRegistry.vitamuiBeanNames().contains(beanName) ||
                NOT_DECLARED_VIA_BEAN_METHOD.contains(beanName);
            if (!known) {
                unresolved.put(field.getName(), beanName);
            }
        }

        assertThat(unresolved)
            .as(
                "CasBeans constants pointing at a bean nobody declares any more. Every one of them is a " +
                "@Qualifier that will fail at context startup."
            )
            .isEmpty();
    }

    @Test
    public void everyWebflowActionBeanIsDeclared() {
        for (final String beanName : WEBFLOW_ACTION_BEANS) {
            assertThat(CasBeanRegistry.vitamuiBeanNames().contains(beanName) || CasBeanRegistry.casDeclares(beanName))
                .as(
                    "The webflow configurers wire the action state '%s' by name, but no @Bean declares it. The " +
                    "flow would fail when a user reaches that state.",
                    beanName
                )
                .isTrue();
        }
    }
}
