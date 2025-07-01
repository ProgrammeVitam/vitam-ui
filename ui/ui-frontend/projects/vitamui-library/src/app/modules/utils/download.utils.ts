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
import { HttpResponse } from '@angular/common/http';

export class DownloadUtils {
  private static DOUBLE_QUOTES = '"';

  static loadFromBlob(resp: HttpResponse<Blob>, mimeType: string, newFilename?: string) {
    // It is necessary to create a new blob object with mime-type explicitly set
    // otherwise only Chrome works like it should
    const newBlob = new Blob([resp.body], { type: mimeType });

    // For other browsers:
    // Create a link pointing to the ObjectURL containing the blob.
    const data = window.URL.createObjectURL(newBlob);

    const link = document.createElement('a');
    link.href = data;
    const fileName = newFilename
      ? newFilename
      : decodeURIComponent(DownloadUtils.getFilenameFromContentDisposition(resp.headers.get('content-disposition')));
    // If the filename is surrounded by double quotes we remove them to have the correct filename
    link.download = DownloadUtils.isSurroundedByDoubleQuotes(fileName) ? DownloadUtils.removeSurroundingDoubleQuotes(fileName) : fileName;
    // This is necessary as link.click() does not work on the latest firefox
    link.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));

    setTimeout(() => {
      // For Firefox it is necessary to delay revoking the ObjectURL
      window.URL.revokeObjectURL(data);
      link.remove();
    }, 100);
  }

  static getFilenameFromContentDisposition(contentDisposition: string): string {
    const regex = /filename[^;\n=]*=((['"]).*?\2|[^;\n]*)/g;
    const match = regex.exec(contentDisposition);
    if (!match || match.length <= 1) {
      return 'attachment';
    }
    return match[1];
  }

  static isSurroundedByDoubleQuotes(charSequence: string): boolean {
    return charSequence.startsWith(DownloadUtils.DOUBLE_QUOTES) && charSequence.endsWith(DownloadUtils.DOUBLE_QUOTES);
  }

  static removeSurroundingDoubleQuotes(charSequence: string): string {
    return charSequence.substr(1, charSequence.length - 2);
  }
}
