/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/

package fr.gouv.vitamui.pastis.common.service;

import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.InputStream;
import java.io.InputStreamReader;


public class PuaPastisValidatorTest {

    PuaPastisValidator puaPastisValidator = new PuaPastisValidator();

    public void testImport(String pathFile, boolean environnementStandalone) {
        InputStream inputStreamPua = getClass().getClassLoader().getResourceAsStream(pathFile);
        JSONTokener tokener = new JSONTokener(new InputStreamReader(inputStreamPua));
        JSONObject profileJson = new JSONObject(tokener);
        puaPastisValidator.validatePUA(profileJson, environnementStandalone);
    }
}
