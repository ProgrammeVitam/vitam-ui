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
import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { SafeResourceUrl } from '@angular/platform-browser';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'vitamui-common-drag-drop-file',
  templateUrl: './vitamui-drag-drop-file.component.html',
  styleUrls: ['./vitamui-drag-drop-file.component.scss'],
})
export class VitamuiDragDropFileComponent {
  private IMAGE_TYPE_PREFIX = 'image';
  public hasDropZoneOver = false;
  private imageToUpload: File = null;
  private lastImageUploaded: File = null;
  public hasError = true;
  public message: string;

  private _imageUrl: string | SafeResourceUrl;
  public get imageUrl(): string | SafeResourceUrl {
    return this._imageUrl;
  }
  public set imageUrl(val: string | SafeResourceUrl) {
    this._imageUrl = val;
    if (this.imageToUpload) {
      this.file.next(this.imageToUpload);
    }
  }

  @Input()
  public canDelete = true;

  @Input()
  public messContent = 'Drag & drop';

  @Input()
  public disabled = false;

  @Input()
  public messPj = 'Attach';

  @Input()
  public set fileUrl(url: SafeResourceUrl) {
    if (url) {
      this.imageUrl = url;
    }
  }

  @Input()
  public logoSize: { width: number; height: number } = { width: 1000, height: 1000 };

  @Output()
  public file = new EventEmitter<File>();

  @Output()
  public delete = new EventEmitter<void>();

  @ViewChild('fileSearch', { static: false }) fileSearch: any;

  constructor(private translateService: TranslateService) {}

  public onImageDropped(files: File[]): void {
    this.hasDropZoneOver = false;
    this.handleImage(files);
  }

  private handleImage(files: File[]): void {
    this.hasError = false;
    this.lastImageUploaded = this.imageToUpload;
    this.message = null;
    this.imageToUpload = files[0];
    if (this.imageToUpload.type.split('/')[0] !== this.IMAGE_TYPE_PREFIX) {
      this.message = this.translateService.instant('DRAG_AND_DROP_FILE.FILE_IS_NOT_AN_IMAGE');
      this.hasError = true;
      return;
    }
    const reader = new FileReader();
    const logoImage = new Image();
    reader.onload = () => {
      const logoUrl = reader.result;
      logoImage.src = logoUrl as string;
      logoImage.onload = () => {
        if (logoImage.width > this.logoSize.width || logoImage.height > this.logoSize.height) {
          this.imageToUpload = this.lastImageUploaded;
          // eslint-disable-next-line max-len
          this.message = this.translateService.instant('DRAG_AND_DROP_FILE.WRONG_FILE_SIZE', {
            width: this.logoSize.width,
            height: this.logoSize.height,
          });
          this.hasError = true;
        } else {
          this.imageUrl = logoUrl as string;
        }
      };
    };
    reader.readAsDataURL(this.imageToUpload);
  }

  public onImageDragOver(inDropZone: boolean): void {
    this.hasDropZoneOver = inDropZone;
  }

  public onImageDragLeave(inDropZone: boolean): void {
    this.hasDropZoneOver = inDropZone;
  }

  public addLogo(): void {
    this.fileSearch.nativeElement.click();
  }

  public onDelete(): void {
    this.imageToUpload = null;
    this.imageUrl = null;
    this.delete.next();
  }

  public handleFileInput(files: FileList): void {
    this.handleImageList(files);
  }

  private handleImageList(files: FileList): void {
    this.hasError = false;
    this.lastImageUploaded = this.imageToUpload;
    this.message = null;
    this.imageToUpload = files.item(0);
    if (this.imageToUpload.type.split('/')[0] !== this.IMAGE_TYPE_PREFIX) {
      this.message = this.translateService.instant('DRAG_AND_DROP_FILE.FILE_IS_NOT_AN_IMAGE');
      this.hasError = true;
      return;
    }
    const reader = new FileReader();
    const logoImage = new Image();
    reader.onload = () => {
      const logoUrl = reader.result;
      logoImage.src = logoUrl as string;
      logoImage.onload = () => {
        if (logoImage.width > this.logoSize.width || logoImage.height > this.logoSize.height) {
          this.imageToUpload = this.lastImageUploaded;
          // eslint-disable-next-line max-len
          this.message = this.translateService.instant('DRAG_AND_DROP_FILE.WRONG_FILE_SIZE', {
            width: this.logoSize.width,
            height: this.logoSize.height,
          });
          this.hasError = true;
        } else {
          this.imageUrl = logoUrl as string;
        }
      };
    };
    reader.readAsDataURL(this.imageToUpload);
  }
}
