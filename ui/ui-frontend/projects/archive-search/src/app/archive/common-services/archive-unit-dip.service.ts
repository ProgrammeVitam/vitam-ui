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
import { Injectable, TemplateRef } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { filter } from 'rxjs/operators';
import { SearchCriteriaEltDto } from 'ui-frontend-common';
import { DipRequestCreateComponent } from '../archive-search/additional-actions-search/dip-request-create/dip-request-create.component';
import {
  TransferRequestModalComponent
} from '../archive-search/additional-actions-search/transfer-request-modal/transfer-request-modal.component';
import { ArchiveSearchComponent } from '../archive-search/archive-search.component';

const DEFAULT_RESULT_THRESHOLD = 10000;
const PAGE_SIZE = 10;

@Injectable()
export class ArchiveUnitDipService {
  constructor(private translateService: TranslateService, public dialog: MatDialog) {}

  launchExportDipModal(
    listOfUACriteriaSearch: SearchCriteriaEltDto[],
    selectedItemCountKnown: boolean,
    accessContract: string,
    tenantIdentifier: number,
    itemSelected: number,
    currentPage: number,
    isAllchecked: boolean,
    confirmSecondActionBigNumberOfResultsActionDialog: TemplateRef<ArchiveSearchComponent>
  ) {
    if (!isAllchecked && itemSelected < DEFAULT_RESULT_THRESHOLD) {
      this.launchExportDIP(listOfUACriteriaSearch, selectedItemCountKnown, accessContract, tenantIdentifier, itemSelected, currentPage);
    } else {
      const dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpen = confirmSecondActionBigNumberOfResultsActionDialog;
      const dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpenRef = this.dialog.open(
        dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpen,
        { panelClass: 'vitamui-dialog' }
      );

      dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpenRef
        .afterClosed()
        .pipe(filter((result) => !!result))
        .subscribe(() => {
          this.launchExportDIP(listOfUACriteriaSearch, selectedItemCountKnown, accessContract, tenantIdentifier, itemSelected, currentPage);
        });
    }
  }

  private launchExportDIP(
    listOfUACriteriaSearch: SearchCriteriaEltDto[],
    selectedItemCountKnown: boolean,
    accessContract: string,
    tenantIdentifier: number,
    itemSelected: number,
    currentPage: number
  ) {
    const exportDIPSearchCriteria = {
      criteriaList: listOfUACriteriaSearch,
      pageNumber: currentPage,
      size: PAGE_SIZE,
      language: this.translateService.currentLang,
    };

    const dialogRef = this.dialog.open(DipRequestCreateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: false,
      data: {
        itemSelected,
        exportDIPSearchCriteria,
        accessContract,
        tenantIdentifier,
        selectedItemCountKnown,
      },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        return;
      }
    });
  }

  launchTransferRequestModal(
    listOfUACriteriaSearch: SearchCriteriaEltDto[],
    selectedItemCountKnown: boolean,
    accessContract: string,
    tenantIdentifier: number,
    itemSelected: number,
    currentPage: number,
    isAllchecked: boolean,
    confirmSecondActionBigNumberOfResultsActionDialog: TemplateRef<ArchiveSearchComponent>
  ) {
    if (!isAllchecked && itemSelected < DEFAULT_RESULT_THRESHOLD) {
      this.launchTransferRequest(
        listOfUACriteriaSearch,
        selectedItemCountKnown,
        accessContract,
        tenantIdentifier,
        itemSelected,
        currentPage
      );
    } else {
      const dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpenRef = this.dialog.open(
        confirmSecondActionBigNumberOfResultsActionDialog,
        { panelClass: 'vitamui-dialog' }
      );
      dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpenRef
        .afterClosed()
        .pipe(filter((result) => !!result))
        .subscribe(() => {
          this.launchTransferRequest(
            listOfUACriteriaSearch,
            selectedItemCountKnown,
            accessContract,
            tenantIdentifier,
            itemSelected,
            currentPage
          );
        });
    }
  }

  private launchTransferRequest(
    listOfUACriteriaSearch: SearchCriteriaEltDto[],
    selectedItemCountKnown: boolean,
    accessContract: string,
    tenantIdentifier: number,
    itemSelected: number,
    currentPage: number
  ) {
    const searchCriteria = {
      criteriaList: listOfUACriteriaSearch,
      pageNumber: currentPage,
      size: PAGE_SIZE,
      language: this.translateService.currentLang,
    };

    const dialogRef = this.dialog.open(TransferRequestModalComponent, {
      panelClass: 'vitamui-modal',
      disableClose: false,
      data: {
        itemSelected,
        searchCriteria,
        accessContract,
        tenantIdentifier,
        selectedItemCountKnown,
      },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        return;
      }
    });
  }
}
