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
import { Component, inject, OnDestroy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import {
  ApplicationId,
  DialogHeaderComponent,
  FileSelectorComponent,
  FileTypes,
  FileValidationErrors,
  FileValidatorFunction,
  SnackBarService,
} from 'vitamui-library';
import { finalize, firstValueFrom, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ImportDialogParam, ReferentialTypes } from './import-dialog-param.interface';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ReferentialImportService } from './referential-import.service';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-import-dialog',
  templateUrl: './import-dialog.component.html',
  styleUrls: ['./import-dialog.component.scss'],
  imports: [
    DialogHeaderComponent,
    MatDialogContent,
    FileSelectorComponent,
    ReactiveFormsModule,
    MatDialogActions,
    MatProgressSpinner,
    TranslatePipe,
  ],
})
export class ImportDialogComponent implements OnDestroy {
  dialogParams = inject<ImportDialogParam>(MAT_DIALOG_DATA);
  dialogRef = inject<MatDialogRef<ImportDialogComponent>>(MatDialogRef);
  private readonly referentialImportService = inject(ReferentialImportService);
  private readonly snackBarService = inject(SnackBarService);
  translateService = inject(TranslateService);

  public isLoading = false;
  private readonly destroy = new Subject<void>();
  extensions: string[] = [];
  usesCsvValidator: boolean;

  fileControl = new FormControl<File[]>(undefined, [Validators.required]);

  constructor() {
    this.setExtensions();
    this.enableCsvValidator();
  }

  public submitFile(): void {
    this.isLoading = true;
    this.referentialImportService
      .importReferential(this.dialogParams.referential, this.fileControl.value[0])
      .pipe(takeUntil(this.destroy))
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: () => {
          this.snackBarService.open({
            message: this.dialogParams.successMessage,
            icon: this.dialogParams.iconMessage,
            buttons: [
              {
                appId: ApplicationId.LOGBOOK_OPERATION_APP,
                label: 'SNACKBAR.VIEW_THE_OPERATIONS_LOG',
              },
            ],
          });

          this.dialogRef.close({ successfulImport: true });
        },
        error: (_) => {
          let showSnackbar = true;
          if (showSnackbar && this.dialogParams.errorMessage) {
            this.snackBarService.open({
              message: this.dialogParams.errorMessage,
              icon: this.dialogParams.iconMessage,
            });
          }
        },
      });
  }

  public cancel(): void {
    this.dialogRef.close();
  }

  csvValidator: FileValidatorFunction = async (file: File, hasErrors: boolean): Promise<FileValidationErrors> => {
    if (hasErrors) return null;

    let errorsDuringImport: string[] = [];

    try {
      await firstValueFrom(this.referentialImportService.getCSVCheckResults(this.dialogParams.referential, file));
      return null;
    } catch (error: any) {
      if (error) {
        if (error.error.args) {
          (error.error.args as string[]).forEach((arg) => {
            const jsonArg = JSON.parse(arg);
            errorsDuringImport.push(
              this.translateService.instant('IMPORT_DIALOG.IMPORT_ERROR_MESSAGE.' + jsonArg.error, {
                line: jsonArg.line,
                column: jsonArg.column,
                data: jsonArg.data,
              }),
            );
          });
        }
      }
      return {
        fileErrors: {
          invalidCsv: true,
        },
        controlErrors: {
          invalidCsvDetail: {
            detail: errorsDuringImport.map((error) => `<li>${error}</li>`).join(''),
          },
        },
      };
    }
  };

  public setExtensions() {
    const allowedFiles = this.dialogParams.allowedFiles;
    this.extensions = [
      ...(allowedFiles.includes(FileTypes.JSON) ? ['.json'] : []),
      ...(allowedFiles.includes(FileTypes.XML) ? ['.xml'] : []),
      ...(allowedFiles.includes(FileTypes.CSV) || allowedFiles.includes(FileTypes.VND) ? ['.csv'] : []),
    ];
  }

  public enableCsvValidator() {
    this.usesCsvValidator = [ReferentialTypes.ACCESS_CONTRACTS, ReferentialTypes.INGEST_CONTRACT, ReferentialTypes.SCHEMA_UNIT].includes(
      this.dialogParams.referential,
    );
  }

  ngOnDestroy() {
    this.destroy.next();
    this.destroy.complete();
  }
}
