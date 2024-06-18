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

import { Directive, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { Logger } from '../../logger/logger';

@Directive({
  selector: '[vitamuiCommonDragAndDrop]',
})
export class DragAndDropDirective {
  @Output() private fileToUploadEmitter: EventEmitter<File[]> = new EventEmitter();
  @Output() private fileDragOverEmitter: EventEmitter<boolean> = new EventEmitter();
  @Output() private fileDragLeaveEmitter: EventEmitter<boolean> = new EventEmitter();

  // Disable dropping on the body of the document.
  // This prevents the browser from loading the dropped files
  // using it's default behaviour if the user misses the drop zone.
  // Set this input to false if you want the browser default behaviour.
  @Input() preventBodyDrop = true;

  @Input() enableFileDragAndDrop = true;

  @Input() enableFolderDragAndDrop = false;

  constructor(public logger: Logger) {}

  @HostListener('dragover', ['$event']) public onDragOver(dragOverEvent: any) {
    dragOverEvent.preventDefault();
    dragOverEvent.stopPropagation();
    this.fileDragOverEmitter.emit(true);
  }

  @HostListener('dragleave', ['$event']) public onDragLeave(dragLeaveEvent: any) {
    dragLeaveEvent.preventDefault();
    dragLeaveEvent.stopPropagation();
    this.fileDragLeaveEmitter.emit(false);
  }

  @HostListener('drop', ['$event']) public async onDrop(dropEvent: any) {
    dropEvent.preventDefault();
    dropEvent.stopPropagation();

    let fileEntries = await this.getAllFileEntries(dropEvent.dataTransfer.items);
    // Filter files
    if (!this.enableFileDragAndDrop) {
      fileEntries = fileEntries.filter((fileEntry) => fileEntry.fullPath.split('/').length - 1 !== 1);
    }
    // Filter folders
    if (!this.enableFolderDragAndDrop) {
      fileEntries = fileEntries.filter((fileEntry) => fileEntry.fullPath.split('/').length - 1 === 1);
    }

    Promise.all(
      fileEntries.map(async (fileEntry) => {
        const file: File = await this.getFile(fileEntry);
        // Add relative path to folder files
        if (fileEntry.fullPath.split('/').length - 1 !== 1) {
          (file as any).relativePath = fileEntry.fullPath?.substring(1);
        }
        return file;
      }),
    ).then((files) => {
      this.fileToUploadEmitter.emit(files);
    });
    dropEvent.stopPropagation();
    dropEvent.preventDefault();
  }

  @HostListener('body:dragover', ['$event'])
  onBodyDragOver(event: DragEvent) {
    if (this.preventBodyDrop) {
      event.preventDefault();
      event.stopPropagation();
    }
  }
  @HostListener('body:drop', ['$event'])
  onBodyDrop(event: DragEvent) {
    if (this.preventBodyDrop) {
      event.preventDefault();
    }
  }

  private getFile = async (fileEntry: any): Promise<File> => {
    try {
      return new Promise((resolve, reject) => fileEntry.file(resolve, reject));
    } catch (err) {
      this.logger.error(this, err);
    }
  };

  private getAllFileEntries = async (dataTransferItemList: DataTransferItemList) => {
    const fileEntries = [];
    // Use BFS to traverse entire directory/file structure
    const queue = [];
    // Unfortunately dataTransferItemList is not iterable i.e. no forEach
    // eslint-disable-next-line @typescript-eslint/prefer-for-of
    for (let i = 0; i < dataTransferItemList.length; i++) {
      // Note webkitGetAsEntry a non-standard feature and may change
      // Usage is necessary for handling directories
      queue.push(dataTransferItemList[i].webkitGetAsEntry());
    }
    while (queue.length > 0) {
      const entry: any = queue.shift();
      if (entry.isFile) {
        fileEntries.push(entry);
      } else if (entry.isDirectory) {
        const reader = entry.createReader();
        queue.push(...(await this.readAllDirectoryEntries(reader)));
      }
    }
    return fileEntries;
  };

  // Get all the entries (files or sub-directories) in a directory by calling readEntries until it returns empty array
  private readAllDirectoryEntries = async (directoryReader: any) => {
    const entries = [];
    let readEntries: File[] = await this.readEntriesPromise(directoryReader);
    while (readEntries.length > 0) {
      entries.push(...readEntries);
      readEntries = await this.readEntriesPromise(directoryReader);
    }
    return entries;
  };

  // Wrap readEntries in a promise to make working with readEntries easier
  private readEntriesPromise = async (directoryReader: any): Promise<File[]> => {
    try {
      return await new Promise((resolve, reject) => {
        directoryReader.readEntries(resolve, reject);
      });
    } catch (err) {
      this.logger.error(this, err);
    }
  };
}
