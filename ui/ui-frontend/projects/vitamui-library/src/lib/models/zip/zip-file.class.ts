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

import JSZip from 'jszip';
import { ZipFileStatus } from './zip-file-status.interface';
import { HttpEvent, HttpEventType } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';

export class ZipFile {
  private zipFile: JSZip;
  zipFileStatus: ZipFileStatus = null;
  zipFileStatus$: BehaviorSubject<ZipFileStatus> = new BehaviorSubject<ZipFileStatus>(null);

  constructor(transactionId?: string) {
    this.zipFile = new JSZip();
    this.zipFileStatus = {
      transactionId: transactionId,
      size: 0,
      uploadedSize: 0,
    };
  }

  setTransactionId(transactionId: string): ZipFile {
    this.zipFileStatus.transactionId = transactionId;
    return this;
  }

  addFiles(files: FileList | File[]): ZipFile {
    if (files.length === 0) {
      return this;
    }
    for (let i = 0; i < files.length; i++) {
      const item = files[i];
      this.zipFile.file(item.webkitRelativePath, item);
      this.zipFileStatus.size += item.size;
    }
    return this;
  }

  generateZip(): Promise<Blob> {
    return this.zipFile
      .generateInternalStream({ type: 'blob' })
      .accumulate((metadata) => this.updateZipFileStatus(metadata.currentFile, metadata.percent));
  }

  private updateZipFileStatus(metadataCurrentFile: string, metadataPercent: number) {
    this.zipFileStatus.currentFile = metadataCurrentFile;
    this.zipFileStatus.currentFileUploadedSize = metadataPercent;
    this.zipFileStatus$.next(this.zipFileStatus);
  }

  updateUploadingZipFileStatus(data: HttpEvent<any>) {
    if (!data) return;
    let progressPercent = 0;
    switch (data.type) {
      case HttpEventType.UploadProgress:
        progressPercent = Math.round((data.loaded / data.total) * 100);
        break;
      case HttpEventType.Response:
        progressPercent = 100;
        break;
    }
    this.zipFileStatus.uploadedSize = progressPercent;
    this.zipFileStatus$.next(this.zipFileStatus);
  }
}
