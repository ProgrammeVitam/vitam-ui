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
import { filter } from 'rxjs/operators';
import { CriteriaDataType, CriteriaOperator, StartupService } from 'ui-frontend-common';
import { ArchiveSearchComponent } from '../archive-search/archive-search.component';
import { ArchiveService } from '../archive.service';
import { SearchCriteriaEltDto, SearchCriteriaTypeEnum } from '../models/search.criteria';
import { ArchiveSearchHelperService } from './archive-search-helper.service';

const ARCHIVE_UNIT_HOLDING_UNIT = 'ARCHIVE_UNIT_HOLDING_UNIT';
const PAGE_SIZE = 10;

@Injectable()
export class ComputeInheritedRulesService {
  constructor(
    private archiveSearchHelperService: ArchiveSearchHelperService,
    private startupService: StartupService,
    private archiveService: ArchiveService,
    private translateService: TranslateService,
    public snackBar: MatSnackBar,
    public dialog: MatDialog
  ) {}

  launchComputedInheritedRulesModal(
    listOfUACriteriaSearch: SearchCriteriaEltDto[],    
    numberOfHoldingUnitTypeOnComputedRules: number,
    tenantIdentifier: number,
    currentPage: number,
    launchComputeInheritedRuleAlerteMessageDialog: TemplateRef<ArchiveSearchComponent>,
    confirmSecondActionBigNumberOfResultsActionDialog: TemplateRef<ArchiveSearchComponent>
  ) {
    listOfUACriteriaSearch.push({
      criteria: ARCHIVE_UNIT_HOLDING_UNIT,
      values: [{ value: 'HOLDING_UNIT', id: 'HOLDING_UNIT' }],
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      dataType: CriteriaDataType.STRING,
    });

    const computedInheritedRulesSearchCriteria = {
      criteriaList: listOfUACriteriaSearch,
      pageNumber: currentPage,
      size: PAGE_SIZE,
      language: this.translateService.currentLang,
    };

    this.archiveService.searchArchiveUnitsByCriteria(computedInheritedRulesSearchCriteria).subscribe((response) => {
      numberOfHoldingUnitTypeOnComputedRules = response.totalResults;
      if (numberOfHoldingUnitTypeOnComputedRules > 0) {
        const dialogToOpen = launchComputeInheritedRuleAlerteMessageDialog;
        const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
        dialogRef
          .afterClosed()
          .pipe(filter((result) => !!result))
          .subscribe(() => {});
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
            const computedInheritedRulesDSLQuery = {
              criteriaList: computedInheritedRulesSearchCriteria.criteriaList.filter(
                (criteriaSearch) => criteriaSearch.criteria !== ARCHIVE_UNIT_HOLDING_UNIT
              ),
              pageNumber: currentPage,
              size: PAGE_SIZE,
              language: this.translateService.currentLang,
            };
            this.archiveService.launchComputedInheritedRules(computedInheritedRulesDSLQuery).subscribe((operationId) => {
              const guid = operationId;
              const message = this.translateService.instant('ARCHIVE_SEARCH.COMPUTED_INHERITED_RULES.OPERATION_MESSAGE');
              const serviceUrl =
                this.startupService.getReferentialUrl() + '/logbook-operation/tenant/' + tenantIdentifier + '?guid=' + guid;

              this.archiveSearchHelperService.openSnackBarForWorkflow(this.snackBar, message, serviceUrl);
            });
          });
      }
    });
  }
}
