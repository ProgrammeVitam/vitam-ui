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
import { Injectable, inject } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { HttpErrorResponse } from '@angular/common/http';
import { OriginatingAgencyReassignmentDialogComponent } from './originating-agency-reassignment-dialog.component';
import {
  ApplicationId,
  SearchCriteriaDto,
  SearchCriteriaEltDto,
  SnackBarService,
  CriteriaOperator,
  SearchCriteriaTypeEnum,
  CriteriaDataType,
  AlertDialogComponent,
} from 'vitamui-library';
import { ArchiveService } from '../../../archive.service';
import { ReassignmentMode, ReassignRequestDto } from '../../../models/reassign-request.interface';
import { filter, switchMap, catchError } from 'rxjs/operators';
import { EMPTY } from 'rxjs';

interface ReassignmentDialogData {
  itemSelected?: number;
  reassignmentMode: ReassignmentMode;
  tenantIdentifier?: number;
  accessContract?: string;
}

interface ReassignmentDialogResult {
  sourceOriginatingAgency: string;
  targetOriginatingAgency: string;
  propagateToObjectGroups: boolean;
  entryOperationIds?: string;
}

@Injectable({
  providedIn: 'root',
})
export class ReassignmentDialogService {
  dialog = inject(MatDialog);
  private archiveService = inject(ArchiveService);
  private snackBarService = inject(SnackBarService);

  launchReassignmentModal(listOfUACriteriaSearch: SearchCriteriaEltDto[], itemSelected: number, tenantIdentifier: number) {
    this.openReassignmentDialog(
      {
        itemSelected,
        reassignmentMode: ReassignmentMode.BY_ID,
      },
      (result) => {
        const searchCriteriaDto: SearchCriteriaDto = {
          criteriaList: listOfUACriteriaSearch,
          pageNumber: 0,
          size: 1,
        };

        return {
          reassignMode: ReassignmentMode.BY_ID,
          sourceOriginatingAgency: result.sourceOriginatingAgency,
          targetOriginatingAgency: result.targetOriginatingAgency,
          propagateToObjectGroups: result.propagateToObjectGroups,
          searchCriteria: searchCriteriaDto,
        };
      },
      'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.REASSIGNMENT_LAUNCHED',
      tenantIdentifier,
    );
  }

  launchEntryOperationReassignmentModal(tenantIdentifier: number, accessContract?: string) {
    this.openReassignmentDialog(
      {
        reassignmentMode: ReassignmentMode.BY_OPI,
        tenantIdentifier,
        accessContract,
      },
      (result) => {
        // Split + trim + remove empty + deduplicate
        const idsArray = [
          ...new Set(
            result.entryOperationIds
              .split(',')
              .map((id: string) => id.trim())
              .filter((id: string) => id.length > 0),
          ),
        ];

        if (!idsArray.length) {
          return null;
        }

        const criteriaList: SearchCriteriaEltDto[] = [
          {
            criteria: 'GUID_OPI',
            values: idsArray.map((id: string) => ({ value: id, id })),
            operator: CriteriaOperator.IN,
            category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
            dataType: CriteriaDataType.STRING,
          },
        ];

        return {
          reassignMode: ReassignmentMode.BY_OPI,
          sourceOriginatingAgency: result.sourceOriginatingAgency,
          targetOriginatingAgency: result.targetOriginatingAgency,
          propagateToObjectGroups: true,
          searchCriteria: {
            criteriaList,
            pageNumber: 0,
            size: 1,
          },
        };
      },
      'ARCHIVE_SEARCH.ENTRY_OPERATION_REASSIGNMENT.REASSIGNMENT_LAUNCHED',
      tenantIdentifier,
    );
  }

  private openReassignmentDialog(
    dialogData: ReassignmentDialogData,
    buildReassignRequest: (result: ReassignmentDialogResult) => ReassignRequestDto | null,
    successMessage: string,
    tenantIdentifier: number,
  ): void {
    const dialogRef = this.dialog.open(OriginatingAgencyReassignmentDialogComponent, {
      disableClose: true,
      data: dialogData,
    });

    dialogRef
      .afterClosed()
      .pipe(
        filter((result) => !!result),
        switchMap((result) => {
          const reassignDto = buildReassignRequest(result);
          if (!reassignDto) {
            return EMPTY;
          }

          return this.archiveService.launchReassignmentAction(reassignDto).pipe(
            catchError((error: HttpErrorResponse) => {
              console.log(error);
              this.handleReassignmentError(error);
              return EMPTY;
            }),
          );
        }),
        filter((opi) => !!opi),
      )
      .subscribe((opi) => {
        this.snackBarService.open({
          message: successMessage,
          buttons: [
            {
              appId: ApplicationId.LOGBOOK_OPERATION_APP,
              path: `/tenant/${tenantIdentifier}?guid=${opi}`,
              label: 'SNACK_BAR.TO_OPERATION_APP',
            },
          ],
          duration: 100_000,
        });
      });
  }

  private handleReassignmentError(err: HttpErrorResponse): void {
    let errorMessage = 'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.REASSIGNMENT_ERROR';

    if (err.status === 400) {
      // Bad Request - typically ERROR_MULTIPLE_SP
      if (err.error?.includes('ERROR_MULTIPLE_SP')) {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
          subhead: 'ARCHIVE_SEARCH.ENTRY_OPERATION_REASSIGNMENT.ERROR_MULTIPLE_SP_MODAL.SUBHEAD',
          title: 'ARCHIVE_SEARCH.ENTRY_OPERATION_REASSIGNMENT.ERROR_MULTIPLE_SP_MODAL.TITLE',
          icon: 'cancel',
          message: 'ARCHIVE_SEARCH.ENTRY_OPERATION_REASSIGNMENT.ERROR_MULTIPLE_SP_MODAL.MESSAGE',
          cancelLabel: 'RULES.ALERTE_MESSAGES.BACK_TO_SELECTION',
        };
        this.dialog.open(AlertDialogComponent, dialogConfig);
        return;
      }
      // Bad Request - typically INVALID_ARCHIVAL_UNIT_TYPE
      if (err.error?.includes('INVALID_ARCHIVAL_UNIT_TYPE')) {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
          subhead: 'ARCHIVE_SEARCH.ENTRY_OPERATION_REASSIGNMENT.ERROR_MULTIPLE_SP_MODAL.SUBHEAD',
          title: 'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.ALERT_MESSAGES.INVALID_ARCHIVAL_UNIT_TYPE',
          icon: 'cancel',
          message: 'RULES.ALERTE_MESSAGES.ACTION_ALERTE_FIRST_MESSAGE',
          cancelLabel: 'RULES.ALERTE_MESSAGES.BACK_TO_SELECTION',
        };
        this.dialog.open(AlertDialogComponent, dialogConfig);
        return;
      }
      errorMessage = 'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.BAD_REQUEST_ERROR';
    } else if (err.status === 500) {
      // Internal Server Error
      errorMessage = 'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.SERVER_ERROR';
    }

    this.snackBarService.open({
      message: errorMessage,
      icon: 'vitamui-icon-close',
      duration: 10000,
    });
  }
}
