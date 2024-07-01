import { Component, Inject, OnDestroy } from '@angular/core';
import { MatLegacyDialogRef as MatDialogRef, MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';
import { FileTypes } from 'projects/vitamui-library/src/public-api';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { VitamUISnackBarService, DragAndDropDirective } from 'vitamui-library';
import { ImportDialogParam, ImportError } from './import-dialog-param.interface';
import { ReferentialImportService } from './referential-import.service';
import { TranslateModule } from '@ngx-translate/core';
import { MatLegacyProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { NgIf, NgClass, NgFor } from '@angular/common';

@Component({
  selector: 'app-import-dialog',
  templateUrl: './import-dialog.component.html',
  styleUrls: ['./import-dialog.component.scss'],
  standalone: true,
  imports: [NgIf, DragAndDropDirective, NgClass, NgFor, MatLegacyProgressSpinnerModule, TranslateModule],
})
export class ImportDialogComponent implements OnDestroy {
  public hasDropZoneOver = false;
  public fileToUpload: File;
  public hasWrongFormat = false;
  public isLoading = false;
  public errorsDuringImport: ImportError[] = [];
  private destroy = new Subject<void>();

  constructor(
    @Inject(MAT_DIALOG_DATA) public dialogParams: ImportDialogParam,
    public dialogRef: MatDialogRef<ImportDialogComponent>,
    private referentialImportService: ReferentialImportService,
    private snackBarService: VitamUISnackBarService,
  ) {}

  public submitFile(): void {
    this.isLoading = true;
    this.errorsDuringImport = [];
    this.hasWrongFormat = false;
    this.referentialImportService
      .importReferential(this.dialogParams.referential, this.fileToUpload)
      .pipe(takeUntil(this.destroy))
      .subscribe(
        () => {
          this.snackBarService.open({ message: this.dialogParams.successMessage, icon: this.dialogParams.iconMessage });
          this.dialogRef.close({ successfulImport: true });
        },
        (error) => {
          this.isLoading = false;

          if (this.dialogParams.errorMessage) {
            this.snackBarService.open({ message: this.dialogParams.errorMessage, icon: this.dialogParams.iconMessage });
          }

          if (error.error) {
            const errorJson = JSON.parse(error.error);
            if (errorJson.args) {
              (errorJson.args as []).forEach((arg) => {
                this.errorsDuringImport.push(JSON.parse(arg));
              });
            }
          }
        },
      );
  }

  public cancel(): void {
    this.dialogRef.close();
  }

  public onFileDropped(files: File[]): void {
    this.hasDropZoneOver = false;
    this.handleFiles(files);
  }

  public onFileDragOver(inDropZone: boolean): void {
    this.hasDropZoneOver = inDropZone;
  }

  public onFileDragLeave(inDropZone: boolean): void {
    this.hasDropZoneOver = inDropZone;
  }

  public handleFiles(files: File[]): void {
    if (!files.length) {
      return;
    }
    this.fileToUpload = null;
    this.errorsDuringImport = [];

    const file = files[0];
    if (this.isAllowedFileType(file.type)) {
      this.fileToUpload = file;
    } else {
      this.hasWrongFormat = true;
    }
  }

  public calculateFileSize(file: File): string {
    return (file.size / 1024).toFixed(0);
  }

  public removeFile(): void {
    this.hasWrongFormat = false;
    this.fileToUpload = null;
    this.errorsDuringImport = [];
  }

  private isAllowedFileType(type: string): boolean {
    return this.dialogParams.allowedFiles.includes(type as FileTypes);
  }

  ngOnDestroy() {
    this.destroy.next();
    this.destroy.complete();
  }
}
