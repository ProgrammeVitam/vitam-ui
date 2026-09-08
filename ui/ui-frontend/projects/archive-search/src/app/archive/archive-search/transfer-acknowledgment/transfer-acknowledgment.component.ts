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
import { Component, inject, OnDestroy, TemplateRef, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import {
  ApplicationId,
  DialogHeaderComponent,
  FileSelectorComponent,
  FileValidationErrors,
  Logger,
  PipesModule,
  readFileContent,
  SnackBarService,
  VitamUICommonModule,
  VitamUILibraryModule,
} from 'vitamui-library';
import { ArchiveService } from '../../archive.service';
import { XMLParser } from 'fast-xml-parser';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { CdkStep } from '@angular/cdk/stepper';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

const FILE_MAX_SIZE = 10737418240;
const ATR_EXTENSION = '.xml';

@Component({
  selector: 'app-transfer-acknowledgment',
  templateUrl: './transfer-acknowledgment.component.html',
  imports: [
    CdkStep,
    DialogHeaderComponent,
    FileSelectorComponent,
    MatDialogActions,
    MatDialogContent,
    MatProgressSpinner,
    PipesModule,
    ReactiveFormsModule,
    TranslatePipe,
    VitamUICommonModule,
    VitamUILibraryModule,
  ],
})
export class TransferAcknowledgmentComponent implements OnDestroy {
  dialog = inject(MatDialog);
  dialogRef = inject<MatDialogRef<TransferAcknowledgmentComponent>>(MatDialogRef);
  dialogRefToClose = inject<MatDialogRef<TransferAcknowledgmentComponent>>(MatDialogRef);

  archiveSearchService = inject(ArchiveService);
  snackBarService = inject(SnackBarService);

  data = inject(MAT_DIALOG_DATA);

  logger = inject(Logger);

  tenantIdentifier: string;

  isSubmitBtnDisabled = false;

  transfertDetails: any = {};

  transferAcknowledgementSubscription: Subscription;

  atrControl = new FormControl<File[]>(undefined, [Validators.required]);

  @ViewChild('confirmDeleteTransferAcknowledgmentDialog', { static: true })
  confirmDeleteTransferAcknowledgmentDialog: TemplateRef<TransferAcknowledgmentComponent>;

  constructor() {
    this.tenantIdentifier = this.data.tenantIdentifier;
  }

  atrContentValidator = async (file: File): Promise<FileValidationErrors> => {
    const xmlFileContent = await readFileContent(file);

    const parser = new XMLParser();
    try {
      const parsedXml: { ArchiveTransferReply: any } = parser.parse(xmlFileContent, true);
      if (parsedXml.ArchiveTransferReply === undefined || parsedXml.ArchiveTransferReply === null) {
        return {
          fileErrors: { atrNotValid: true },
          controlErrors: { invalidFiles: true },
        };
      } else {
        this.transfertDetails.messageRequestIdentifier = parsedXml.ArchiveTransferReply?.MessageRequestIdentifier;
        this.transfertDetails.date = parsedXml.ArchiveTransferReply?.Date;
        this.transfertDetails.archivalAgreement = parsedXml.ArchiveTransferReply?.ArchivalAgreement;
        this.transfertDetails.archivalAgency = parsedXml.ArchiveTransferReply.ArchivalAgency?.Identifier;
        this.transfertDetails.transferringAgency = parsedXml.ArchiveTransferReply.TransferringAgency?.Identifier;
        this.transfertDetails.archiveTransferReply = parsedXml.ArchiveTransferReply.ReplyCode?.replace(/(\r\n|\n|\r)/gm, '')?.trim();
      }
    } catch (error: any) {
      this.logger.error('Error with parsing the xml file :', error);
      return {
        fileErrors: { fileBadFormat: true },
        controlErrors: { invalidFiles: true },
      };
    }
    return null;
  };

  ngOnDestroy(): void {
    this.transferAcknowledgementSubscription?.unsubscribe();
  }

  onCancel() {
    const dialogToOpen = this.confirmDeleteTransferAcknowledgmentDialog;
    this.dialogRefToClose = this.dialog.open(dialogToOpen, { panelClass: 'small' });
  }

  onClose() {
    this.dialogRefToClose.close(true);
  }

  onConfirm() {
    this.dialogRefToClose.close(true);
    this.close();
  }

  close() {
    this.dialogRef.close(true);
  }

  // Step 3 :
  applyTransferAcknowledgment() {
    this.isSubmitBtnDisabled = true;
    const atrFile = this.atrControl.value[0];
    this.transferAcknowledgementSubscription = this.archiveSearchService
      .transferAcknowledgment(this.data.tenantIdentifier, atrFile)
      .subscribe(
        (operationId) => {
          this.dialogRef.close(true);
          this.isSubmitBtnDisabled = false;

          this.snackBarService.open({
            message: 'ARCHIVE_SEARCH.TRANSFER_ACKNOWLEDGMENT.REQUEST_MESSAGE',
            buttons: [
              {
                appId: ApplicationId.LOGBOOK_OPERATION_APP,
                path: `/tenant/${this.data.tenantIdentifier}?guid=${operationId}`,
                label: 'SNACK_BAR.TO_OPERATION_APP',
              },
            ],
            duration: 100_000,
          });
        },
        (error: any) => {
          this.isSubmitBtnDisabled = false;
          this.logger.error('Error message :', error);
        },
      );
  }

  protected readonly FILE_MAX_SIZE = FILE_MAX_SIZE;
  protected readonly ATR_EXTENSION = ATR_EXTENSION;
}
