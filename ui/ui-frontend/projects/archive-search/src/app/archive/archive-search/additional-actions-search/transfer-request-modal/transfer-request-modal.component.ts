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
 */
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { ConfirmDialogService, Logger, StartupService } from 'ui-frontend-common';
import { ArchiveService } from '../../../archive.service';
import { SearchCriteriaEltDto } from '../../../models/search.criteria';
import { TransferRequestDto, TransferRequestParameters } from '../../../models/transfer-request-detail.interface';

@Component({
  selector: 'app-transfer-request-modal',
  templateUrl: './transfer-request-modal.component.html',
  styleUrls: ['./transfer-request-modal.component.scss'],
})
export class TransferRequestModalComponent implements OnInit, OnDestroy {
  transferRequestFormGroup: FormGroup;
  itemSelected: number;
  selectedItemCountKnown: boolean;
  keyPressSubscription: Subscription;
  dataObjectVersions = ['BinaryMaster', 'Dissemination', 'Thumbnail', 'TextContent', 'PhysicalMaster'];

  constructor(
    private translate: TranslateService,
    public dialogRef: MatDialogRef<TransferRequestModalComponent>,
    private formBuilder: FormBuilder,
    private archiveService: ArchiveService,
    private startupService: StartupService,
    private confirmDialogService: ConfirmDialogService,
    private logger: Logger,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      itemSelected: number;
      searchCriteria: SearchCriteriaEltDto[];
      accessContract: string;
      tenantIdentifier: string;
      selectedItemCountKnown?: boolean;
    }
  ) {}

  ngOnInit(): void {
    this.itemSelected = this.data.itemSelected;
    this.selectedItemCountKnown = this.data.selectedItemCountKnown;
    this.initTransferForm();
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.transferRequestFormGroup.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.transferRequestFormGroup.invalid) {
      return;
    }

    const transferRequestParameters: TransferRequestParameters = this.transferRequestFormGroup.getRawValue();
    transferRequestParameters.relatedTransferReference = [this.transferRequestFormGroup.get('relatedTransferReference').value];
    const transferRequestDto: TransferRequestDto = {
      transferRequestParameters,
      searchCriteria: this.data.searchCriteria,
      dataObjectVersions: this.dataObjectVersions,
      lifeCycleLogs: this.transferRequestFormGroup.get('lifeCycleLogs').value === this.translate.instant('ARCHIVE_SEARCH.DIP.INCLUDE'),
    };

    this.archiveService.transferRequestService(transferRequestDto).subscribe(
      (response) => {
        this.dialogRef.close(true);
        const serviceUrl =
          this.startupService.getReferentialUrl() + '/logbook-operation/tenant/' + this.data.tenantIdentifier + '?guid=' + response;
        this.archiveService.openSnackBarForWorkflow(this.translate.instant('ARCHIVE_SEARCH.DIP.REQUEST_MESSAGE'), serviceUrl);
        this.transferRequestFormGroup.reset();
      },
      (error: any) => {
        this.logger.error('Error message :', error);
      }
    );
  }

  private initTransferForm() {
    this.transferRequestFormGroup = this.formBuilder.group({
      lifeCycleLogs: [this.translate.instant('ARCHIVE_SEARCH.DIP.INCLUDE')],
      archivalAgreement: [null, Validators.required],
      originatingAgencyIdentifier: [null, Validators.required],
      comment: [null],
      submissionAgencyIdentifier: [null],
      relatedTransferReference: [null],
      transferRequestReplyIdentifier: [null],
      archivalAgencyIdentifier: [null, Validators.required],
      transferringAgency: [null],
    });
  }
}
