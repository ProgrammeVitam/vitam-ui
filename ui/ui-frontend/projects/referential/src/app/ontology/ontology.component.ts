import {Component, OnInit, ViewChild} from '@angular/core';

import {
  GlobalEventService,
  SidenavPage
} from 'ui-frontend-common';
import { MatDialog } from "@angular/material/dialog";
import { ActivatedRoute } from "@angular/router";
import { Ontology } from "projects/vitamui-library/src/lib/models/ontology";
import { OntologyCreateComponent } from "./ontology-create/ontology-create.component";
import { OntologyListComponent } from "./ontology-list/ontology-list.component";

@Component({
  selector: 'app-ontology',
  templateUrl: './ontology.component.html',
  styleUrls: ['./ontology.component.scss']
})
export class OntologyComponent extends SidenavPage<Ontology> implements OnInit {

  search: string = '';

  @ViewChild(OntologyListComponent, { static: true }) ontologyListComponent: OntologyListComponent;

  constructor(public dialog: MatDialog, route: ActivatedRoute, globalEventService: GlobalEventService) {
    super(route, globalEventService);
  }

  openCreateOntologyDialog() {
    const dialogRef = this.dialog.open(OntologyCreateComponent, { panelClass: 'vitamui-modal', disableClose: true });
    dialogRef.afterClosed().subscribe((result) => {
      if (result.success) { this.refreshList(); }
      if (result.action === "restart") { this.openCreateOntologyDialog(); }
    });
  }

  private refreshList() {
    if (!this.ontologyListComponent) { return; }
    this.ontologyListComponent.searchOntologyOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  ngOnInit() { }

  showOntology(item: Ontology) {
    this.openPanel(item)
  }

}
