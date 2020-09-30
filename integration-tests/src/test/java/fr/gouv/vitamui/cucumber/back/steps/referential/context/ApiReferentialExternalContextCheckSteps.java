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
package fr.gouv.vitamui.cucumber.back.steps.referential.context;

import io.cucumber.java.en.When;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.utils.TestConstants;

/**
 * Teste l'API Contextes dans Referential admin : opérations de vérification.
 *
 *
 */
public class ApiReferentialExternalContextCheckSteps extends CommonSteps {

    @When("^un utilisateur vérifie l'existence d'un contexte par son identifiant$")
    public void un_utilisateur_vérifie_l_existence_d_un_contexte_par_son_identifiant() {
        try {
            final QueryDto criteria = QueryDto.criteria("id", TestConstants.CONTEXT_NAME, CriterionOperator.EQUALS);
            testContext.bResponse = getContextRestClient().checkExist(getSystemTenantUserAdminContext(), TestConstants.CONTEXT_IDENTIFIER);
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }

    @When("^un utilisateur vérifie l'existence d'un contexte par son code et son nom$")
    public void un_utilisateur_vérifie_l_existence_d_un_contexte_par_son_code_et_son_nom() {
        try {
            final QueryDto criteria = QueryDto.criteria(
                	"id", TestConstants.CONTEXT_IDENTIFIER, CriterionOperator.EQUALS).addCriterion(
                	"name",TestConstants.CONTEXT_NAME, CriterionOperator.EQUALS);
            testContext.bResponse = getContextRestClient().checkExist(getSystemTenantUserAdminContext(), criteria.toJson());
        }
        catch (final RuntimeException e) {
            testContext.exception = e;
        }
    }
}