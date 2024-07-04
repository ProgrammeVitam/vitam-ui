/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  AccessionRegisterDetail,
  ExternalParameters,
  ExternalParametersService,
  SidenavPage,
  VitamuiTitleBreadcrumbComponent,
  SearchBarWithSiblingButtonComponent,
} from 'vitamui-library';
import { AccessionRegistersService } from './accession-register.service';
import { TranslateModule } from '@ngx-translate/core';
import { AccessionRegisterListComponent } from './accession-register-list/accession-register-list.component';
import { AccessionRegisterFacetsComponent } from './accession-register-facets/accession-register-facets.component';
import { AccessionRegisterAdvancedSearchComponent } from './accession-register-advanced-search/accession-register-advanced-search.component';
import { AccessionRegisterPreviewComponent } from './accession-register-preview/accession-register-preview.component';
import { NgClass, NgIf, AsyncPipe } from '@angular/common';
import { MatSidenavModule } from '@angular/material/sidenav';

@Component({
  selector: 'app-accession-register',
  templateUrl: './accession-register.component.html',
  styleUrls: ['./accession-register.component.scss'],
  standalone: true,
  imports: [
    MatSidenavModule,
    NgClass,
    NgIf,
    AccessionRegisterPreviewComponent,
    VitamuiTitleBreadcrumbComponent,
    SearchBarWithSiblingButtonComponent,
    AccessionRegisterAdvancedSearchComponent,
    AccessionRegisterFacetsComponent,
    AccessionRegisterListComponent,
    AsyncPipe,
    TranslateModule,
  ],
})
export class AccessionRegisterComponent extends SidenavPage<AccessionRegisterDetail> implements OnInit, OnDestroy {
  search: string;
  advancedSearchPanelOpenState$: Observable<boolean>;
  isAdvancedFormChanged$: Observable<boolean>;
  accessContract: string;

  constructor(
    private accessionRegistersService: AccessionRegistersService,
    route: ActivatedRoute,
    private externalParameterService: ExternalParametersService,
  ) {
    super(route, accessionRegistersService);
  }

  ngOnInit(): void {
    this.advancedSearchPanelOpenState$ = this.accessionRegistersService.isOpenAdvancedSearchPanel();
    this.isAdvancedFormChanged$ = this.accessionRegistersService.isAdvancedFormChanged();
    this.fetchUserAccessContract().subscribe((accessContract) => {
      this.accessContract = accessContract;
    });
  }

  fetchUserAccessContract(): Observable<string> {
    return this.externalParameterService
      .getUserExternalParameters()
      .pipe(map((parameters) => parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT)));
  }

  ngOnDestroy() {
    super.ngOnDestroy();
  }

  onSearchTextChanged(search: string) {
    this.search = search;
  }

  onSearchSubmit() {
    this.accessionRegistersService.setGlobalSearchButtonEvent(true);
  }

  openAdvancedSearchPanel() {
    this.accessionRegistersService.toggleOpenAdvancedSearchPanel();
  }

  resetAdvancedSearch() {
    this.accessionRegistersService.setGlobalResetEvent(true);
  }

  accessionRegisterClick(item: AccessionRegisterDetail) {
    this.openPanel(item);
  }
}
