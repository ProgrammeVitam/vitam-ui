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
import { defer, merge, Observable, Subject } from 'rxjs';
import { startWith, switchMap, take, takeUntil } from 'rxjs/operators';

import { SelectionModel } from '@angular/cdk/collections';
import {
  AfterContentInit, Component, ContentChildren, EventEmitter, HostListener, Input, NgZone,
  OnDestroy, OnInit, Output, QueryList
} from '@angular/core';

import { TableFilterOptionComponent } from './table-filter-option/table-filter-option.component';

@Component({
  selector: 'vitamui-common-table-filter',
  templateUrl: './table-filter.component.html',
  styleUrls: ['./table-filter.component.scss']
})
export class TableFilterComponent implements AfterContentInit, OnInit, OnDestroy {

  @Input()
  set filter(values: any[]) {
    this._filter = values || [];
  }
  // tslint:disable-next-line:variable-name
  private _filter: any[];

  @Input() showSearchBar = false;

  @Output() readonly filterChange = new EventEmitter<any[]>();
  @Output() readonly search = new EventEmitter<string>();
  @Output() readonly filterClose = new EventEmitter();

  @ContentChildren(TableFilterOptionComponent, { descendants: true }) options: QueryList<TableFilterOptionComponent>;

  searchText: string;

  /** Combined stream of all of the child options' change events. */
  readonly optionSelectionChanges: Observable<TableFilterOptionComponent> = defer(() => {
    if (this.options) {
      return this.options.changes.pipe(
        startWith(this.options),
        switchMap(() => merge(...this.options.map((option) => option.selectionChange)))
      );
    }

    return this.ngZone.onStable
      .asObservable()
      .pipe(take(1), switchMap(() => this.optionSelectionChanges));
  }) as Observable<TableFilterOptionComponent>;

  private readonly destroy = new Subject<void>();
  private selectionModel: SelectionModel<TableFilterOptionComponent>;

  constructor(private ngZone: NgZone) { }

  ngOnInit() {
    this.selectionModel = new SelectionModel<TableFilterOptionComponent>(true);
  }

  ngOnDestroy() {
    this.destroy.next();
    this.destroy.complete();
  }

  ngAfterContentInit() {
    this.selectionModel.changed.pipe(takeUntil(this.destroy)).subscribe((event) => {
      event.added.forEach((option) => option.select());
      event.removed.forEach((option) => option.deselect());
    });

    this.options.changes.pipe(startWith(null), takeUntil(this.destroy)).subscribe(() => {
      this.resetOptions();
      this.initializeSelection();
    });
  }

  @HostListener('document:keydown', ['$event'])
  onKeyDown(event: KeyboardEvent) {
    if (event.key === 'Escape') {
      this.filterClose.emit();
    }
  }

  onSearchChange(text: string) {
    this.search.emit(text);
  }

  cancelSearch() {
    this.searchText = null;
    this.onSearchChange(null);
  }

  onInputKeyDown(event: KeyboardEvent) {
    if (event.key === 'Escape') {
      if (this.searchText) {
        event.preventDefault();
        event.stopPropagation();
        this.cancelSearch();
      }
    }
  }

  private selectValue(value: any) {
    const filterOption = this.options.find((option) => option.value === value);

    if (!filterOption) {
      return;
    }

    this.selectionModel.select(filterOption);
  }

  private resetOptions() {
    const changedOrDestroyed = merge(this.options.changes, this.destroy);

    this.optionSelectionChanges.pipe(takeUntil(changedOrDestroyed)).subscribe((filterOption) => {
      filterOption.selected ? this.selectionModel.select(filterOption) : this.selectionModel.deselect(filterOption);
      this.filterChange.emit(this.selectionModel.selected.map((option) => option.value));
    });
  }

  private initializeSelection() {
    if (!this.options) {
      return;
    }

    this.selectionModel.clear();

    if (!this._filter) {
      return;
    }

    this._filter.forEach((currentValue) => this.selectValue(currentValue));
  }

}
