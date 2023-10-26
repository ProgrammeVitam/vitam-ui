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
 */

package fr.gouv.vitamui.common.spring;

import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.apache.commons.beanutils.BeanUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;

/**
 * Implementation of {@link NotNullIfAnotherFieldHasValue} validator.
 **/
public class NotNullIfAnotherFieldHasValueValidator
    implements ConstraintValidator<NotNullIfAnotherFieldHasValue, Object> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(NotNullIfAnotherFieldHasValueValidator.class);


    private String fieldName;
    private String expectedFieldValue;
    private String[] fieldNamesToCheck;

    @Override
    public void initialize(NotNullIfAnotherFieldHasValue annotation) {
        fieldName          = annotation.fieldName();
        expectedFieldValue = annotation.fieldValue();
        fieldNamesToCheck    = annotation.fieldNamesToCheck();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {

        LOGGER.debug("Start : check validity of attribute");

        if (value == null) {
            return true;
        }

        try {
            String fieldValue = BeanUtils.getProperty(value, fieldName);

            for(String fieldNameToCheck : fieldNamesToCheck) {
                String dependFieldValue = BeanUtils.getProperty(value, fieldNameToCheck);

                if (expectedFieldValue.equals(fieldValue) && dependFieldValue == null) {
                    ctx.disableDefaultConstraintViolation();
                    ctx.buildConstraintViolationWithTemplate(ctx.getDefaultConstraintMessageTemplate())
                        .addNode(fieldNameToCheck)
                        .addConstraintViolation();
                    return false;
                }
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            LOGGER.error("Error with the attributes list");
            throw new PreconditionFailedException("Error with the attributes list", ex);
        }

        LOGGER.debug("the attribute (s) is (are) valid");
        return true;
    }

}
