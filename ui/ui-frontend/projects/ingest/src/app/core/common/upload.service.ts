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
import {
  HttpClient,
  HttpEvent,
  HttpHeaders,
  HttpRequest,
  HttpResponse,
} from "@angular/common/http";
import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable } from "rxjs";
import { retry, timeout } from "rxjs/operators";
import { IngestApiService } from "../api/ingest-api.service";
import { IngestInfo, IngestList, IngestStatus } from "./ingest-list";

const BYTES_PER_CHUNK = 1024 * 1024; // 1MB request
const tenantKey = "X-Tenant-Id";
const contextIdKey = "X-Context-Id";
const actionKey = "X-Action";
const chunkOffsetKey = "X-Chunk-Offset";
const totalSizeKey = "X-Size-Total";
const requestIdKey = "X-Request-Id";

const MAX_RETRIES = 3;

@Injectable()
export class UploadService {
  uploadStatus = new BehaviorSubject<IngestList>(new IngestList());

  constructor(
    private ingestApiService: IngestApiService,
    private httpClient: HttpClient
  ) {}

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

  updateFileStatus(requestId: string, status?: IngestStatus): void {
    const map: IngestList = this.uploadStatus.getValue();
    map.update(requestId, status);
  }

  uploadFile(
    file: File,
    contextId: string,
    action: string,
    tenantIdentifier: string
  ): Observable<IngestList> {
    const totalSize = file.size;
    let start = 0;
    let end = totalSize < BYTES_PER_CHUNK ? totalSize : BYTES_PER_CHUNK;
    const nbChunks = Math.ceil(totalSize / BYTES_PER_CHUNK);

    const request = this.generateIngestRequest(
      tenantIdentifier,
      contextId,
      action,
      start,
      end,
      totalSize,
      file
    );

    this.ingestApiService
      .upload(request)
      .pipe(timeout(1480000), retry(MAX_RETRIES))
      .subscribe(
        (event) => {
          if (event instanceof HttpResponse) {
            // We get the requestId with the first request.
            const requestId = event.headers.get(requestIdKey);
            this.addNewUploadFile(
              requestId,
              new IngestInfo(
                file.name,
                totalSize,
                nbChunks,
                1,
                IngestStatus.WIP
              )
            );
            console.log("First API Request Id : " + requestId);
            start = end;
            end = start + BYTES_PER_CHUNK;

            if (start >= totalSize) {
              this.updateFileStatus(requestId, IngestStatus.FINISHED);
              return;
            }

            for (
              let pointer = start;
              pointer < totalSize;
              pointer += BYTES_PER_CHUNK
            ) {
              this.uploadChunks(
                file,
                requestId,
                pointer,
                pointer + BYTES_PER_CHUNK,
                totalSize,
                tenantIdentifier,
                contextId,
                action
              );
            }
          }
        },
        (error) => {
          console.log(error);
          this.addNewUploadFile(
            "error",
            new IngestInfo(
              file.name,
              totalSize,
              nbChunks,
              1,
              IngestStatus.ERROR
            )
          );
        }
      );
    return this.uploadStatus;
  }

  private uploadChunks(
    file: File,
    requestId: any,
    start: number,
    end: number,
    totalSize: any,
    tenantIdentifier: string,
    contextId: string,
    action: string
  ) {
    const request = this.generateIngestRequest(
      tenantIdentifier,
      contextId,
      action,
      start,
      end,
      totalSize,
      file,
      requestId
    );
    this.ingestApiService
      .upload(request)
      .pipe(retry(MAX_RETRIES))
      .subscribe(
        (event) => {
          if (event instanceof HttpResponse) {
            this.updateFileStatus(requestId);
          }
        },
        (error) => {
          console.log(error);
          this.updateFileStatus(requestId, IngestStatus.ERROR);
        }
      );
  }

  private generateIngestRequest(
    tenantIdentifier: string,
    contextId: string,
    action: string,
    start: number,
    end: number,
    totalSize: number,
    file: File,
    requestId?: string
  ): HttpRequest<FormData> {
    let headers = new HttpHeaders();
    headers = headers.set(tenantKey, tenantIdentifier.toString());
    headers = headers.set(chunkOffsetKey, start.toString());
    headers = headers.set(totalSizeKey, totalSize.toString());
    headers = headers.set(contextIdKey, contextId);
    headers = headers.set(actionKey, action);
    headers = headers.set("reportProgress", "true");
    headers = headers.set("fileName", file.name);
    if (requestId) {
      headers = headers.set(requestIdKey, requestId);
    }

    const formdata: FormData = new FormData();
    formdata.append("uploadedFile", file.slice(start, end), file.name);

    return new HttpRequest(
      "POST",
      this.ingestApiService.getBaseUrl() + "/ingest/upload",
      formdata,
      { headers }
    );
  }

  public uploadFileV2(
    tenantIdentifier: string,
    contextId: string,
    action: string,
    file: Blob,
    fileName: string
  ): Observable<HttpEvent<void>> {
    let headers = new HttpHeaders();
    headers = headers.set(tenantKey, tenantIdentifier.toString());
    headers = headers.set(contextIdKey, contextId);
    headers = headers.set(actionKey, action);
    headers = headers.set("Content-Type", "application/octet-stream");
    headers = headers.set("reportProgress", "true");
    headers = headers.set("ngsw-bypass", "true");
    headers = headers.set("fileName", fileName);

    const options = {
      headers: headers,
      responseType: "text" as "text",
      reportProgress: true,
    };

    return this.httpClient.request(
      new HttpRequest(
        "POST",
        this.ingestApiService.getBaseUrl() + "/ingest/upload-v2",
        file,
        options
      )
    );
  }
}
