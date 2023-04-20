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
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { StartupService } from 'ui-frontend-common';
import { ArchiveSearchComponent } from '../archive-search/archive-search.component';
import { ArchiveService } from '../archive.service';
import { SearchCriteriaEltDto } from '../models/search.criteria';
import { ArchiveSearchHelperService } from './archive-search-helper.service';

const DEFAULT_RESULT_THRESHOLD = 10000;
const PAGE_SIZE = 10;

@Injectable()
export class ArchiveUnitEliminationService {
  constructor(
    private archiveService: ArchiveService,
    private translateService: TranslateService,
    private startupService: StartupService,
    private archiveHelperService: ArchiveSearchHelperService,
    public snackBar: MatSnackBar,
    public dialog: MatDialog
  ) {
  }

  launchEliminationAnalysisModal(
    listOfUACriteriaSearch: SearchCriteriaEltDto[],    
    selectedItemCountKnown: boolean,
    itemSelected: number,
    tenantIdentifier: number,
    currentPage: number,
    confirmSecondActionBigNumberOfResultsActionDialog: TemplateRef<ArchiveSearchComponent>,
    showConfirmBigNumberOfResultsSuscription: Subscription
  ) {
    if (selectedItemCountKnown && itemSelected < DEFAULT_RESULT_THRESHOLD) {
      this.launchEliminationAnalysis(listOfUACriteriaSearch, tenantIdentifier, currentPage);
    } else {

      const dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpen = confirmSecondActionBigNumberOfResultsActionDialog;
      const showConfirmBigNumberOfResultsSuscription = this.dialog.open(
        dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpen,
        { panelClass: 'vitamui-dialog' }
      );

      showConfirmBigNumberOfResultsSuscription
        .afterClosed()
        .pipe(filter((result) => !!result))
        .subscribe(() => {
          this.launchEliminationAnalysis(
            listOfUACriteriaSearch,
            tenantIdentifier,
            currentPage
          );
        });
    }
    showConfirmBigNumberOfResultsSuscription?.unsubscribe();
  }

  private launchEliminationAnalysis(
    listOfUACriteriaSearch: SearchCriteriaEltDto[],    
    tenantIdentifier: number,
    currentPage: number
  ) {
    const exportDIPSearchCriteria = {
      criteriaList: listOfUACriteriaSearch,
      pageNumber: currentPage,
      size: PAGE_SIZE,
      language: this.translateService.currentLang,
    };

    this.archiveService.startEliminationAnalysis(exportDIPSearchCriteria).subscribe((data) => {
      const eliminationAnalysisResponse = data.$results;
      if (eliminationAnalysisResponse && eliminationAnalysisResponse[0].itemId) {
        const guid = eliminationAnalysisResponse[0].itemId;
        const message = this.translateService.instant('ARCHIVE_SEARCH.ELIMINATION.ELIMINATION_LAUNCHED');
        const serviceUrl = this.startupService.getReferentialUrl() + '/logbook-operation/tenant/' + tenantIdentifier + '?guid=' + guid;
        this.archiveHelperService.openSnackBarForWorkflow(this.snackBar, message, serviceUrl);
      }
    });
  }

  launchEliminationModal(
    listOfUACriteriaSearch: SearchCriteriaEltDto[],
    tenantIdentifier: number,
    currentPage: number,
    confirmSecondActionBigNumberOfResultsActionDialog: TemplateRef<ArchiveSearchComponent>
  ) {
    const dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpenRef = this.dialog.open(
      confirmSecondActionBigNumberOfResultsActionDialog,
      { panelClass: 'vitamui-dialog' }
    );
    dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpenRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.launchEliminationAction(listOfUACriteriaSearch, tenantIdentifier, currentPage);
      });
  }

  private launchEliminationAction(
    listOfUACriteriaSearch: SearchCriteriaEltDto[],
    tenantIdentifier: number,
    currentPage: number
  ) {
    const exportDIPSearchCriteria = {
      criteriaList: listOfUACriteriaSearch,
      pageNumber: currentPage,
      size: PAGE_SIZE,
      language: this.translateService.currentLang,
    };

    this.archiveService.launchEliminationAction(exportDIPSearchCriteria).subscribe((response) => {
      const eliminationActionResponse = response.$results;

      if (eliminationActionResponse && eliminationActionResponse[0].itemId) {
        const guid = eliminationActionResponse[0].itemId;
        const message = this.translateService.instant('ARCHIVE_SEARCH.ELIMINATION.ELIMINATION_LAUNCHED');
        const serviceUrl = this.startupService.getReferentialUrl() + '/logbook-operation/tenant/' + tenantIdentifier + '?guid=' + guid;

        this.archiveHelperService.openSnackBarForWorkflow(this.snackBar, message, serviceUrl);
      }
    });
  }
}
