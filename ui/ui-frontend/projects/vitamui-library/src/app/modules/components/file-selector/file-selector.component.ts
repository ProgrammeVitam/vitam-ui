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
import { TranslateModule } from '@ngx-translate/core';
import { NgForOf, NgIf, NgTemplateOutlet } from '@angular/common';
import { PipesModule } from '../../pipes/pipes.module';
import { DisplayFile } from './display-file.interface';

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
  @Input() multipleFiles = false;
  /** only a directory can be chosen */
  @Input() directoryMode = false;
  @Input() maxSizeInBytes: number; // TODO: do some control on the file size?

  @ViewChild('inputFiles') inputFiles: ElementRef;

  @ContentChild('fileList') fileList: TemplateRef<any>;
  @ContentChild('content') content: TemplateRef<any>;

  @Output() filesChanged = new EventEmitter<File[]>();

  private files: File[] = [];
  protected displayFiles: DisplayFile[] = [];

  handleFilesSelection(files: FileList | File[]) {
    if (!this.multipleFiles && this.files.length > 0) {
      return;
    }
    // Filter to keep only the ones matching extension list (useful for drag & drop and to make sure no other type has been selected)
    const filteredFiles = Array.from(files)
      .filter((file) => !this.extensions?.length || this.extensions.some((ext) => file.name.toLowerCase().endsWith(ext.toLowerCase())))
      .slice(0, this.multipleFiles ? undefined : 1);
    this.files.push(...filteredFiles);
    const directory = this.getDirectory(files);
    if (this.directoryMode && directory) {
      this.displayFiles.push(directory);
    } else {
      const displayFiles: DisplayFile[] = Array.from(filteredFiles).map((file: File): DisplayFile => {
        return {
          name: file.name,
          size: file.size,
          directory: false,
        };
      });
      this.displayFiles.push(...displayFiles);
    }
    this.filesChanged.emit(this.files);
    this.resetInput();
  }

  private getDirectory(files: FileList | File[]): DisplayFile | null {
    if (!files || files.length === 0) return null;

    const firstFile = files[0];
    const relativePath = firstFile.webkitRelativePath;

    if (relativePath && relativePath.includes('/')) {
      const directoryName = relativePath.split('/')[0];

      const totalSize = Array.from(files).reduce((total, file) => {
        return total + (file.size || 0);
      }, 0);

      return {
        name: directoryName,
        size: totalSize,
        directory: true,
      };
    }

    return null;
  }

  openFileSelectorOSDialog() {
    this.inputFiles.nativeElement.click();
  }

  removeFile(displayFile: DisplayFile) {
    if (displayFile.directory) {
      this.files = this.files.filter((file) => !file.webkitRelativePath.startsWith(displayFile.name));
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
}
