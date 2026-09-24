package fr.gouv.vitamui.iam.server.arch;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import fr.gouv.vitamui.commons.test.arch.AuthenticationContractRules;

@AnalyzeClasses(packages = "fr.gouv.vitamui")
public class ArchitectureTest {

    @ArchTest
    static final ArchTests authenticationContract = ArchTests.in(AuthenticationContractRules.class);
}
