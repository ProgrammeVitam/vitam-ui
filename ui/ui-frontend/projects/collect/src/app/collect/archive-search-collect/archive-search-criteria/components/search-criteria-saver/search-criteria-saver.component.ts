/*Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
and the signatories of the "VITAM - Accord du Contributeur" agreement.

contact@programmevitam.fr

This software is a computer program whose purpose is to implement
implement a digital archiving front-office system for the secure and
efficient high volumetry VITAM solution.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { ConfirmDialogService, Direction } from 'ui-frontend-common';
import { ArchiveSharedDataService } from '../../services/archive-shared-data.service';
import { SearchCriteriaHistory } from '../../models/search-criteria-history.interface';
import { SearchCriteria, SearchCriteriaTypeEnum } from '../../models/search.criteria';
import { SearchCriteriaSaverService } from '../../services/search-criteria-saver.service';
import { VitamUISnackBarComponent } from '../../../../shared/vitamui-snack-bar';

@Component({
  selector: 'app-search-criteria-saver',
  templateUrl: './search-criteria-saver.component.html',
  styleUrls: ['./search-criteria-saver.component.css'],
})
export class SearchCriteriaSaverComponent implements OnInit, OnDestroy {
  searchCriteriaForm: FormGroup;
  criteria: string;
  searchCriteriaHistory: SearchCriteriaHistory;
  searchCriterias: Map<string, SearchCriteria>;
  nbCriterias = 0;
  nameControl: string;
  ToUpdate = true;
  noScroll = false;
  showScrollFilter = false;
  showScroll = false;
  updateConfirm = false;
  subscriptionAllSearchCriteriaHistory: Subscription;
  searchCriteriaHistories: SearchCriteriaHistory[];
  events: any[] = [];
  criteriaId = '';
  criteriaToUpdate: SearchCriteriaHistory;
  saveSearchCriteriaHistorySubscription: Subscription;
  updateSearchCriteriaHistorySubscription: Subscription;
  maxlength = 150;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<SearchCriteriaSaverComponent>,
    private formBuilder: FormBuilder,
    private searchCriteriaSaverService: SearchCriteriaSaverService,
    private archiveExchangeDataService: ArchiveSharedDataService,
    private confirmDialogService: ConfirmDialogService,
    private snackBar: MatSnackBar
  ) {
    this.searchCriteriaForm = this.formBuilder.group({
      searchCriteriaForm: null,
      name: null,
    });

    this.criteria = data.criteria;
    this.searchCriteriaHistory = data.searchCriteriaHistory;
    this.nbCriterias = data.nbCriterias;
    this.searchCriterias = data.originalSearchCriteria;

    this.subscriptionAllSearchCriteriaHistory = this.archiveExchangeDataService.getAllSearchCriteriaHistoryShared().subscribe((results) => {
      if (results) {
        this.searchCriteriaHistories = results;
        this.archiveExchangeDataService.sort(Direction.ASCENDANT, this.searchCriteriaHistories);
      }
    });
  }
  ngOnDestroy(): void {
    this.updateSearchCriteriaHistorySubscription?.unsubscribe();
    this.saveSearchCriteriaHistorySubscription?.unsubscribe();
  }

  ngOnInit(): void {
    this.nameControl = '';
  }

  onCancel() {
    this.searchCriteriaForm.dirty ? this.confirmDialogService.confirmBeforeClosing(this.dialogRef) : this.dialogRef.close();
  }

  onSubmit() {
    this.searchCriteriaHistory.name = this.searchCriteriaForm.value.name;
    this.saveSearchCriteriaHistorySubscription = this.searchCriteriaSaverService
      .saveSearchCriteriaHistory(this.searchCriteriaHistory)
      .subscribe(
        (response) => {
          this.searchCriteriaHistory.id = response.id;
          this.archiveExchangeDataService.emitSearchCriteriaHistory(this.searchCriteriaHistory);
          this.dialogRef.close(true);
          this.snackBar.openFromComponent(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: 'searchCriteriaHistoryCreated', name: response.name },
            duration: 10000,
          });
        },
        (error) => {
          this.dialogRef.close(false);
          this.snackBar.open(error.error.message, null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
          });
        }
      );
  }

  preUpdate(criteria: SearchCriteriaHistory, event: any) {
    const className = 'dynamic-color';
    if (this.events.length > 0) {
      if (this.criteriaId === criteria.id) {
        event.target.classList.remove(className);
        this.events.pop();
        this.criteriaId = '';
        this.criteriaToUpdate = null;
      } else {
        this.events[0].target.classList.remove(className);
        this.events.pop();
        if (event.target.classList.contains(className)) {
          event.target.classList.remove(className);
        } else {
          event.target.classList.add(className);
        }
        this.criteriaId = criteria.id;
        this.criteriaToUpdate = criteria;
      }
    } else {
      this.criteriaToUpdate = criteria;
    }

    this.events.push(event);

    if (this.criteriaToUpdate) {
      this.ToUpdate = null;
      this.updateConfirm = true;
      this.noScroll = true;
      this.criteriaId = '';
    }
  }

  createNewCriteria() {
    this.ToUpdate = false;
  }

  cancel() {
    this.updateConfirm = false;
    this.criteriaToUpdate = null;
    this.ToUpdate = true;
  }

  update() {
    this.criteriaToUpdate.searchCriteriaList = this.searchCriteriaHistory.searchCriteriaList;
    this.criteriaToUpdate.savingDate = new Date().toISOString();
    this.updateSearchCriteriaHistorySubscription = this.searchCriteriaSaverService
      .updateSearchCriteriaHistory(this.criteriaToUpdate)
      .subscribe(
        () => {
          this.dialogRef.close(true);
          this.snackBar.openFromComponent(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: 'searchCriteriaHistoryCreated', name: this.criteriaToUpdate.name },
            duration: 10000,
          });
        },
        (error) => {
          this.snackBar.open(error.error.message, null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
          });
        }
      );
  }

  getNbFilters(criteria: SearchCriteriaHistory): number {
    return this.archiveExchangeDataService.nbFilters(criteria);
  }

  closeSaveCriteriaForm() {
    this.dialogRef.close(true);
  }

  over(eventType: string) {
    switch (eventType) {
      case 'scroll-results':
        this.showScroll = true;
        this.noScroll = false;
        break;
      case 'scroll-filters':
        this.showScrollFilter = true;
        this.noScroll = false;
        break;
      default:
        break;
    }
  }
  out(eventType: string) {
    switch (eventType) {
      case 'scroll-results':
        this.showScroll = false;
        break;
      case 'scroll-filters':
        this.showScrollFilter = false;
        this.noScroll = false;
        break;
      default:
        break;
    }
  }

  getCategoryName(categoryEnum: SearchCriteriaTypeEnum): string {
    return SearchCriteriaTypeEnum[categoryEnum];
  }
}
