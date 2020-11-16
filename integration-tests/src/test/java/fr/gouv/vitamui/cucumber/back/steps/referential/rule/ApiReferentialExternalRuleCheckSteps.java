/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.cucumber.back.steps.referential.rule;

import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.referential.common.dto.RuleDto;
import fr.gouv.vitamui.referential.common.utils.ReferentialDtoBuilder;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

/**
 * Teste l'API Rules dans Referential admin : operations de verification.
 *
 *
 */
public class ApiReferentialExternalRuleCheckSteps extends CommonSteps {

    @Given("la regle RuleTest existe")
    public void la_regle_RuleTest_existe() {
        final RuleDto ruleDto = ReferentialDtoBuilder.buildRuleDto(null, "RuleTest", "StorageRule", "Test rule value 1", "Test rule Description 1", "1", "DAY");
        if(!getRuleRestClient().check(getSystemTenantUserAdminContext(), ruleDto)) {
            getRuleRestClient().create(getSystemTenantUserAdminContext(), ruleDto);
        }
    }

    @Given("la regle RuleTest n'existe pas")
    public void la_regle_RuleTest_n_existe_pas() {
        final RuleDto ruleDto = ReferentialDtoBuilder.buildRuleDto(null, "RuleTest", "StorageRule", "Test rule value 1", "Test rule Description 1", "1", "DAY");
        if(getRuleRestClient().check(getSystemTenantUserAdminContext(), ruleDto)) {
            getRuleRestClient().delete(getSystemTenantUserAdminContext(), "RuleTest");
        }
    }

    @When("^un utilisateur verifie l'existence de la regle RuleTest par son identifiant$")
    public void un_utilisateur_verifie_l_existence_de_la_regle_RuleTest_par_son_identifiant() {
        try {
            final RuleDto ruleDto = new RuleDto();
            ruleDto.setRuleId("RuleTest");
            testContext.bResponse = getRuleRestClient().check(getSystemTenantUserAdminContext(), ruleDto);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}
