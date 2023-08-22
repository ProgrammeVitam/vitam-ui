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
import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { Subject, Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { Direction, SearchCriteriaHistory } from 'ui-frontend-common';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { VitamUISnackBar, VitamUISnackBarComponent } from '../../shared/vitamui-snack-bar';
import { ConfirmActionComponent } from './confirm-action/confirm-action.component';
import { SearchCriteriaListService } from './search-criteria-list.service';

@Component({
  selector: 'app-search-criteria-list',
  templateUrl: './search-criteria-list.component.html',
  styleUrls: ['./search-criteria-list.component.css'],
})
export class SearchCriteriaListComponent implements OnInit, OnDestroy {
  @Output()
  storedSearchCriteriaHistory = new EventEmitter<any>();

  searchCriteriaHistory: SearchCriteriaHistory[];
  private readonly orderChange = new Subject<string>();
  direction: Direction = Direction.ASCENDANT;

  subscriptionSearchCriteriaHistoryShared: Subscription;
  subscriptionSearchCriteriaHistory: Subscription;
  keyPressSubscription: Subscription;

  pending = false;

  constructor(
    private searchCriteriaListService: SearchCriteriaListService,
    private archiveSharedDataService: ArchiveSharedDataService,
    public dialog: MatDialog,
    private snackBar: VitamUISnackBar,
    private translateService: TranslateService
  ) {}

  ngOnInit() {
    this.subscriptionSearchCriteriaHistoryShared = this.archiveSharedDataService
      .getSearchCriteriaHistoryShared()
      .subscribe((searchCriteriaHistoryResults) => {
        if (searchCriteriaHistoryResults) {
          this.searchCriteriaHistory.push(searchCriteriaHistoryResults);
          this.archiveSharedDataService.sort(Direction.ASCENDANT, this.searchCriteriaHistory);
        }
      });
    this.getSearchCriteriaHistory();
    this.direction = Direction.ASCENDANT;
  }

  ngOnDestroy(): void {
    this.subscriptionSearchCriteriaHistoryShared?.unsubscribe();
    this.subscriptionSearchCriteriaHistory?.unsubscribe();
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  getSearchCriteriaHistory() {
    this.pending = true;
    this.subscriptionSearchCriteriaHistory = this.searchCriteriaListService.getSearchCriteriaHistory().subscribe((data) => {
      this.searchCriteriaHistory = data;
      this.archiveSharedDataService.sort(Direction.ASCENDANT, this.searchCriteriaHistory);
      this.archiveSharedDataService.emitAllSearchCriteriaHistory(data);
      this.pending = false;
    });
  }

  deleteSearchCriteriaHistory(searchCriteriaHistory: SearchCriteriaHistory) {
    const dialog = this.dialog.open(ConfirmActionComponent, { panelClass: 'vitamui-confirm-dialog' });
    dialog.componentInstance.objectType = this.translateService.instant('ARCHIVE_SEARCH.SEARCH_CRITERIA_SAVER.OBJECT_TYPE');
    dialog.componentInstance.objectName = searchCriteriaHistory.name;
    dialog.componentInstance.objectDate = searchCriteriaHistory.savingDate;

    dialog
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.searchCriteriaListService.deleteSearchCriteriaHistory(searchCriteriaHistory.id).subscribe(() => {
          this.clearElement(searchCriteriaHistory.id);
          this.snackBar.openFromComponent(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: 'searchCriteriaHistoryDeleted', name: searchCriteriaHistory.name },
            duration: 10000,
          });
        });
      });
  }

  clearElement(id: string) {
    for (let i = 0; i < this.searchCriteriaHistory.length; i++) {
      if (this.searchCriteriaHistory[i].id === id) {
        this.searchCriteriaHistory.splice(i, 1);
      }
    }
  }
}
