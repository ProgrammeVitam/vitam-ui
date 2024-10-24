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

import { Component, Inject, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import {
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
} from '@angular/material/legacy-dialog';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { BytesPipe, Logger, StartupService } from 'vitamui-library';
import { ArchiveService } from '../../archive.service';
import { XMLParser } from 'fast-xml-parser';

const FILE_MAX_SIZE = 10737418240;
const ATR_EXTENSION = '.xml';

@Component({
  selector: 'app-transfer-acknowledgment',
  templateUrl: './transfer-acknowledgment.component.html',
  styleUrls: ['./transfer-acknowledgment.component.scss'],
})
export class TransferAcknowledgmentComponent implements OnInit, OnDestroy {
  public stepIndex = 0;
  public stepCount = 3;
  fileSize = 0;

  isAtrNotValid = false;
  isDisabled = true;
  hasFileSizeError = false;
  hasError = false;
  isLoadingData = false;
  isSubmitBtnDisabled = false;

  fileToUpload: File = null;
  transfertDetails: any = {};

  message: string;
  fileName: string;
  fileSizeString: string;
  accessContract: string;
  tenantIdentifier: string;
  transfertDetailsCode: string;

  transferAcknowledgementSubscription: Subscription;

  @ViewChild('confirmDeleteTransferAcknowledgmentDialog', { static: true })
  confirmDeleteTransferAcknowledgmentDialog: TemplateRef<TransferAcknowledgmentComponent>;

  @ViewChild('atrXmlFile', { static: false }) atrXmlFile: any;

  constructor(
    public dialog: MatDialog,
    private dialogRef: MatDialogRef<TransferAcknowledgmentComponent>,
    private dialogRefToClose: MatDialogRef<TransferAcknowledgmentComponent>,
    public logger: Logger,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      accessContract: string;
      tenantIdentifier: string;
    },
    private archiveSearchService: ArchiveService,
    private startupService: StartupService,
    private translateService: TranslateService,
    private bytesPipe: BytesPipe,
  ) {}

  async parseXmlToTransferDetails(xmlFileContent: string) {
    this.isLoadingData = true;

    const parser = new XMLParser();
    try {
      const parsedXml: { ArchiveTransferReply: any } = parser.parse(xmlFileContent, true);
      if (parsedXml.ArchiveTransferReply === undefined || parsedXml.ArchiveTransferReply === null) {
        this.isAtrNotValid = true;
        this.isLoadingData = false;
      } else {
        this.transfertDetails.messageRequestIdentifier = parsedXml.ArchiveTransferReply?.MessageRequestIdentifier;
        this.transfertDetails.date = parsedXml.ArchiveTransferReply?.Date;
        this.transfertDetails.archivalAgreement = parsedXml.ArchiveTransferReply?.ArchivalAgreement;
        this.transfertDetails.archivalAgency = parsedXml.ArchiveTransferReply.ArchivalAgency?.Identifier;
        this.transfertDetails.transferringAgency = parsedXml.ArchiveTransferReply.TransferringAgency?.Identifier;
        this.transfertDetails.archiveTransferReply = parsedXml.ArchiveTransferReply.ReplyCode;
        this.isAtrNotValid = false;
        this.stepIndex = this.stepIndex + 1;
        this.isLoadingData = false;
      }
    } catch (error: any) {
      this.message = this.translateService.instant('ARCHIVE_SEARCH.TRANSFER_ACKNOWLEDGMENT.FILE_BAD_FORMAT');
      this.hasError = true;
      this.isLoadingData = false;
      this.logger.error('Error with parsing the xml file :', error);
    }
  }

  ngOnInit(): void {
    this.accessContract = this.data.accessContract;
    this.tenantIdentifier = this.data.tenantIdentifier;
  }

  ngOnDestroy(): void {
    this.transferAcknowledgementSubscription?.unsubscribe();
  }

  backToPreviousStep() {
    this.stepIndex = this.stepIndex - 1;
  }

  onCancel() {
    const dialogToOpen = this.confirmDeleteTransferAcknowledgmentDialog;
    this.dialogRefToClose = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
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

  initializeParameters() {
    this.isDisabled = false;
    this.hasError = false;
    this.hasFileSizeError = false;
    this.message = null;
    this.isAtrNotValid = false;
    this.transfertDetails = {};
    this.transfertDetailsCode = null;
  }

  private isFileList(files: FileList | File[]): files is FileList {
    return files instanceof FileList;
  }

  private initializeFileToUpload(files: FileList | File[]) {
    if (files) {
      this.fileToUpload = this.isFileList(files) ? files.item(0) : files[0];
      this.fileName = this.fileToUpload.name;
      this.fileSize = this.fileToUpload.size;
    }
  }

  handleFile(files: FileList | File[]) {
    this.initializeParameters();
    this.initializeFileToUpload(files);

    this.fileSizeString = this.bytesPipe.transform(this.fileSize);

    if (!this.checkFileExtension(this.fileName)) {
      this.message = this.translateService.instant('ARCHIVE_SEARCH.TRANSFER_ACKNOWLEDGMENT.FILE_BAD_FORMAT');
      this.hasError = true;
    } else {
      if (this.fileSize > FILE_MAX_SIZE) {
        this.logger.error(this.translateService.instant('ARCHIVE_SEARCH.TRANSFER_ACKNOWLEDGMENT.AUTHORIZED_SIZE'));
        this.hasFileSizeError = true;
      }
    }
  }

  addTransferAtrFile() {
    this.atrXmlFile.nativeElement.click();
  }

  checkFileExtension(fileName: string): boolean {
    return fileName.endsWith(ATR_EXTENSION);
  }

  goToNextStep() {
    this.stepIndex = this.stepIndex + 1;
    this.transfertDetailsCode = this.transfertDetails.archiveTransferReply.replace(/(\r\n|\n|\r)/gm, '').trim();
  }

  // Step 1 :
  validateAndParseXmlFile() {
    if (this.fileToUpload && !this.hasError && !this.hasFileSizeError) {
      this.isLoadingData = true;
      this.fileToUpload.text().then((xmlFileContent) => this.parseXmlToTransferDetails(xmlFileContent));
    }
  }

  // Step 3 :
  applyTransferAcknowledgment() {
    this.isSubmitBtnDisabled = true;
    this.transferAcknowledgementSubscription = this.archiveSearchService
      .transferAcknowledgment(this.tenantIdentifier, this.fileToUpload, this.fileName)
      .subscribe(
        (operationId) => {
          this.dialogRef.close(true);
          this.isSubmitBtnDisabled = false;
          const serviceUrl =
            this.startupService.getReferentialUrl() + '/logbook-operation/tenant/' + this.data.tenantIdentifier + '?guid=' + operationId;

          this.archiveSearchService.openSnackBarForWorkflow(
            this.translateService.instant('ARCHIVE_SEARCH.TRANSFER_ACKNOWLEDGMENT.REQUEST_MESSAGE'),
            serviceUrl,
          );
        },
        (error: any) => {
          this.isSubmitBtnDisabled = false;
          this.logger.error('Error message :', error);
        },
      );
  }
}
