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
package fr.gouv.vitamui.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipGenerator {

    private static final int BYTE_NUMBER = 1024;

    /**
     * Method allowing to generate a zip folder containing the documents from the map.
     * @param filenames List containing the files paths to add to the folder
     * @param zipOutputStream the stream to write to. The owner of the outputstream is responsible for closing the stream.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void createZipFolder(final List<Path> filePaths, final OutputStream zipOutputStream) throws IOException {

        try (final ZipOutputStream zipFolder = new ZipOutputStream(zipOutputStream);) {

            for (final Path path : filePaths) {
                final File fileToZip = path.toFile();
                try (final FileInputStream fileInputStream = new FileInputStream(fileToZip)) {
                    final ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    zipFolder.putNextEntry(zipEntry);

                    final byte[] bytes = new byte[BYTE_NUMBER];
                    int length;
                    while ((length = fileInputStream.read(bytes)) >= 0) {
                        zipFolder.write(bytes, 0, length);
                    }
                }
            }
        }
    }

}
