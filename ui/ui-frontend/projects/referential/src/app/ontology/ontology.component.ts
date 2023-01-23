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
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';

import { Ontology } from 'projects/vitamui-library/src/lib/models/ontology';
import { GlobalEventService, SidenavPage } from 'ui-frontend-common';
import { Referential } from '../shared/vitamui-import-dialog/referential.enum';
import { VitamUIImportDialogComponent } from '../shared/vitamui-import-dialog/vitamui-import-dialog.component';
import { OntologyCreateComponent } from './ontology-create/ontology-create.component';
import { OntologyListComponent } from './ontology-list/ontology-list.component';

@Component({
  selector: 'app-ontology',
  templateUrl: './ontology.component.html',
  styleUrls: ['./ontology.component.scss'],
})
export class OntologyComponent extends SidenavPage<Ontology> implements OnInit {
  search = '';

  @ViewChild(OntologyListComponent, { static: true }) ontologyListComponent: OntologyListComponent;

  constructor(public dialog: MatDialog, route: ActivatedRoute, globalEventService: GlobalEventService) {
    super(route, globalEventService);
  }

  openCreateOntologyDialog() {
    const dialogRef = this.dialog.open(OntologyCreateComponent, { panelClass: 'vitamui-modal', disableClose: true });
    dialogRef.afterClosed().subscribe((result) => {
      if (result.success) {
        this.refreshList();
      }
      if (result.action === 'restart') {
        this.openCreateOntologyDialog();
      }
    });
  }

  private refreshList() {
    if (!this.ontologyListComponent) {
      return;
    }
    this.ontologyListComponent.searchOntologyOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  ngOnInit() {}

  showOntology(item: Ontology) {
    this.openPanel(item);
  }

  openOntologyImportDialog() {
    const dialogRef = this.dialog.open(VitamUIImportDialogComponent, {
      panelClass: 'vitamui-modal',
      data: Referential.ONTOLOGY,
      disableClose: true,
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.success) {
        this.refreshList();
      }
    });
  }
}
