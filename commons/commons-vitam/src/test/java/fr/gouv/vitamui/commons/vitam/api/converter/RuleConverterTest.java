/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *
 *
 */

package fr.gouv.vitamui.commons.vitam.api.converter;

import fr.gouv.vitam.common.model.administration.FileRulesModel;
import fr.gouv.vitam.common.model.administration.RuleMeasurementEnum;
import fr.gouv.vitam.common.model.administration.RuleType;
import fr.gouv.vitamui.commons.rest.dto.RuleDto;
import fr.gouv.vitamui.commons.vitam.api.config.converter.RuleConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class RuleConverterTest {

    private final RuleConverter ruleConverter = new RuleConverter();

    @Test
    void testConvertRuleVitamModelToRuleDtoVitamUI() {
        FileRulesModel fileRulesModel = new FileRulesModel();
        fileRulesModel.setRuleId("APP-09049");
        fileRulesModel.setId("FileModelId");
        fileRulesModel.setVersion(1);
        fileRulesModel.setTenant(11);
        fileRulesModel.setRuleDuration("36");
        fileRulesModel.setCreationDate("15/12/2009");
        fileRulesModel.setUpdateDate("30/08/2012");
        fileRulesModel.setRuleValue("Elimination Ouverture des stations SNCF Gare d’Austerlitz");
        fileRulesModel.setRuleDescription(
            "Elimination - L’échéance est calculée à partir de la date d’annonce de la fermeture"
        );

        RuleDto ruleDto = ruleConverter.convertVitamToDto(fileRulesModel);

        assertThat(ruleDto).isNotNull().isEqualToComparingFieldByField(fileRulesModel);
    }

    @Test
    void testConvertRuleDtoVitamUiToRuleModelVitam() {
        RuleDto ruleDto = new RuleDto();
        ruleDto.setTenant(15);
        ruleDto.setRuleId("APP-00001");
        ruleDto.setVersion(5);
        ruleDto.setCreationDate("25/08/2018");
        ruleDto.setUpdateDate("22/04/2019");
        ruleDto.setRuleDuration("35");
        ruleDto.setRuleDescription("règle de gestion : Appraisal Rule");
        ruleDto.setRuleValue("gestion gestion");

        FileRulesModel fileRulesModel = ruleConverter.convertDtoToVitam(ruleDto);

        assertThat(fileRulesModel).isNotNull();
        assertThat(ruleDto).isEqualToComparingFieldByField(fileRulesModel);
    }

    @Test
    void testConvertRuleVitamModelToRuleDtoVitamUIWithRuleTypeAndRuleMeasurement() {
        final String month = "month";
        final String holdRule = "HoldRule";

        FileRulesModel fileRulesModel = new FileRulesModel();
        fileRulesModel.setRuleMeasurement(RuleMeasurementEnum.MONTH);
        fileRulesModel.setRuleType(RuleType.HoldRule);

        RuleDto ruleDto = ruleConverter.convertVitamToDto(fileRulesModel);

        assertThat(ruleDto).isNotNull();
        assertThat(ruleDto.getRuleMeasurement()).isEqualTo(month);
        assertThat(ruleDto.getRuleType()).isEqualTo(holdRule);
    }

    @Test
    void testConvertRuleDtoVitamUiToRuleModelVitamWithRuleTypeAndRuleMeasurement() {
        final String day = "DAY";
        final String appraisalRule = "AppraisalRule";

        RuleDto ruleDto = new RuleDto();
        ruleDto.setRuleMeasurement("day");
        ruleDto.setRuleType("AppraisalRule");

        FileRulesModel fileRulesModel = ruleConverter.convertDtoToVitam(ruleDto);

        assertThat(fileRulesModel).isNotNull();
        assertThat(fileRulesModel.getRuleMeasurement().name()).isEqualTo(day);
        assertThat(fileRulesModel.getRuleType().name()).isEqualTo(appraisalRule);
    }
}
