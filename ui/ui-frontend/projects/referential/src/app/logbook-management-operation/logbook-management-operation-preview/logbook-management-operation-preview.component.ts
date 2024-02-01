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

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { OperationDetails } from '../../models/operation-response.interface';
import { LogbookManagementOperationService } from '../logbook-management-operation.service';

@Component({
  selector: 'app-logbook-management-operation-preview',
  templateUrl: './logbook-management-operation-preview.component.html',
  styleUrls: ['./logbook-management-operation-preview.component.scss'],
})
export class LogbookManagementOperationPreviewComponent implements OnInit, OnDestroy {
  @Input() operation: OperationDetails;
  @Input() tenantIdentifier: number;
  @Input() tenant: any;
  @Output() previewClose = new EventEmitter();
  @ViewChild('confirmUpdateOperationDialog', { static: true })
  confirmUpdateOperationDialog: TemplateRef<LogbookManagementOperationPreviewComponent>;

  actionId: string;
  operationUpdatedSub: Subscription;

  constructor(
    private matDialog: MatDialog,
    public logbookManagementOperationService: LogbookManagementOperationService,
  ) {}
  ngOnDestroy(): void {
    this.operationUpdatedSub.unsubscribe();
  }

  ngOnInit(): void {
    if (this.logbookManagementOperationService.operationUpdated) {
      this.operationUpdatedSub = this.logbookManagementOperationService.operationUpdated.subscribe((updatedOperation: OperationDetails) => {
        this.operation = updatedOperation;
      });
    }
  }

  emitClose() {
    this.previewClose.emit();
  }

  updateOperationStatus(operation: OperationDetails, actionId: string) {
    let dialogToOpen;
    this.actionId = actionId;
    dialogToOpen = this.confirmUpdateOperationDialog;
    const dialogRef = this.matDialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
    dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.logbookManagementOperationService.updateOperationProcessExecution(operation.operationId, actionId).subscribe((operations) => {
          if (operations) {
            this.operation = operations.results[operations.hits.total - 1];
          }
        });
      });
  }

  cancelOperation(operation: OperationDetails) {
    let dialogToOpen;
    this.actionId = 'CANCEL';
    dialogToOpen = this.confirmUpdateOperationDialog;

    const dialogRef = this.matDialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
    dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.logbookManagementOperationService.cancelOperationProcessExecution(operation.operationId).subscribe((operations) => {
          if (operations.results) {
            this.operation = operations.results[operations.hits.total - 1];
          }
        });
      });
  }

  operationStatus(operation: OperationDetails): string {
    return operation.stepStatus;
  }
}
