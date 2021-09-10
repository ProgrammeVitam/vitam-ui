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

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { ConfirmDialogService, Logger, StartupService } from 'ui-frontend-common';
import * as uuid from 'uuid';
import { ArchiveService } from '../../archive.service';
import { ExportDIPCriteriaList, ExportDIPRequestDetail } from '../../models/dip-request-detail.interface';
import { SearchCriteriaEltDto } from '../../models/search.criteria';

@Component({
  selector: 'app-dip-request-create',
  templateUrl: './dip-request-create.component.html',
  styleUrls: ['./dip-request-create.component.css'],
})
export class DipRequestCreateComponent implements OnInit, OnDestroy {
  constructor(
    private translate: TranslateService,
    public dialogRef: MatDialogRef<DipRequestCreateComponent>,
    private formBuilder: FormBuilder,
    private archiveService: ArchiveService,
    private startupService: StartupService,
    private confirmDialogService: ConfirmDialogService,
    private logger: Logger,
    @Inject(MAT_DIALOG_DATA)
    public data: { itemSelected: number; exportDIPSearchCriteria: SearchCriteriaEltDto[]; accessContract: string; tenantIdentifier: string }
  ) {}
  exportDIPform: FormGroup;
  exportDIPIncludeform: FormGroup;
  itemSelected: number;
  keyPressSubscription: Subscription;
  dataObjectVersions = ['BinaryMaster', 'Dissemination', 'Thumbnail', 'TextContent', 'PhysicalMaster'];

  ngOnInit(): void {
    this.itemSelected = this.data.itemSelected;
    this.initExportForm();
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.exportDIPform.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  private initExportForm() {
    const messageRequestIdentifier = uuid.v4();
    this.exportDIPform = this.formBuilder.group({
      lifeCycleLogs: [this.translate.instant('ARCHIVE_SEARCH.DIP.INCLUDE')],
      messageRequestIdentifier: [{ value: messageRequestIdentifier, disabled: true }, Validators.required],
      requesterIdentifier: [null, Validators.required],
      archivalAgencyIdentifier: [null, Validators.required],
      authorizationRequestReplyIdentifier: [null],
      submissionAgencyIdentifier: [null],
      comment: [null],
      archivalAgreement: [this.data.accessContract],
    });
  }

  onSubmit() {
    if (this.exportDIPform.invalid) {
      return;
    }

    const exportDIPformDetail: ExportDIPRequestDetail = this.exportDIPform.getRawValue();
    const exportDIPCriteriaList: ExportDIPCriteriaList = {
      dipRequestParameters: exportDIPformDetail,
      exportDIPSearchCriteria: this.data.exportDIPSearchCriteria,
      dataObjectVersions: this.dataObjectVersions,
      lifeCycleLogs: this.exportDIPform.get('lifeCycleLogs').value === this.translate.instant('ARCHIVE_SEARCH.DIP.INCLUDE'),
    };

    this.archiveService.exportDIPService(exportDIPCriteriaList, this.data.accessContract).subscribe(
      (response) => {
        this.dialogRef.close(true);
        const serviceUrl =
          this.startupService.getReferentialUrl() + '/logbook-operation/tenant/' + this.data.tenantIdentifier + '?guid=' + response;

        this.archiveService.openSnackBarForWorkflow(this.translate.instant('ARCHIVE_SEARCH.DIP.REQUEST_MESSAGE'), serviceUrl);
      },
      (error: any) => {
        this.logger.error('Error message :', error);
      }
    );
  }
}
