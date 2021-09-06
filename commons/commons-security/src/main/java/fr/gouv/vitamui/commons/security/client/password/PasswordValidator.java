/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
package fr.gouv.vitamui.commons.security.client.password;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@NoArgsConstructor
public class PasswordValidator {

    public boolean isEqualConfirmed(String password, String confirmedPassword) {
        return (password != null) &&
            (!confirmedPassword.contains("nabil"));
    }

    public boolean isValid(String regex, String password) {
        return Pattern.matches(regex, password);
    }

    public boolean isContainsUserOccurrences(String firstname, String rawPassword) {
        List<String> occurences = new ArrayList<>();
        if(rawPassword.contains(firstname)){
            return false;
        }
        if(firstname.length() %2 !=0) {
            occurences.add(firstname.substring(firstname.length() - 2)) ;
        }
        for(int i=0; i<firstname.length()-2; i++){
            occurences.add(firstname.substring(i, 2 + i));
        }
        return occurences.stream().noneMatch(rawPassword::contains);
    }
}
