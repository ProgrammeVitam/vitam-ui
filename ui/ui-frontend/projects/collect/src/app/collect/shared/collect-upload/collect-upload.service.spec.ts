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
import { HttpEventType, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { VitamuiHttpHeaders } from 'vitamui-library';
import { CollectUploadFile, CollectZippedUploadFile } from './collect-upload-file';
import { CollectUploadService } from './collect-upload.service';

const fileWithPath = (path: string, content: string): File => {
  const file = new File([content], path.split('/').pop());
  Object.defineProperty(file, 'webkitRelativePath', { value: path });
  return file;
};

const fileList = (...files: File[]): FileList => {
  const list: any = { length: files.length, item: (i: number) => files[i] };
  files.forEach((f, i) => (list[i] = f));
  return list as FileList;
};

const fileEntry = (fullPath: string, content: string, failWith?: Error): FileSystemFileEntry =>
  ({
    isDirectory: false,
    isFile: true,
    fullPath,
    file: (resolve: (f: File) => void, reject: (e: Error) => void) =>
      failWith ? reject(failWith) : resolve(new File([content], fullPath.split('/').pop())),
  }) as unknown as FileSystemFileEntry;

/** Directory entry whose reader returns its children in batches, then an empty batch (like browsers do). */
const dirEntry = (fullPath: string, batches: FileSystemEntry[][]): FileSystemDirectoryEntry =>
  ({
    isDirectory: true,
    isFile: false,
    fullPath,
    createReader: () => {
      const remaining = [...batches];
      return { readEntries: (resolve: (e: FileSystemEntry[]) => void) => resolve(remaining.shift() ?? []) };
    },
  }) as unknown as FileSystemDirectoryEntry;

const itemList = (...entries: FileSystemEntry[]): DataTransferItemList => {
  const list: any = { length: entries.length };
  entries.forEach((e, i) => (list[i] = { webkitGetAsEntry: () => e }));
  return list as DataTransferItemList;
};

describe('CollectUploadService', () => {
  let service: CollectUploadService;
  let httpTesting: HttpTestingController;

  const currentUploadingFiles = () => firstValueFrom(service.getUploadingFiles());
  const zipPaths = () => Object.keys(service.zipFile.files).sort();

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(CollectUploadService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  describe('handleUpload (folder selection)', () => {
    it('should ignore an empty selection', async () => {
      await service.handleUpload(fileList());

      expect(await currentUploadingFiles()).toEqual([]);
      expect(zipPaths()).toEqual([]);
    });

    it('should register the top-level folder with the total size and add every file to the zip', async () => {
      await service.handleUpload(fileList(fileWithPath('folder/a.txt', 'abc'), fileWithPath('folder/sub/b.txt', 'hello')));

      expect(await currentUploadingFiles()).toEqual([{ name: 'folder', size: 8, dragged: false }]);
      expect(zipPaths()).toEqual(['folder/', 'folder/a.txt', 'folder/sub/', 'folder/sub/b.txt']);
      expect(await service.zipFile.file('folder/sub/b.txt').async('string')).toBe('hello');
    });

    it('should use the whole relative path as name when the first file has no folder', async () => {
      await service.handleUpload(fileList(fileWithPath('single.txt', 'x')));

      expect((await currentUploadingFiles())[0].name).toBe('single.txt');
    });
  });

  describe('handleDragAndDropUpload', () => {
    it('should recursively zip dropped folders, keep empty folders and compute the total size', async () => {
      const root = dirEntry('/dropped', [
        [fileEntry('/dropped/a.txt', 'abcd'), dirEntry('/dropped/empty', [])],
        [dirEntry('/dropped/sub', [[fileEntry('/dropped/sub/b.txt', 'xy')]])],
      ]);

      await service.handleDragAndDropUpload(itemList(root));

      expect(await currentUploadingFiles()).toEqual([{ name: 'dropped', size: 6, dragged: true }]);
      expect(zipPaths()).toEqual(['dropped/', 'dropped/a.txt', 'dropped/empty/', 'dropped/sub/', 'dropped/sub/b.txt']);
      expect(await service.zipFile.file('dropped/sub/b.txt').async('string')).toBe('xy');
    });

    it('should skip items that are not file system entries', async () => {
      const items: any = itemList(fileEntry('/a.txt', 'aa'));
      items[1] = { webkitGetAsEntry: (): FileSystemEntry => null };
      items.length = 2;

      await service.handleDragAndDropUpload(items);

      expect(await currentUploadingFiles()).toEqual([{ name: 'a.txt', size: 2, dragged: true }]);
      expect(zipPaths()).toEqual(['a.txt']);
    });

    it('should reject and not register anything when a file cannot be read', async () => {
      const error = new Error('NotReadableError');

      await expect(service.handleDragAndDropUpload(itemList(fileEntry('/broken.txt', '', error)))).rejects.toBe(error);
      expect(await currentUploadingFiles()).toEqual([]);
    });
  });

  describe('directoryExistInZipFile', () => {
    beforeEach(async () => {
      await service.handleUpload(fileList(fileWithPath('existing/a.txt', 'a')));
    });

    it('should detect an already zipped folder from a folder selection', () => {
      expect(service.directoryExistInZipFile(fileList(fileWithPath('existing/other.txt', 'b')), false)).toBe(true);
      expect(service.directoryExistInZipFile(fileList(fileWithPath('new/other.txt', 'b')), false)).toBe(false);
    });

    it('should detect an already zipped folder from a drop', () => {
      expect(service.directoryExistInZipFile(itemList(dirEntry('/existing', [])), true)).toBe(true);
      expect(service.directoryExistInZipFile(itemList(dirEntry('/exist', [])), true)).toBe(false);
    });

    it('should not match a file name against folders', () => {
      expect(service.directoryExistInZipFile(itemList(fileEntry('/existing/a.txt', '')), true)).toBe(false);
    });
  });

  describe('removeFolder / reinitializeZip', () => {
    let files: CollectUploadFile[];

    beforeEach(async () => {
      await service.handleUpload(fileList(fileWithPath('one/a.txt', 'a')));
      await service.handleUpload(fileList(fileWithPath('two/b.txt', 'b')));
      files = await currentUploadingFiles();
    });

    it('should remove the folder from the zip and from the uploading list', async () => {
      service.removeFolder(files[0]);

      expect((await currentUploadingFiles()).map((f) => f.name)).toEqual(['two']);
      expect(zipPaths()).toEqual(['two/', 'two/b.txt']);
    });

    it('should ignore a folder that is not in the list', async () => {
      service.removeFolder({ name: 'one', size: 1, dragged: false });

      expect((await currentUploadingFiles()).length).toBe(2);
      expect(zipPaths()).toContain('one/a.txt');
    });

    it('should empty both the list and the zip on reinitialize', async () => {
      service.reinitializeZip();

      expect(await currentUploadingFiles()).toEqual([]);
      expect(zipPaths()).toEqual([]);
    });
  });

  describe('uploadZip', () => {
    let zipStates: CollectZippedUploadFile[];

    beforeEach(async () => {
      await service.handleUpload(fileList(fileWithPath('folder/a.txt', 'abc')));
      await service.handleUpload(fileList(fileWithPath('other/b.txt', 'hello')));
      zipStates = [];
      service.getZipFile().subscribe((z) => zipStates.push(z ? { ...z } : z));
    });

    it('should POST the generated zip with transaction headers and track progress until the response', async () => {
      const upload$ = await service.uploadZip('tx-1');

      expect(zipStates.at(-1)).toEqual(expect.objectContaining({ name: 'tx-1.zip', size: 8, currentFileUploadedSize: 100 }));
      expect(zipStates.some((z) => z?.currentFile === 'folder/a.txt')).toBe(true);

      const events: any[] = [];
      upload$.subscribe((e) => events.push(e));
      const req = httpTesting.expectOne('./collect-api/projects/upload');

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBeInstanceOf(Blob);
      expect(req.request.reportProgress).toBe(true);
      expect(req.request.responseType).toBe('text');
      expect(req.request.headers.get(VitamuiHttpHeaders.X_TRANSACTION_ID)).toBe('tx-1');
      expect(req.request.headers.get(VitamuiHttpHeaders.X_ORIGINAL_FILENAME)).toBe('tx-1.zip');
      expect(req.request.headers.get('Content-Type')).toBe('application/octet-stream');

      req.event({ type: HttpEventType.UploadProgress, loaded: 25, total: 100 });
      expect(zipStates.at(-1).uploadedSize).toBe(25);

      req.flush('OK');
      expect(zipStates.at(-1).uploadedSize).toBe(100);
      expect(events.at(-1).body).toBe('OK');
    });

    it('should round the upload progress percentage', async () => {
      const upload$ = await service.uploadZip('tx-2');
      upload$.subscribe();
      const req = httpTesting.expectOne('./collect-api/projects/upload');

      req.event({ type: HttpEventType.UploadProgress, loaded: 1, total: 3 });

      expect(zipStates.at(-1).uploadedSize).toBe(33);
      req.flush('');
    });

    it('should emit the error instead of failing the stream when the upload fails', async () => {
      const upload$ = await service.uploadZip('tx-3');
      const events: any[] = [];
      let failed = false;
      upload$.subscribe({ next: (e) => events.push(e), error: () => (failed = true) });

      httpTesting.expectOne('./collect-api/projects/upload').flush('quota', { status: 507, statusText: 'Insufficient Storage' });

      expect(failed).toBe(false);
      expect(events.at(-1).status).toBe(507);
    });
  });
});
