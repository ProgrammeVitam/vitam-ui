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
import { Injectable, TemplateRef, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { filter } from 'rxjs/operators';
import {
  ApplicationId,
  CriteriaDataType,
  CriteriaOperator,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
  SnackBarService,
  UnitType,
} from 'vitamui-library';
import { ArchiveSearchComponent } from '../archive-search/archive-search.component';
import { ArchiveService } from '../archive.service';

const ARCHIVE_UNIT_HOLDING_UNIT = 'ARCHIVE_UNIT_HOLDING_UNIT';
const PAGE_SIZE = 10;

@Injectable({
  providedIn: 'root',
})
export class ComputeInheritedRulesService {
  private archiveService = inject(ArchiveService);
  private translateService = inject(TranslateService);
  dialog = inject(MatDialog);
  private snackBarService = inject(SnackBarService);

  launchComputedInheritedRulesModal(
    listOfUACriteriaSearch: SearchCriteriaEltDto[],
    numberOfHoldingUnitTypeOnComputedRules: number,
    tenantIdentifier: number,
    currentPage: number,
    launchComputeInheritedRuleAlerteMessageDialog: TemplateRef<ArchiveSearchComponent>,
    confirmSecondActionBigNumberOfResultsActionDialog: TemplateRef<ArchiveSearchComponent>,
  ) {
    listOfUACriteriaSearch.push({
      criteria: ARCHIVE_UNIT_HOLDING_UNIT,
      values: [{ value: UnitType.HOLDING_UNIT, id: UnitType.HOLDING_UNIT }],
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      dataType: CriteriaDataType.STRING,
    });

    const computedInheritedRulesSearchCriteria = {
      criteriaList: listOfUACriteriaSearch,
      pageNumber: currentPage,
      size: PAGE_SIZE,
      language: this.translateService.getCurrentLang(),
    };

    this.archiveService.searchArchiveUnitsByCriteria(computedInheritedRulesSearchCriteria).subscribe((response) => {
      numberOfHoldingUnitTypeOnComputedRules = response.totalResults;
      if (numberOfHoldingUnitTypeOnComputedRules > 0) {
        const dialogToOpen = launchComputeInheritedRuleAlerteMessageDialog;
        const dialogRef = this.dialog.open(dialogToOpen);
        dialogRef
          .afterClosed()
          .pipe(filter((result) => !!result))
          .subscribe(() => {});
      } else {
        const dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpen = confirmSecondActionBigNumberOfResultsActionDialog;
        const dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpenRef = this.dialog.open(
          dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpen,
        );

        dialogConfirmSecondActionBigNumberOfResultsActionDialogToOpenRef
          .afterClosed()
          .pipe(filter((result) => !!result))
          .subscribe(() => {
            const computedInheritedRulesDSLQuery = {
              criteriaList: computedInheritedRulesSearchCriteria.criteriaList.filter(
                (criteriaSearch) => criteriaSearch.criteria !== ARCHIVE_UNIT_HOLDING_UNIT,
              ),
              pageNumber: currentPage,
              size: PAGE_SIZE,
              language: this.translateService.getCurrentLang(),
            };
            this.archiveService.launchComputedInheritedRules(computedInheritedRulesDSLQuery).subscribe((operationId) => {
              this.snackBarService.open({
                message: 'ARCHIVE_SEARCH.COMPUTED_INHERITED_RULES.OPERATION_MESSAGE',
                buttons: [
                  {
                    appId: ApplicationId.LOGBOOK_OPERATION_APP,
                    path: `/tenant/${tenantIdentifier}?guid=${operationId}`,
                    label: 'SNACK_BAR.TO_OPERATION_APP',
                  },
                ],
                duration: 100_000,
              });
            });
          });
      }
    });
  }
}
