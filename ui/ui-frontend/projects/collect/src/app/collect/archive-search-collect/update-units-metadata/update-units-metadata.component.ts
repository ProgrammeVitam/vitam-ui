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
 *
 *
 */

import { Component, Inject, OnDestroy, TemplateRef, ViewChild } from '@angular/core';
import {
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
} from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { BytesPipe, Logger, Transaction } from 'vitamui-library';
import { VitamUISnackBarComponent } from '../../shared/vitamui-snack-bar/vitamui-snack-bar.component';
import { ArchiveCollectService } from '../archive-collect.service';

const CSV_EXTENSION = '.csv';
const VITAMUI_SNACK_BAR = 'vitamui-snack-bar';

@Component({
  selector: 'app-update-units-metadata',
  templateUrl: './update-units-metadata.component.html',
  styleUrls: ['./update-units-metadata.component.scss'],
})
export class UpdateUnitsaMetadataComponent implements OnDestroy {
  public stepIndex = 0;
  public stepCount = 1;

  fileSize = 0;

  hasDropZoneOver = false;
  isAtrNotValid = false;
  isDisabled = true;
  hasFileSizeError = false;
  hasError = false;
  fileSizeString: string;
  isLoadingData = false;

  fileToUpload: File = null;
  transfertDetails: any = {};

  erroeMessage: string;
  fileName: string;

  subscriptions: Subscription;

  @ViewChild('confirmDeleteUpdateUnitsMetadataDialog', { static: true })
  confirmDeleteUpdateUnitsMetadataDialog: TemplateRef<UpdateUnitsaMetadataComponent>;

  @ViewChild('updateMetadataCSVFile', { static: false }) updateMetadataCSVFile: any;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      tenantIdentifier: string;
      selectedTransaction: Transaction;
    },
    private logger: Logger,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<UpdateUnitsaMetadataComponent>,
    private dialogRefToClose: MatDialogRef<UpdateUnitsaMetadataComponent>,
    private translateService: TranslateService,
    private archiveCollectService: ArchiveCollectService,
  ) {}

  ngOnDestroy(): void {
    this.subscriptions?.unsubscribe();
  }

  updateUAMetadatas() {
    this.stepIndex = this.stepIndex + 1;
  }

  addUpdateMetadataCSVFile() {
    this.updateMetadataCSVFile.nativeElement.click();
  }

  updateUnitsMetadata() {
    this.isLoadingData = true;
    this.snackBar.openFromComponent(VitamUISnackBarComponent, {
      panelClass: VITAMUI_SNACK_BAR,
      data: {
        type: 'waitMassUpdateUnitsMetadata',
      },
      duration: 100000,
    });

    this.subscriptions = this.archiveCollectService
      .updateUnitsAMetadata(this.data.tenantIdentifier, this.fileToUpload, this.fileName, this.data.selectedTransaction.id)
      .subscribe(
        (data) => {
          this.isLoadingData = false;
          if (data) {
            this.dialogRef.close(true);
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: VITAMUI_SNACK_BAR,
              data: {
                type: 'massUpdateUnitsMetadata',
              },
              duration: 100000,
            });
          }
        },
        (error: any) => {
          this.isLoadingData = false;
          this.dialogRef.close(true);
          this.logger.error('Error message :', error);
        },
      );
  }

  handleFileInput(files: FileList) {
    this.handleFileList(files);
  }

  onDragOver(inDropZone: boolean) {
    this.hasDropZoneOver = inDropZone;
  }

  onDragLeave(inDropZone: boolean) {
    this.hasDropZoneOver = inDropZone;
  }

  onDropped(files: File[]) {
    this.hasDropZoneOver = false;
    this.handleFile(files);
  }

  checkFileExtension(fileName: string): boolean {
    return fileName.endsWith(CSV_EXTENSION);
  }

  initializeFileToUpload(files: File[]) {
    if (files) {
      this.fileToUpload = files[0];

      this.fileName = this.fileToUpload.name;
      this.fileSize = this.fileToUpload.size;
    }
  }

  initializeFileListToUpload(files: FileList) {
    if (files) {
      this.fileToUpload = files.item(0);
      this.fileName = this.fileToUpload.name;
      this.fileSize = this.fileToUpload.size;
    }
  }

  onCancel() {
    const dialogToOpen = this.confirmDeleteUpdateUnitsMetadataDialog;
    this.dialogRefToClose = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
  }

  onCloseAction() {
    this.dialogRefToClose.close(true);
  }

  onConfirmAction() {
    this.dialogRefToClose.close(true);
    this.dialogRef.close(true);
  }

  initializeParameters() {
    this.isDisabled = false;
    this.hasError = false;
    this.hasFileSizeError = false;
    this.erroeMessage = null;
    this.isAtrNotValid = false;
  }

  handleFile(files: File[]) {
    this.initializeParameters();
    this.initializeFileToUpload(files);

    const transformer = new BytesPipe(this.logger);
    this.fileSizeString = transformer.transform(this.fileSize);

    if (!this.checkFileExtension(this.fileName)) {
      this.erroeMessage = this.translateService.instant('COLLECT.UPDATE_UNITS_METADATA.FILE_BAD_FORMAT');
      this.hasError = true;
      return;
    }
  }

  handleFileList(files: FileList) {
    this.initializeParameters();
    this.initializeFileListToUpload(files);

    const transformer = new BytesPipe(this.logger);
    this.fileSizeString = transformer.transform(this.fileSize);

    if (!this.checkFileExtension(this.fileName)) {
      this.erroeMessage = this.translateService.instant('COLLECT.UPDATE_UNITS_METADATA.FILE_BAD_FORMAT');
      this.hasError = true;
      return;
    }
  }
}
