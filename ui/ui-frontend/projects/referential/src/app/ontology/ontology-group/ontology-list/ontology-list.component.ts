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
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { finalize, merge, Subject, Subscription } from 'rxjs';
import { debounceTime, filter, takeUntil } from 'rxjs/operators';
import {
  ConfirmActionComponent,
  DEFAULT_PAGE_SIZE,
  Direction,
  InfiniteScrollTable,
  IOntology,
  Ontology,
  PageRequest,
  VitamUICommonModule,
  TenantSelectionService,
} from 'vitamui-library';

import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { OntologyService } from '../../ontology.service';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  imports: [CommonModule, TranslatePipe, VitamUICommonModule, MatProgressSpinnerModule],
  selector: 'app-ontology-list',
  templateUrl: './ontology-list.component.html',
  styleUrls: ['./ontology-list.component.scss'],
})
export class OntologyListComponent extends InfiniteScrollTable<Ontology> implements OnDestroy, OnInit {
  @Input()
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  private _searchText: string;

  @Output() ontologyClick = new EventEmitter<Ontology>();

  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<void>();
  private destroy$ = new Subject<void>();

  protected readonly shortName: keyof IOntology = 'ShortName';
  protected readonly identifier: keyof IOntology = 'Identifier';
  protected readonly creationDate: keyof IOntology = 'CreationDate';
  orderBy: keyof IOntology;
  direction = Direction.ASCENDANT;

  private subscriptions = new Subscription();

  constructor(
    public ontologyService: OntologyService,
    private translateService: TranslateService,
    private matDialog: MatDialog,
    private tenantSelectionService: TenantSelectionService,
  ) {
    super(ontologyService);
    this.orderBy = this.shortName;
  }

  ngOnInit() {
    this.pending = true;
    this.ontologyService
      .search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.shortName, Direction.ASCENDANT))
      .pipe(finalize(() => (this.pending = false)))
      .subscribe({
        next: (data: Ontology[]) => {
          this.dataSource = data;
        },
        error: (e) => console.error(e),
      });
    this.replaceUpdatedOntology();
    this.searchOnOrderChange();
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  private searchOnOrderChange() {
    this.subscriptions.add(
      merge(this.searchChange, this.orderChange)
        .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS))
        .subscribe(() => {
          const query: any = this.buildOntologyCriteriaFromSearch();
          const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
          this.search(pageRequest);
        }),
    );
  }

  buildOntologyCriteriaFromSearch() {
    const criteria: any = {};
    if (this._searchText.length > 0) {
      criteria.ShortName = this._searchText;
      criteria.Identifier = this._searchText;
    }
    return criteria;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    this.updatedData.unsubscribe();
  }

  searchOntologyOrdered() {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT));
  }

  deleteOntologyDialog(ontology: Ontology) {
    const dialog = this.matDialog.open(ConfirmActionComponent, { panelClass: 'small' });

    dialog.componentInstance.objectType = this.translateService.instant('ONTOLOGY.HOME.TITLE');
    dialog.componentInstance.objectName = ontology.identifier;

    dialog
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.ontologyService.delete(ontology).subscribe(() => {
          this.searchOntologyOrdered();
        });
      });
  }

  private replaceUpdatedOntology(): void {
    this.subscriptions.add(
      this.ontologyService.updated.pipe(takeUntil(this.destroy$)).subscribe((updatedOntology: Ontology) => {
        const index = this.dataSource.findIndex((item: Ontology) => item.id === updatedOntology.id);
        if (index !== -1) {
          this.dataSource[index] = updatedOntology;
        }
      }),
    );
  }

  selectLine(ontology: Ontology) {
    this.ontologyService.selectedId$.next(ontology.id);
    this.ontologyClick.emit(ontology);
  }

  canDelete(ontology: Ontology) {
    return ontology.origin === 'EXTERNAL' && ontology.tenant === this.tenantSelectionService.getSelectedTenant().identifier;
  }
}
