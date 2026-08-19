/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { Component } from '@angular/core';

import { FileValidationErrors } from 'vitamui-library';
import { readFileContent, VitamUICommonModule } from 'vitamui-library';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'design-system-upload',
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.scss',
  imports: [VitamUICommonModule, FormsModule, ReactiveFormsModule],
})
export class UploadComponent {
  filenameValidator =
    (expectedFileName: string) =>
    async (file: File): Promise<FileValidationErrors> => {
      if (file.name !== expectedFileName) {
        console.warn(`Invalid filename. Expected: "${expectedFileName}"`);
        return {
          fileErrors: { INVALID_FILE_NAME: { expectedFileName: expectedFileName } },
          controlErrors: { invalidFiles: true },
        };
      }
      console.log(`Filename valid`);
      return null;
    };

  csvValidator = async (file: File): Promise<FileValidationErrors> => {
    const content = await readFileContent(file);
    const errors = [];
    const header = 'Identifier,Name,Description';
    const lines = content.split('\n');
    if (!content.startsWith(header)) {
      errors.push(`Première ligne invalide :<ul><li>Valeur : "${lines[0]}"</li><li>Valeur attendue : "${header}"</li></ul>`);
    }
    const dataLines = lines.slice(1, -1);
    if (dataLines.length === 0) {
      errors.push(`Il n'y a aucune ligne de données`);
    }
    dataLines.forEach((line, index) => {
      if (line.split(',').length !== header.split(',').length) {
        errors.push(
          `La ligne ${index + 1} n'a pas autant de colonnes que le header :<ul><li>Valeur : "${line}"</li><li>${header.split(',').length} colonnes attendues</li></ul>`,
        );
      }
    });
    if (errors.length > 0) {
      console.warn(`Invalid CSV:`, errors);
      return {
        fileErrors: { INVALID_CSV: true },
        controlErrors: { INVALID_CSV_DETAIL: { detail: errors.map((error) => `<li>${error}</li>`).join('') } },
      };
    }
    console.log(`Valid content`);
    return null;
  };
}
