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
import fr.gouv.vitamui.archives.search.common.dto.VitamUiManagementMetadataAction;
import fr.gouv.vitamui.archives.search.common.dto.VitamUiRuleAction;
import fr.gouv.vitamui.archives.search.common.dto.VitamUiRuleActions;
import fr.gouv.vitamui.archives.search.common.dto.VitamUiRuleCategoryAction;
import fr.gouv.vitamui.archives.search.common.dto.VitamUiRuleCategoryActionDeletion;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Update Archive units Rules Converters
 */
@Getter
@Setter
@NoArgsConstructor
public class RuleOperationsConverter {

    public ManagementMetadataAction convertToVitamManagementMetadataAction(
        VitamUiManagementMetadataAction vitamUiManagementMetadataAction) {

        return VitamUIUtils
            .copyProperties(vitamUiManagementMetadataAction, new ManagementMetadataAction());
    }

    public RuleAction convertToVitamRuleAction(VitamUiRuleAction vitamUiRuleAction) {
        final RuleAction ruleAction = VitamUIUtils.
            copyProperties(vitamUiRuleAction, new RuleAction());
     if(vitamUiRuleAction.getStartDate() != null) {
    LocalDateTime startDate =
        LocalDateTime.parse(vitamUiRuleAction.getStartDate(), ArchiveSearchConsts.ISO_FRENCH_FORMATER).withHour(0)
            .withMinute(0).withSecond(0).withNano(0);
    ruleAction.setStartDate(ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(startDate.plusDays(1)));
}

        return ruleAction;
    }

    public RuleActions convertToVitamRuleActions(VitamUiRuleActions vitamUiRuleActions) {

        final RuleActions ruleActions = VitamUIUtils.
            copyProperties(vitamUiRuleActions,new RuleActions());
        List<Map<String, RuleCategoryAction>> vitamAdd = new ArrayList<>();
        List<Map<String, RuleCategoryActionDeletion>> vitamDelete = new ArrayList<>();
        List<Map<String, RuleCategoryAction>> vitamUpdate = new ArrayList<>();

        if(vitamUiRuleActions != null && vitamUiRuleActions.getAddOrUpdateMetadata() != null) {
            ruleActions.setAddOrUpdateMetadata(convertToVitamManagementMetadataAction(vitamUiRuleActions.getAddOrUpdateMetadata()));
        }

        if(vitamUiRuleActions != null && vitamUiRuleActions.getDeleteMetadata() != null) {
            ruleActions.setDeleteMetadata(convertToVitamManagementMetadataAction(vitamUiRuleActions.getDeleteMetadata()));
        }

        if(vitamUiRuleActions != null && !vitamUiRuleActions.getAdd().isEmpty()){
            vitamUiRuleActions.getAdd().forEach(rule -> {
                Map<String, RuleCategoryAction> map = new HashMap<>();
                rule.keySet().forEach(ruleCategoryActionKey ->
                    map.put(ruleCategoryActionKey, convertToRuleCategoryAction(rule.get(ruleCategoryActionKey)))
                );
                vitamAdd.add(map);
            });
            ruleActions.setAdd(vitamAdd);
        }

        if(vitamUiRuleActions != null && !vitamUiRuleActions.getUpdate().isEmpty()){
            vitamUiRuleActions.getUpdate().forEach(rule -> {
                Map<String, RuleCategoryAction> map = new HashMap<>();
                rule.keySet().forEach(ruleCategoryActionKey ->
                    map.put(ruleCategoryActionKey, convertToRuleCategoryAction(rule.get(ruleCategoryActionKey)))
                );
                vitamUpdate.add(map);
            });
            ruleActions.setUpdate(vitamUpdate);
        }

        if(vitamUiRuleActions != null && !vitamUiRuleActions.getDelete().isEmpty()){
            vitamUiRuleActions.getDelete().forEach(rule -> {
                Map<String, RuleCategoryActionDeletion> map = new HashMap<>();
                rule.keySet().forEach(ruleCategoryActionKey ->
                    map.put(ruleCategoryActionKey, convertToRuleCategoryActionDeletion(rule.get(ruleCategoryActionKey)))
                );
                vitamDelete.add(map);
            });
            ruleActions.setDelete(vitamDelete);
        }
        return ruleActions;
    }

    public RuleCategoryAction convertToRuleCategoryAction(VitamUiRuleCategoryAction vitamUiRuleCategoryAction) {

        RuleCategoryAction ruleCategoryAction = VitamUIUtils.
            copyProperties(vitamUiRuleCategoryAction,new RuleCategoryAction());
        List<RuleAction> ruleActionList = new ArrayList<>();
        if(vitamUiRuleCategoryAction != null && vitamUiRuleCategoryAction.getRules() != null) {
            vitamUiRuleCategoryAction.getRules().forEach(rule ->
                ruleActionList.add(convertToVitamRuleAction(rule))
            );
            ruleCategoryAction.setRules(ruleActionList);
        }
        return ruleCategoryAction;
    }

    public RuleCategoryActionDeletion convertToRuleCategoryActionDeletion(
        VitamUiRuleCategoryActionDeletion vitamUiRuleCategoryActionDeletion) {

        RuleCategoryActionDeletion ruleCategoryActionDeletion  = VitamUIUtils.
            copyProperties(vitamUiRuleCategoryActionDeletion,new RuleCategoryActionDeletion());

        List<RuleAction> ruleActionList = new ArrayList<>();
        if(vitamUiRuleCategoryActionDeletion != null && vitamUiRuleCategoryActionDeletion.getRules() != null) {
            vitamUiRuleCategoryActionDeletion.getRules().ifPresent(ruleCategoryAction -> {
                ruleCategoryAction.forEach(rule ->
                    ruleActionList.add(convertToVitamRuleAction(rule)));
                ruleCategoryActionDeletion.setRules(ruleActionList);
            });
        }
        return ruleCategoryActionDeletion;
    }
}
