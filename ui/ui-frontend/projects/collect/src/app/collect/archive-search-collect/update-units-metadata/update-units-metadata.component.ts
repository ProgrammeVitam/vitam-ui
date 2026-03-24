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
import { Component, Inject, OnDestroy, TemplateRef, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Subscription, throwError } from 'rxjs';
import { Logger, SnackBarService, Transaction, VitamErrorDetails } from 'vitamui-library';
import { ArchiveCollectService } from '../archive-collect.service';

@Component({
  selector: 'app-update-units-metadata',
  templateUrl: './update-units-metadata.component.html',
  styleUrls: ['./update-units-metadata.component.scss'],
  standalone: false,
})
export class UpdateUnitsMetadataComponent implements OnDestroy {
  isLoadingData = false;

  fileToUpload: File = undefined;
  errorsDetails: VitamErrorDetails[];
  errorMessage: string;

  subscriptions: Subscription;

  @ViewChild('confirmDeleteUpdateUnitsMetadataDialog', { static: true })
  confirmDeleteUpdateUnitsMetadataDialog: TemplateRef<UpdateUnitsMetadataComponent>;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      tenantIdentifier: string;
      selectedTransaction: Transaction;
    },
    private logger: Logger,
    private dialog: MatDialog,
    private dialogRef: MatDialogRef<UpdateUnitsMetadataComponent>,
    private dialogRefToClose: MatDialogRef<UpdateUnitsMetadataComponent>,
    private archiveCollectService: ArchiveCollectService,
    private snackBarService: SnackBarService,
  ) {}

  ngOnDestroy(): void {
    this.subscriptions?.unsubscribe();
  }

  updateUnitsMetadata() {
    this.isLoadingData = true;
    this.snackBarService.open({
      message: 'COLLECT.UPDATE_UNITS_METADATA.WAIT_MESSAGE',
      duration: 100_000,
    });

    this.subscriptions = this.archiveCollectService
      .updateUnitsMetadataByCsvFile(this.fileToUpload, this.fileToUpload.name, this.data.selectedTransaction.id)
      .subscribe({
        next: () => {
          this.isLoadingData = false;
          this.dialogRef.close(true);
          this.snackBarService.open({
            message: 'COLLECT.UPDATE_UNITS_METADATA.SUCCESS_MESSAGE',
            duration: 100_000,
          });
        },
        error: (error: any) => {
          this.isLoadingData = false;
          this.dialogRef.close(true);
          this.logger.error('Error message :', error);
          return throwError(error);
        },
      });
  }

  onCancel() {
    const dialogToOpen = this.confirmDeleteUpdateUnitsMetadataDialog;
    this.dialogRefToClose = this.dialog.open(dialogToOpen);
  }

  onCloseAction() {
    this.dialogRefToClose.close(true);
  }

  onConfirmAction() {
    this.dialogRefToClose.close(true);
    this.dialogRef.close(true);
  }

  handleFiles(files: File[]) {
    this.fileToUpload = files?.length ? files[0] : undefined;
  }
}
