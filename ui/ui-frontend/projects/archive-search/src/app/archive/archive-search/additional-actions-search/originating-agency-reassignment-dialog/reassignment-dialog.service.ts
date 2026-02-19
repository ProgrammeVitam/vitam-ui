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
import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { OriginatingAgencyReassignmentDialogComponent } from './originating-agency-reassignment-dialog.component';
import { ApplicationId, SearchCriteriaDto, SearchCriteriaEltDto, SnackBarService } from 'vitamui-library';
import { ArchiveService } from '../../../archive.service';
import { ReassignRequestDto } from '../../../models/reassign-request.interface';
import { filter, switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ReassignmentDialogService {
  constructor(
    public dialog: MatDialog,
    private archiveService: ArchiveService,
    private snackBarService: SnackBarService,
  ) {}

  lanchReassignmentModal(listOfUACriteriaSearch: SearchCriteriaEltDto[], itemSelected: number, tenantIdentifier: number) {
    const dialogRef = this.dialog.open(OriginatingAgencyReassignmentDialogComponent, {
      disableClose: false,
      data: {
        itemSelected,
      },
    });

    dialogRef
      .afterClosed()
      .pipe(
        filter((result) => !!result),
        switchMap((result) => {
          const searchCriteriaDto: SearchCriteriaDto = {
            criteriaList: listOfUACriteriaSearch,
            pageNumber: 0,
            size: 1,
          };

          const reassignDto: ReassignRequestDto = {
            ...result,
            searchCriteria: searchCriteriaDto,
          };
          return this.archiveService.launchReassignmentAction(reassignDto);
        }),
        filter((opi) => !!opi),
      )
      .subscribe((opi) => {
        this.snackBarService.open({
          message: 'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.REASSIGNMENT_LAUNCHED',
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
}
