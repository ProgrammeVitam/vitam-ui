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
import { MatDialog } from '@angular/material/dialog';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, filter, switchMap } from 'rxjs/operators';
import { EMPTY } from 'rxjs';
import { ApplicationId, SearchCriteriaDto, SearchCriteriaEltDto, SnackBarService } from 'vitamui-library';
import { ArchiveService } from '../../../archive.service';
import { PreservationDialogComponent } from './preservation-dialog.component';
import { PreservationRequestDto } from '../../../models/preservation-request.interface';

interface PreservationDialogResult {
  sourceUsage: PreservationRequestDto['sourceUsage'];
  version: PreservationRequestDto['version'];
  scenarioIdentifier: string;
  targetUsage?: PreservationRequestDto['targetUsage'];
}

@Injectable({
  providedIn: 'root',
})
export class PreservationDialogService {
  dialog = inject(MatDialog);
  private archiveService = inject(ArchiveService);
  private snackBarService = inject(SnackBarService);

  launchPreservationModal(listOfUACriteriaSearch: SearchCriteriaEltDto[], itemSelected: number, tenantIdentifier: number): void {
    const dialogRef = this.dialog.open(PreservationDialogComponent, {
      disableClose: true,
      data: { itemSelected, criteriaList: listOfUACriteriaSearch },
    });

    dialogRef
      .afterClosed()
      .pipe(
        filter((result: PreservationDialogResult) => !!result),
        switchMap((result: PreservationDialogResult) => {
          const searchCriteriaDto: SearchCriteriaDto = {
            criteriaList: listOfUACriteriaSearch,
            pageNumber: 0,
            size: 1,
          };

          const preservationRequestDto: PreservationRequestDto = {
            scenarioIdentifier: result.scenarioIdentifier,
            sourceUsage: result.sourceUsage,
            targetUsage: result.targetUsage,
            version: result.version,
            searchCriteria: searchCriteriaDto,
          };

          return this.archiveService.launchPreservation(preservationRequestDto).pipe(
            catchError((error: HttpErrorResponse) => {
              this.handlePreservationError(error);
              return EMPTY;
            }),
          );
        }),
        filter((opi) => !!opi),
      )
      .subscribe((opi) => {
        this.snackBarService.open({
          message: 'ARCHIVE_SEARCH.PRESERVATION.LAUNCHED',
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

  private handlePreservationError(error: HttpErrorResponse): void {
    console.log(error);
    this.snackBarService.open({
      message: 'ARCHIVE_SEARCH.PRESERVATION.ERROR',
      icon: 'vitamui-icon-close',
      duration: 10000,
    });
  }
}
