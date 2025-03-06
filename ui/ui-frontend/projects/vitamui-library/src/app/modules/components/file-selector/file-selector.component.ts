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
import { Component, ContentChild, ElementRef, EventEmitter, Input, Output, TemplateRef, ViewChild } from '@angular/core';
import { DragAndDropDirective } from '../../directives/drag-and-drop/drag-and-drop.directive';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NgForOf, NgIf, NgTemplateOutlet } from '@angular/common';
import { PipesModule } from '../../pipes/pipes.module';
import { DisplayFile } from './display-file.interface';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomFile } from '../../../../lib/models/custom-file';

@Component({
  selector: 'vitamui-file-selector',
  templateUrl: './file-selector.component.html',
  styleUrl: './file-selector.component.scss',
  standalone: true,
  imports: [DragAndDropDirective, TranslateModule, NgIf, NgForOf, PipesModule, NgTemplateOutlet],
})
export class FileSelectorComponent {
  /**
   * Allowed extensions. Ex: ['.json', '.rng']
   */
  @Input() extensions?: string[];
  /** Allows to select multiple files. Automatically set to true in directoryMode **/
  @Input() set multiple(multiple: boolean) {
    this.#multiple = multiple;
  }
  get multiple() {
    return this.#multiple || this.directoryMode;
  }
  #multiple = false;
  /** Only directories can be selected through OS picker. Drag&Drop allows both directories and files */
  @Input() directoryMode = false;
  @Input() maxSizeInBytes: number; // TODO: do some control on the file size?

  @ContentChild('fileList') fileList: TemplateRef<any>;
  @ContentChild('content') content: TemplateRef<any>;

  @Output() filesChanged = new EventEmitter<File[]>();

  @ViewChild('inputFiles') inputFiles: ElementRef;

  files: File[] = [];
  displayFiles: DisplayFile[] = [];

  constructor(
    private translationService: TranslateService,
    public snackBar: MatSnackBar,
  ) {}

  handleFilesSelection(files: FileList | File[]) {
    if (!this.multiple && (this.files?.length > 0 || files.length > 1)) {
      this.resetInput();
      return;
    }

    if (this.hasDuplicateRootElement(files)) {
      this.snackBar.open(this.translationService.instant('COLLECT.UPLOAD_FILE_ALREADY_IMPORTED'), null, {
        panelClass: 'vitamui-snack-bar',
        duration: 10000,
      });
      this.resetInput();
      return;
    }

    // Filter to keep only the ones matching extension list (useful for drag & drop and to make sure no other type has been selected)
    const filteredFiles = Array.from(files)
      .filter((file) => !this.extensions?.length || this.extensions.some((ext) => file.name.toLowerCase().endsWith(ext.toLowerCase())))
      .slice(0, this.multiple ? undefined : 1);
    this.files.push(...filteredFiles);
    if (this.directoryMode) {
      this.displayFiles.push(...this.getRootElements(files));
    } else {
      const displayFiles: DisplayFile[] = Array.from(filteredFiles).map(
        (file: File): DisplayFile => ({
          name: file.name,
          size: file.size,
          directory: false,
        }),
      );
      this.displayFiles.push(...displayFiles);
    }
    this.filesChanged.emit(this.files);
    this.resetInput();
  }

  openFileSelectorOSDialog() {
    this.inputFiles.nativeElement.click();
  }

  removeFile(displayFile: DisplayFile) {
    if (displayFile.directory) {
      this.files = this.files.filter((file: CustomFile) => !this.getPath(file).startsWith(displayFile.name));
    } else {
      this.files.splice(
        this.files.findIndex((file) => file.name === displayFile.name),
        1,
      );
    }
    this.filesChanged.emit(this.files);
    this.displayFiles.splice(this.displayFiles.indexOf(displayFile), 1);
  }

  /**
   * Reset the value to allow a new "change" event.
   */
  private resetInput(): void {
    if (this.inputFiles) {
      this.inputFiles.nativeElement.value = '';
    }
  }

  private getRootElements(files: FileList | File[]): DisplayFile[] {
    if (files.length === 0) return [];

    const rootElementsMap = new Map<string, DisplayFile>();

    Array.from(files).forEach((file: CustomFile) => {
      const path = this.getPath(file);
      const rootPath = path.split('/')[0];
      rootElementsMap.set(rootPath, {
        name: rootPath,
        size: (rootElementsMap.get(rootPath)?.size || 0) + (file?.size || 0),
        directory: rootElementsMap.get(rootPath)?.directory || rootPath !== path,
      });
    });

    return Array.from(rootElementsMap.values());
  }

  /** We need the file path, so we use the webkitRelativePath attribute when loading a folder via the native HTML file selector,
   the relativePath attribute in the case of drag & drop, and the name attribute when uploading a file. */
  private getPath(file: CustomFile) {
    return file?.webkitRelativePath || file?.relativePath || file?.name;
  }

  private hasDuplicateRootElement(files: FileList | File[]): boolean {
    const rootElementNames = [...new Set(Array.from(files).map((file: CustomFile) => this.getPath(file)?.split('/')[0] || file.name))];
    return rootElementNames.some((rootElementName) =>
      this.displayFiles.some((displayElement) => displayElement.name?.toLowerCase() === rootElementName?.toLowerCase()),
    );
  }
}
