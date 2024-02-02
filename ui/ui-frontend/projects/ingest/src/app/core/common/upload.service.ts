/*
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
import { HttpEventType } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

import { IngestApiService } from '../api/ingest-api.service';
import { IngestInfo, IngestList, IngestUploadStatus } from './ingest-list';
import { IngestType } from './ingest-type.enum';

@Injectable()
export class UploadService {
  uploadStatus = new BehaviorSubject<IngestList>(new IngestList());

  constructor(private ingestApiService: IngestApiService) {}

  filesStatus(): BehaviorSubject<IngestList> {
    return this.uploadStatus;
  }

  setUploadStatus(uploadStaus: BehaviorSubject<IngestList>) {
    this.uploadStatus = uploadStaus;
  }

  addNewUploadFile(requestId: string, ingest: IngestInfo): void {
    const map: IngestList = this.uploadStatus.getValue();
    map.add(requestId, ingest);
    this.uploadStatus.next(map);
  }

  updateFileStatus(requestId: string, sizeUploaded: number, status?: IngestUploadStatus): void {
    const map: IngestList = this.uploadStatus.getValue();
    map.update(requestId, sizeUploaded, status);
  }

  public uploadIngest(
    tenantIdentifier: string,
    file: Blob,
    fileName: string,
    type: IngestType,
    callback?: (operationId: string) => any,
  ): Observable<IngestList> {
    let progressPercent = 0;
    this.addNewUploadFile(fileName, new IngestInfo(fileName, file.size, 0, IngestUploadStatus.WIP));
    this.ingestApiService.uploadStreaming(tenantIdentifier, type, 'RESUME', file, fileName).subscribe(
      (data) => {
        if (data) {
          switch (data.type) {
            case HttpEventType.UploadProgress:
              progressPercent = Math.round((data.loaded / data.total) * 100);
              this.updateFileStatus(fileName, progressPercent);
              break;
            case HttpEventType.Response:
              this.updateFileStatus(fileName, 100, IngestUploadStatus.FINISHED);
              callback(data.headers.get('X-Operation-Id'));
              break;
          }
        }
      },
      (error) => {
        this.updateFileStatus(fileName, IngestUploadStatus.ERROR);
        console.log('ERROR: ', error);
      },
    );
    return this.uploadStatus;
  }
}
