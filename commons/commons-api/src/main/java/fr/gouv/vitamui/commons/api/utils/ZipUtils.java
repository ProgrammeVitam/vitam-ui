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
package fr.gouv.vitamui.commons.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;

import fr.gouv.vitamui.commons.api.exception.FileGenerationException;

/**
 * Tool allowing to compress files into a ZIP archive.
 *
 */
public class ZipUtils {

    /**
     * Method allowing to generate a SIP with a provided manifest and files.
     * @param manifest Stream of the manifest.
     * @param contentFiles List of streams of files declared into the manifest.
     * @param outputStream Stream where the ZIP will be store (it's the caller's responsibility to close its stream !).
     */
    public static void generate(final Map<String, InputStream> files, final OutputStream outputStream) {
        Assert.notNull(files, "Files must be not null");
        files.forEach((name, stream) -> {
            Assert.notNull(stream, "A null stream is set for the following file : " + name);
        });
        Assert.notNull(outputStream, "The output stream is null");

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {

            for (final Entry<String, InputStream> entry : files.entrySet()) {
                addZipEntry(zos, entry.getKey(), entry.getValue());
            }
            zos.flush();
        }
        catch (final IOException exception) {
            throw new FileGenerationException("IO exception when creating zip file: " + exception.getMessage(), exception);
        }
    }

    /**
     * Method allowing to add an entry to the zip according an input stream.
     * @param zipStream Stream of the archive.
     * @param name Name of the entry (can be a relative path).
     * @param input Stream to store.
     * @return The updated archive.
     * @throws IOException Exception can be thrown when an error occurred during the copy of the file's content.
     */
    protected static ZipOutputStream addZipEntry(final ZipOutputStream zipStream, final String name, final InputStream input) throws IOException {

        final ZipEntry entry = new ZipEntry(name);
        zipStream.putNextEntry(entry);
        try {
            IOUtils.copy(input, zipStream);
        }
        finally {
            zipStream.closeEntry();
        }
        return zipStream;
    }
}
