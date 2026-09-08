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
import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { Ontology, SchemaElement, SchemaService } from 'vitamui-library';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { fromPromise } from 'rxjs/internal/observable/innerFrom';
import { MatTabChangeEvent } from '@angular/material/tabs';

import { MatTabsModule } from '@angular/material/tabs';
import { OntologyListComponent } from './ontology-list/ontology-list.component';
import { SchemaListComponent } from './schema-list/schema-list.component';
import { TranslatePipe } from '@ngx-translate/core';
import { OntologyService } from '../ontology.service';

@Component({
  imports: [MatTabsModule, TranslatePipe, OntologyListComponent, SchemaListComponent],
  selector: 'app-ontology-group',
  templateUrl: './ontology-group.component.html',
  styleUrls: ['./ontology-group.component.scss'],
})
export class OntologyGroupComponent {
  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);
  private schemaService = inject(SchemaService);
  private ontologyService = inject(OntologyService);

  @Input() searchText: string;
  @Output() selectElement = new EventEmitter<Ontology | SchemaElement>();

  tabIndex = 0;

  constructor() {
    this.activatedRoute.queryParams.subscribe((params) => {
      this.tabIndex = params['tab'];
    });
  }

  changeTab(matTabChangeEvent: MatTabChangeEvent) {
    this.setQueryParams({ tab: matTabChangeEvent.index });
    this.schemaService.selectedPath$.next('');
    this.ontologyService.selectedId$.next('');
  }

  private setQueryParams(queryParams: Params): Observable<boolean> {
    return fromPromise(
      this.router.navigate([], {
        queryParams,
        queryParamsHandling: 'merge', // Merge with existing query parameters
        replaceUrl: true, // Prevent navigation
      }),
    );
  }
}
