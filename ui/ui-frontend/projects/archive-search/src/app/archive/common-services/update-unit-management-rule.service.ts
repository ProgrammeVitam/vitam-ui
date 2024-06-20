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
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { CriteriaDataType, CriteriaOperator, SearchCriteriaEltDto, SearchCriteriaTypeEnum, UnitType } from 'vitamui-library';
import { ManagementRulesSharedDataService } from '../../core/management-rules-shared-data.service';
import { ArchiveSearchComponent } from '../archive-search/archive-search.component';
import { ArchiveService } from '../archive.service';

const ARCHIVE_UNIT_HOLDING_UNIT = 'ARCHIVE_UNIT_HOLDING_UNIT';
const PAGE_SIZE = 10;

@Injectable({
  providedIn: 'root',
})
export class UpdateUnitManagementRuleService {
  constructor(
    private archiveService: ArchiveService,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private translateService: TranslateService,
    public dialog: MatDialog,
  ) {}

  DEFAULT_ELIMINATION_ANALYSIS_THRESHOLD = 100000;
  DEFAULT_DIP_EXPORT_THRESHOLD = 100000;
  DEFAULT_ELIMINATION_THRESHOLD = 10000;
  DEFAULT_TRANSFER_THRESHOLD = 100000;
  DEFAULT_UPDATE_MGT_RULES_THRESHOLD = 100000;

  goToUpdateManagementRule(
    listOfUACriteriaSearch: SearchCriteriaEltDto[],
    criteriaSearchList: SearchCriteriaEltDto[],
    currentPage: number,
    tenantIdentifier: number,
    numberOfHoldingUnitType: number,
    router: Router,
    itemSelected: number,
    updateArchiveUnitAlerteMessageDialogSubscription: Subscription,
    updateArchiveUnitAlerteMessageDialog: TemplateRef<ArchiveSearchComponent>,
    confirmSecondActionBigNumberOfResultsActionDialog: TemplateRef<ArchiveSearchComponent>,
  ) {
    const dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpen = confirmSecondActionBigNumberOfResultsActionDialog;
    const dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpenRef = this.dialog.open(
      dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpen,
      { panelClass: 'vitamui-dialog' },
    );
    dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpenRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.updateManagementRule(
          listOfUACriteriaSearch,
          criteriaSearchList,
          currentPage,
          tenantIdentifier,
          numberOfHoldingUnitType,
          router,
          itemSelected,
          updateArchiveUnitAlerteMessageDialogSubscription,
          updateArchiveUnitAlerteMessageDialog,
        );
      });
  }

  getRuleManagementCategory(categoryName: string): string {
    switch (categoryName) {
      case 'AppraisalRule':
        return SearchCriteriaTypeEnum.APPRAISAL_RULE;
      case 'AccessRule':
        return SearchCriteriaTypeEnum.ACCESS_RULE;
      case 'StorageRule':
        return SearchCriteriaTypeEnum.STORAGE_RULE;
      case 'HoldRule':
        return SearchCriteriaTypeEnum.HOLD_RULE;
      case 'DisseminationRule':
        return SearchCriteriaTypeEnum.DISSEMINATION_RULE;
      case 'ReuseRule':
        return SearchCriteriaTypeEnum.REUSE_RULE;
      case 'ClassificationRule':
        return SearchCriteriaTypeEnum.CLASSIFICATION_RULE;
      default:
    }
  }

  private updateManagementRule(
    listOfUACriteriaSearch: SearchCriteriaEltDto[],
    criteriaSearchList: SearchCriteriaEltDto[],
    currentPage: number,
    tenantIdentifier: number,
    numberOfHoldingUnitType: number,
    router: Router,
    itemSelected: number,
    updateArchiveUnitAlerteMessageDialogSubscription: Subscription,
    updateArchiveUnitAlerteMessageDialog: TemplateRef<ArchiveSearchComponent>,
  ) {
    listOfUACriteriaSearch.push({
      criteria: ARCHIVE_UNIT_HOLDING_UNIT,
      values: [{ value: UnitType.HOLDING_UNIT, id: UnitType.HOLDING_UNIT }],
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      dataType: CriteriaDataType.STRING,
    });

    const criteriaSearchDSLQuery = {
      criteriaList: listOfUACriteriaSearch,
      pageNumber: currentPage,
      size: PAGE_SIZE,
      language: this.translateService.currentLang,
    };
    this.archiveService.searchArchiveUnitsByCriteria(criteriaSearchDSLQuery).subscribe((data) => {
      numberOfHoldingUnitType = data.totalResults;
      if (numberOfHoldingUnitType > 0) {
        const dialogToOpen = updateArchiveUnitAlerteMessageDialog;
        const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
        updateArchiveUnitAlerteMessageDialogSubscription = dialogRef
          .afterClosed()
          .pipe(filter((result) => !!result))
          .subscribe(() => {});
        updateArchiveUnitAlerteMessageDialogSubscription?.unsubscribe();
      } else {
        const criteriaSearchDSLQueryToSend = {
          criteriaList: listOfUACriteriaSearch.filter((criteria) => criteria.criteria !== ARCHIVE_UNIT_HOLDING_UNIT),
          pageNumber: currentPage,
          size: PAGE_SIZE,
          language: this.translateService.currentLang,
        };
        this.managementRulesSharedDataService.emitselectedItems(itemSelected);
        this.managementRulesSharedDataService.emitCriteriaSearchListToSave(criteriaSearchList);
        this.managementRulesSharedDataService.emitCriteriaSearchDSLQuery(criteriaSearchDSLQueryToSend);

        router.navigate(['/archive-search/update-rules/tenant/', tenantIdentifier]);
      }
    });
  }
}
