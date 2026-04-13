package fr.gouv.vitamui.pastis.common.service.arch;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import fr.gouv.vitamui.commons.test.arch.OdfToolkitRules;

@AnalyzeClasses(packages = "fr.gouv.vitamui")
public class ArchitectureTest {

    @ArchTest
    static final ArchTests rules = ArchTests.in(OdfToolkitRules.class);
}
