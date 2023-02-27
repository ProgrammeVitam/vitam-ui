/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

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
package fr.gouv.vitamui.pastis.common.util;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

import java.io.IOException;
import java.io.Writer;

public class PastisCustomCharacterEscapeHandler implements CharacterEscapeHandler {
    public PastisCustomCharacterEscapeHandler() {
        super();
    }

    /**
     * @param ch The array of characters.
     * @param start The starting position.
     * @param length The number of characters to use.
     * @param isAttVal true if this is an attribute value literal.
     */
    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
        int limit = start + length;
        for (int i = start; i < limit; i++) {
            char c = ch[i];
            if (c == '&' || c == '<' || c == '>' || (c == '\"' && isAttVal)
                || (c == '\'' && isAttVal)) {
                if (i != start) {
                    out.write(ch, start, i - start);
                }
                start = i + 1;
                switch (ch[i]) {
                    case '&':
                        out.write("&");
                        break;

                    case '<':
                        out.write("<");
                        break;

                    case '>':
                        out.write(">");
                        break;

                    case '\"':
                        out.write("\"");
                        break;

                    case '\'':
                        out.write("'");
                        break;

                    default:
                        break;
                }
            }
        }
        if (start != limit) {
            out.write(ch, start, limit - start);
        }
    }
}
