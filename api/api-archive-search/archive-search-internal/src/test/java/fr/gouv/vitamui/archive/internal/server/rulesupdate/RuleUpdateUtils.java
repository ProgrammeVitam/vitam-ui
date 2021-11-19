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

package fr.gouv.vitamui.archive.internal.server.rulesupdate;

import fr.gouv.vitamui.archives.search.common.dto.VitamUiRuleAction;

import java.util.ArrayList;
import java.util.List;

public class RuleUpdateUtils {

    public static List<VitamUiRuleAction> createListOfVitamUiRule() {
        List<VitamUiRuleAction> result = new ArrayList<>();
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

        VitamUiRuleAction vitamUiRuleActionExample = new VitamUiRuleAction();
        vitamUiRuleActionExample.setRule("rule_Id");
        vitamUiRuleActionExample.setOldRule("old_Rule");
        vitamUiRuleActionExample.setDeleteHoldEndDate(true);
        vitamUiRuleActionExample.setDeleteHoldReason(true);
        vitamUiRuleActionExample.setDeleteHoldOwner(false);
        vitamUiRuleActionExample.setDeleteHoldReassessingDate(false);
        vitamUiRuleActionExample.setEndDate("end_Date");
        vitamUiRuleActionExample.setDeletePreventRearrangement(false);
        vitamUiRuleActionExample.setStartDate("2021-11-07T23:00:00.000Z");
        vitamUiRuleActionExample.setName("rule_Name");
        vitamUiRuleActionExample.setHoldOwner("hold_Owner");

        result.add(vitamUiRuleAction);
        result.add(vitamUiRuleActionExample);

        return result;
    }
}
