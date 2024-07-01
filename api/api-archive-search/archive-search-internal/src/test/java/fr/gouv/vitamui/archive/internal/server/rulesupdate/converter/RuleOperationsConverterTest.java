/*
 * *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 *  * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *  *
 *  * contact@programmevitam.fr
 *  *
 *  * This software is a computer program whose purpose is to implement
 *  * implement a digital archiving front-office system for the secure and
 *  * efficient high volumetry VITAM solution.
 *  *
 *  * This software is governed by the CeCILL-C license under French law and
 *  * abiding by the rules of distribution of free software.  You can  use,
 *  * modify and/ or redistribute the software under the terms of the CeCILL-C
 *  * license as circulated by CEA, CNRS and INRIA at the following URL
 *  * "http://www.cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and  rights to copy,
 *  * modify and redistribute granted by the license, users are provided only
 *  * with a limited warranty  and the software's author,  the holder of the
 *  * economic rights,  and the successive licensors  have only  limited
 *  * liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated
 *  * with loading,  using,  modifying and/or developing or reproducing the
 *  * software by the user in light of its specific status of free software,
 *  * that may mean  that it is complicated to manipulate,  and  that  also
 *  * therefore means  that it is reserved for developers  and  experienced
 *  * professionals having in-depth computer knowledge. Users are therefore
 *  * encouraged to load and test the software's suitability as regards their
 *  * requirements in conditions enabling the security of their systems and/or
 *  * data to be ensured and,  more generally, to use and operate it in the
 *  * same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had
 *  * knowledge of the CeCILL-C license and that you accept its terms.
 *
 */

package fr.gouv.vitamui.archive.internal.server.rulesupdate.converter;

import fr.gouv.vitam.common.model.massupdate.ManagementMetadataAction;
import fr.gouv.vitam.common.model.massupdate.RuleAction;
import fr.gouv.vitam.common.model.massupdate.RuleActions;
import fr.gouv.vitam.common.model.massupdate.RuleCategoryAction;
import fr.gouv.vitam.common.model.massupdate.RuleCategoryActionDeletion;
import fr.gouv.vitamui.archive.internal.server.rulesupdate.RuleUpdateUtils;
import fr.gouv.vitamui.archives.search.common.dto.VitamUiManagementMetadataAction;
import fr.gouv.vitamui.archives.search.common.dto.VitamUiRuleAction;
import fr.gouv.vitamui.archives.search.common.dto.VitamUiRuleActions;
import fr.gouv.vitamui.archives.search.common.dto.VitamUiRuleCategoryAction;
import fr.gouv.vitamui.archives.search.common.dto.VitamUiRuleCategoryActionDeletion;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class RuleOperationsConverterTest {

    private final RuleOperationsConverter ruleOperationsConverter = new RuleOperationsConverter();

    @Test
    void testConvertVitamUiRuleActionToVitamRuleAction() {
        VitamUiRuleAction vitamUiRuleAction = new VitamUiRuleAction();
        vitamUiRuleAction.setRule("rule_Id");
        vitamUiRuleAction.setOldRule("old_Rule");
        vitamUiRuleAction.setDeleteHoldEndDate(true);
        vitamUiRuleAction.setDeleteHoldReason(true);
        vitamUiRuleAction.setDeleteHoldOwner(false);
        vitamUiRuleAction.setDeleteHoldReassessingDate(false);
        vitamUiRuleAction.setEndDate("end_Date");
        vitamUiRuleAction.setDeletePreventRearrangement(false);
        vitamUiRuleAction.setStartDate("2021-11-07T23:00:00.000Z");
        vitamUiRuleAction.setName("rule_Name");
        vitamUiRuleAction.setHoldOwner("hold_Owner");

        RuleAction ruleActionResult = ruleOperationsConverter.convertToVitamRuleAction(vitamUiRuleAction);

        LocalDateTime startDate = LocalDateTime.parse(
            vitamUiRuleAction.getStartDate(),
            ArchiveSearchConsts.ISO_FRENCH_FORMATER
        )
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        vitamUiRuleAction.setStartDate(ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(startDate.plusDays(1)));

        assertThat(ruleActionResult).isEqualToComparingFieldByField(vitamUiRuleAction);
    }

    @Test
    void testCovertVitamUiManagementMetadataActionToVitamManagementMetadataAction() {
        VitamUiManagementMetadataAction vitamUiManagementMetadataAction = new VitamUiManagementMetadataAction();
        vitamUiManagementMetadataAction.setArchiveUnitProfile("archive_unit-profile");
        ManagementMetadataAction managementMetadataActionResult =
            ruleOperationsConverter.convertToVitamManagementMetadataAction(vitamUiManagementMetadataAction);

        assertThat(managementMetadataActionResult).isEqualToComparingFieldByField(vitamUiManagementMetadataAction);
    }

    @Test
    void testCovertVitamUiRuleCategoryActionToVitamRuleCategoryAction() {
        VitamUiRuleCategoryAction vitamUiRuleCategoryAction = new VitamUiRuleCategoryAction();
        vitamUiRuleCategoryAction.setFinalAction("Keep");
        vitamUiRuleCategoryAction.setClassificationAudience("classificationAudience");
        vitamUiRuleCategoryAction.setClassificationLevel("Keep");
        vitamUiRuleCategoryAction.setPreventInheritance(true);
        vitamUiRuleCategoryAction.setRules(new ArrayList<>());
        vitamUiRuleCategoryAction.setPreventRulesId(new HashSet<>());
        vitamUiRuleCategoryAction.setPreventRulesIdToAdd(new HashSet<>());

        RuleCategoryAction ruleCategoryActionResult = ruleOperationsConverter.convertToRuleCategoryAction(
            vitamUiRuleCategoryAction
        );
        assertThat(ruleCategoryActionResult).isEqualToComparingFieldByField(vitamUiRuleCategoryAction);
    }

    @Test
    void testCovertVitamUiRuleCategoryActionDeletionToRuleCategoryActionDeletion() {
        // Given
        Set<String> rulesIdToRemove = new HashSet<>();
        VitamUiRuleCategoryActionDeletion vitamUiRuleCategoryActionDeletion = new VitamUiRuleCategoryActionDeletion();
        rulesIdToRemove.add("rulesId1");
        rulesIdToRemove.add("rulesId2");
        rulesIdToRemove.add("rulesId3");
        rulesIdToRemove.add("rulesId4");
        vitamUiRuleCategoryActionDeletion.setPreventRulesIdToRemove(rulesIdToRemove);

        // When
        RuleCategoryActionDeletion ruleCategoryActionDeletion =
            ruleOperationsConverter.convertToRuleCategoryActionDeletion(vitamUiRuleCategoryActionDeletion);

        // Then
        assertThat(ruleCategoryActionDeletion).isEqualToComparingFieldByField(vitamUiRuleCategoryActionDeletion);
    }

    @Test
    void testConvertVitamUiRuleActionsToVitamRuleActions() {
        VitamUiRuleActions vitamUiRuleActions = new VitamUiRuleActions();
        vitamUiRuleActions.setAdd(new ArrayList<>());
        vitamUiRuleActions.setDelete(new ArrayList<>());
        vitamUiRuleActions.setUpdate(new ArrayList<>());

        RuleActions ruleActionsResult = ruleOperationsConverter.convertToVitamRuleActions(vitamUiRuleActions);
        assertThat(ruleActionsResult).isEqualToComparingFieldByField(vitamUiRuleActions);
    }

    @Test
    void testConvertVitamUiListOfRuleActionToVitamRuleAction() {
        List<VitamUiRuleAction> vitamUiRuleActionList = RuleUpdateUtils.createListOfVitamUiRule();
        vitamUiRuleActionList.forEach(vitamUiRuleAction -> {
            RuleAction ruleActionResult = ruleOperationsConverter.convertToVitamRuleAction(vitamUiRuleAction);
            LocalDateTime startDate = LocalDateTime.parse(
                vitamUiRuleAction.getStartDate(),
                ArchiveSearchConsts.ISO_FRENCH_FORMATER
            )
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
            vitamUiRuleAction.setStartDate(ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(startDate.plusDays(1)));

            assertThat(ruleActionResult).isEqualToComparingFieldByField(vitamUiRuleAction);
        });
    }
}
