import {Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {Event} from 'projects/vitamui-library/src/public-api';
import {GlobalEventService, SearchBarComponent, SidenavPage} from 'ui-frontend-common';

import {ProbativeValueCreateComponent} from './probative-value-create/probative-value-create.component';
import {ProbativeValueListComponent} from './probative-value-list/probative-value-list.component';

@Component({
  selector: 'app-probative-value',
  templateUrl: './probative-value.component.html',
  styleUrls: ['./probative-value.component.scss']
})
export class ProbativeValueComponent extends SidenavPage<Event> implements OnInit {

  search: string;
  dateRangeFilterForm: FormGroup;
  filters: any = {};

  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;
  @ViewChild(ProbativeValueListComponent, {static: true}) probativeValueListComponent: ProbativeValueListComponent;

  constructor(
    public dialog: MatDialog,
    private router: Router,
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    private formBuilder: FormBuilder) {
    super(route, globalEventService);

    this.dateRangeFilterForm = this.formBuilder.group({
      startDate: null,
      endDate: null
    });

    this.dateRangeFilterForm.controls.startDate.valueChanges.subscribe(value => {
      this.filters.startDate = value;
      this.probativeValueListComponent.filters = this.filters;
    });
    this.dateRangeFilterForm.controls.endDate.valueChanges.subscribe((value: Date) => {
      if (value) {
        value.setDate(value.getDate() + 1);
      }
      this.filters.endDate = value;
      this.probativeValueListComponent.filters = this.filters;
    });
  }

  openCreateProbativeValueDialog() {
    const dialogRef = this.dialog.open(ProbativeValueCreateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true
    });
    dialogRef.afterClosed().subscribe((result: any) => {
      if (result !== undefined && result.success) {
        this.refreshList();
      }
    });
  }

  private refreshList() {
    if (!this.probativeValueListComponent) {
      return;
    }
    this.probativeValueListComponent.searchProbativeValueOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  clearDate(date: 'startDate' | 'endDate') {
    if (date === 'startDate') {
      this.dateRangeFilterForm.get(date).reset(null, {emitEvent: false});
    } else if (date === 'endDate') {
      this.dateRangeFilterForm.get(date).reset(null, {emitEvent: false});
    } else {
      console.error('clearDate() error: unknown date ' + date);
    }
  }

  resetFilters() {
    this.dateRangeFilterForm.reset();
    this.searchBar.reset();
  }

  ngOnInit() {
  }

  showProbativeValue(item: Event) {
    this.openPanel(item);
  }

  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], {relativeTo: this.route});
  }
}
