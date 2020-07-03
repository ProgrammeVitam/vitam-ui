import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmActionComponent} from 'projects/vitamui-library/src/public-api';
import {merge, Subject} from 'rxjs';
import {debounceTime, filter} from 'rxjs/operators';
import {DEFAULT_PAGE_SIZE, Direction, InfiniteScrollTable, PageRequest} from 'ui-frontend-common';

import {Ontology} from '../../../../../vitamui-library/src/lib/models/ontology';
import {OntologyService} from '../ontology.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-ontology-list',
  templateUrl: './ontology-list.component.html',
  styleUrls: ['./ontology-list.component.scss']
})
export class OntologyListComponent extends InfiniteScrollTable<Ontology> implements OnDestroy, OnInit {
  // tslint:disable-next-line:no-input-rename
  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  // tslint:disable-next-line:variable-name
  private _searchText: string;

  @Output() ontologyClick = new EventEmitter<Ontology>();

  orderBy = 'ShortName';
  direction = Direction.ASCENDANT;

  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<string>();

  constructor(
    public ontologyService: OntologyService,
    private matDialog: MatDialog
  ) {
    super(ontologyService);
  }

  ngOnInit() {
    this.pending = true;
    this.ontologyService.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT))
      .subscribe((data: Ontology[]) => {
          this.dataSource = data;
        },
        () => {
        },
        () => this.pending = false);

    const searchCriteriaChange = merge(this.searchChange, this.orderChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildOntologyCriteriaFromSearch();
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
    });
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
    this.updatedData.unsubscribe();
  }

  searchOntologyOrdered() {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT));
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  deleteOntologyDialog(ontology: Ontology) {
    const dialog = this.matDialog.open(ConfirmActionComponent, {panelClass: 'vitamui-confirm-dialog'});

    dialog.componentInstance.objectType = 'ontologie';
    dialog.componentInstance.objectName = ontology.identifier;

    dialog.afterClosed().pipe(
      filter((result) => !!result)
    ).subscribe(() => {
      this.ontologyService.delete(ontology).subscribe(
        () => {
          this.searchOntologyOrdered();
        }
      );
    });


  }

}
