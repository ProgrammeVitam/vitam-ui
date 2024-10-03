/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
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
import { HttpClient, HttpEvent, HttpEventType, HttpHeaders, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import * as JSZip from 'jszip';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { CollectUploadFile, CollectZippedUploadFile } from './collect-upload-file';

@Injectable({
  providedIn: 'root',
})
export class CollectUploadService {
  private static X_TENANT_KEY = 'X-Tenant-Id';
  private static X_TRANSACTION_ID_KEY = 'X-Transaction-Id';
  private static X_ORIGINAL_FILENAME_HEADER = 'X-Original-Filename';
  private static COLLECT_UPLOAD_URL = './collect-api/projects/upload';
  zipFile: JSZip;
  private uploadingFiles$: BehaviorSubject<CollectUploadFile[]> = new BehaviorSubject<CollectUploadFile[]>([]);
  private filesToUpload: CollectUploadFile[] = [];
  private zippedFile: CollectZippedUploadFile = null;
  private watchZippedFile$: BehaviorSubject<CollectZippedUploadFile> = new BehaviorSubject<CollectZippedUploadFile>(null);

  constructor(private httpClient: HttpClient) {
    this.zipFile = new JSZip();
  }

  private static uploadFilesInfo(files: any) {
    let size = 0;
    // tslint:disable-next-line:prefer-for-of
    for (let i = 0; i < files.length; i++) {
      size += files[i].size;
    }
    const name = CollectUploadService.uploadFilesDirectoryName(files);
    return { name, size, dragged: false } as CollectUploadFile;
  }

  private static uploadFilesDirectoryName(files: any) {
    let name = files[0].webkitRelativePath;
    if (name.indexOf('/') !== -1) {
      name = name.split('/')[0];
    }
    return name;
  }

  private static dragAndDropUploadFilesDirectoryName(files: any) {
    return files[0].webkitGetAsEntry().fullPath.substring(1);
  }

  /*** Public methods ***/
  getUploadingFiles(): Observable<CollectUploadFile[]> {
    return this.uploadingFiles$.asObservable();
  }

  getZipFile(): Observable<CollectZippedUploadFile> {
    return this.watchZippedFile$.asObservable();
  }

  removeFolder(uploadFile: CollectUploadFile) {
    const index = this.filesToUpload.indexOf(uploadFile);
    if (index === -1) {
      return;
    }
    this.zipFile.remove(uploadFile.name);
    this.filesToUpload.splice(index, 1);
    this.uploadingFiles$.next(this.filesToUpload);
  }

  directoryExistInZipFile(files: any, dragged: boolean) {
    let name: string;
    if (dragged) {
      name = CollectUploadService.dragAndDropUploadFilesDirectoryName(files);
    } else {
      name = CollectUploadService.uploadFilesDirectoryName(files);
    }
    const indexName = Object.values(this.zipFile.files)
      .filter((f) => f.dir)
      .map((f) => f.name.slice(0, -1))
      .findIndex((n) => n === name);
    return indexName !== -1;
  }

  uploadZip(tenantIdentifier: number, transactionId: string) {
    this.zippedFile = {
      name: `${transactionId}.zip`,
      size: this.filesToUpload.map((f) => f.size).reduce((prev, cur) => prev + cur, 0),
      uploadedSize: 0,
    };
    this.watchZippedFile$.next(this.zippedFile);
    let headers = new HttpHeaders();
    headers = headers.set(CollectUploadService.X_TENANT_KEY, tenantIdentifier.toString());
    headers = headers.set(CollectUploadService.X_TRANSACTION_ID_KEY, transactionId);
    headers = headers.set(CollectUploadService.X_ORIGINAL_FILENAME_HEADER, this.zippedFile.name);
    headers = headers.set('Content-Type', 'application/octet-stream');
    headers = headers.set('reportProgress', 'true');
    headers = headers.set('ngsw-bypass', 'true');

    const options = {
      headers,
      responseType: 'text' as 'text',
      reportProgress: true,
    };
    return this.zipFile
      .generateInternalStream({ type: 'blob' })
      .accumulate((metadata) => {
        this.updateInternalZipFile(metadata.currentFile, metadata.percent);
      })
      .then((content) => {
        return this.httpClient.request(new HttpRequest('POST', CollectUploadService.COLLECT_UPLOAD_URL, content, options)).pipe(
          tap((data) => {
            if (data) {
              this.updateUploadedZipFile(data);
            }
          }),
          catchError((error) => of(error)),
        );
      });
  }
  reinitializeZip() {
    for (const file of this.filesToUpload) {
      this.zipFile.remove(file.name);
    }
    this.filesToUpload = [];
    this.uploadingFiles$.next(this.filesToUpload);
  }

  async handleUpload(files: any) {
    if (files.length === 0) {
      return;
    }
    this.uploadInfo(CollectUploadService.uploadFilesInfo(files));
    // tslint:disable-next-line:prefer-for-of
    for (let i = 0; i < files.length; i++) {
      const item = files[i];
      this.zipFile.file(item.webkitRelativePath, item);
    }
  }

  async handleDragAndDropUpload(files: any[]) {
    const [uploadFile] = await Promise.all([this.dragAndDropUploadFilesInfo(files), this.buildAsyncZip(files)]);
    this.uploadInfo(uploadFile);
  }

  /*** Private methods ***/
  private updateUploadedZipFile(data: HttpEvent<any>) {
    let progressPercent = 0;

    switch (data.type) {
      case HttpEventType.UploadProgress:
        progressPercent = Math.round((data.loaded / data.total) * 100);
        break;
      case HttpEventType.Response:
        progressPercent = 100;
        break;
    }

    this.zippedFile.uploadedSize = progressPercent;
    this.watchZippedFile$.next(this.zippedFile);
  }

  private updateInternalZipFile(currentZipFile: string, progress: number) {
    this.zippedFile.currentFile = currentZipFile;
    this.zippedFile.currentFileUploadedSize = progress;
    this.watchZippedFile$.next(this.zippedFile);
  }

  private async buildAsyncZip(files: any) {
    // tslint:disable-next-line:prefer-for-of
    for (let i = 0; i < files.length; i++) {
      const item = files[i].webkitGetAsEntry();
      if (item) {
        await this.parse(this.zipFile, item);
      }
    }
  }

  private async dragAndDropUploadFilesInfo(files: any) {
    const name = CollectUploadService.dragAndDropUploadFilesDirectoryName(files);
    let size = 0;
    // tslint:disable-next-line:prefer-for-of
    for (let i = 0; i < files.length; i++) {
      const item = files[i].webkitGetAsEntry();
      if (item) {
        size += await this.calcDragAndDropUploadFilesSize(item);
      }
    }
    return { name, size, dragged: true } as CollectUploadFile;
  }

  private async calcDragAndDropUploadFilesSize(item: any) {
    if (item.isDirectory) {
      let size = 0;
      const dirEntries: any[] = await this.parseDirectoryEntry(item);
      for (const entry of dirEntries) {
        size += await this.calcDragAndDropUploadFilesSize(entry);
      }
      return size;
    } else {
      const f = await this.parseFileEntry(item);
      return f.size;
    }
  }

  private async parse(zip: JSZip, item: any) {
    if (item.isDirectory) {
      const dirEntries: any[] = await this.parseDirectoryEntry(item);
      if (dirEntries.length === 0) {
        zip.folder(item.fullPath.substring(1));
      }
      for (const entry of dirEntries) {
        await this.parse(zip, entry);
      }
    } else {
      const f = await this.parseFileEntry(item);
      zip.file(item.fullPath.substring(1), f);
    }
  }

  private parseFileEntry(fileEntry: any): Promise<any> {
    return new Promise((resolve, reject) => fileEntry.file(resolve, reject));
  }

  private async parseDirectoryEntry(directoryEntry: any): Promise<any[]> {
    const directoryReader = directoryEntry.createReader();
    const entries = [];
    let batch;
    // We have to call readEntries several times until it returns an empty list to make sure we read all entries (otherwise it is limited to 100 entries)
    while ((batch = await new Promise<any[]>((resolve, reject) => directoryReader.readEntries(resolve, reject))).length) {
      entries.push(...batch);
    }
    return entries;
  }

  private uploadInfo(uploadFile: CollectUploadFile) {
    this.filesToUpload.push(uploadFile);
    this.uploadingFiles$.next(this.filesToUpload);
  }
}
