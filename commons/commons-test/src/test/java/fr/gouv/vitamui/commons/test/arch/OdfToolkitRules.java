package fr.gouv.vitamui.commons.test.arch;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.odftoolkit.simple.table.Cell;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class OdfToolkitRules {

    /**
     * Forbid usage of Cell#setStringValue().
     *
     * Reason:
     * - This method indirectly triggers AWT via Cell.optimizeCellSize().
     * - In environments without system fonts (e.g. minimal Linux / no fontconfig),
     *   this leads to runtime failures:
     *   "Fontconfig head is null, check your fonts or fonts configuration".
     *
     * Recommended alternative:
     * - Use Cell#setCellText(), which writes directly to the ODT DOM
     *   and does not rely on AWT.
     *
     * This rule prevents reintroducing the issue in CI.
     */
    @ArchTest
    public static final ArchRule no_set_string_value_usage = noClasses()
        .should()
        .callMethod(Cell.class, "setStringValue", String.class)
        .because(
            "setStringValue() triggers AWT via optimizeCellSize() and crashes in environments without system fonts. Use OdfUtils.setCellText() instead."
        );
}
