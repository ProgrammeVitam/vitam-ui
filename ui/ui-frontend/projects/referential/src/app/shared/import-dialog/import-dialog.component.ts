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
import { Component, Inject, OnDestroy } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { FileTypes } from 'projects/vitamui-library/src/public-api';
import { finalize, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { VitamUISnackBarService, ApplicationId } from 'vitamui-library';
import { ImportDialogParam, ImportError } from './import-dialog-param.interface';
import { ReferentialImportService } from './referential-import.service';

@Component({
  selector: 'app-import-dialog',
  templateUrl: './import-dialog.component.html',
  styleUrls: ['./import-dialog.component.scss'],
})
export class ImportDialogComponent implements OnDestroy {
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
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: () => {
          this.snackBarService
            .openWithAppUrlBtn(
              {
                message: this.dialogParams.successMessage,
                icon: this.dialogParams.iconMessage,
              },
              ApplicationId.LOGBOOK_OPERATION_APP,
              'SNACKBAR.VIEW_THE_OPERATIONS_LOG',
            )
            .subscribe();

          this.dialogRef.close({ successfulImport: true });
        },
        error: (error) => {
          if (this.dialogParams.errorMessage) {
            this.snackBarService
              .openWithAppUrlBtn(
                {
                  message: this.dialogParams.errorMessage,
                  icon: this.dialogParams.iconMessage,
                },
                ApplicationId.LOGBOOK_OPERATION_APP,
                'SNACKBAR.VIEW_THE_OPERATIONS_LOG',
              )
              .subscribe();
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
      });
  }

  public cancel(): void {
    this.dialogRef.close();
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
