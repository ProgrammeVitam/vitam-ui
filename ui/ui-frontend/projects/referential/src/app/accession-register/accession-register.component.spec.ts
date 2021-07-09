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
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessionRegisterComponent } from './accession-register.component';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { BASE_URL, ENVIRONMENT, InjectorModule, LoggerModule, RoleToggleModule, TableFilterModule } from 'ui-frontend-common';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatDialogModule } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { AccessionRegisterBusiness } from './accession-register.business';
import { of } from 'rxjs';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SharedModule } from '../shared/shared.module';
import { VitamUILibraryModule } from '../../../../vitamui-library/src/lib/vitamui-library.module';
import { AccessionRegisterRoutingModule } from './accession-register-routing.module';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';
import { GroupAttributionModule } from '../../../../identity/src/app/user/group-attribution/group-attribution.module';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatCardModule } from '@angular/material/card';
import { MatPseudoCheckboxModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { environment } from '../../../../archive-search/src/environments/environment.prod';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import {TranslateModule} from "@ngx-translate/core";

describe('AccessionRegisterComponent', () => {
  let component: AccessionRegisterComponent;
  let fixture: ComponentFixture<AccessionRegisterComponent>;

  const accessionRegisterBusinessMock = {
    getAccessionRegisterStatus: () => of([]),
    setOpenAdvancedSearchPanel: () => {},
    toggleOpenAdvancedSearchPanel: () => {},
    isOpenAdvancedSearchPanel: () => of(true),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        VitamUICommonTestModule,
        RouterTestingModule,
        InjectorModule,
        LoggerModule.forRoot(),
        NoopAnimationsModule,
        ReactiveFormsModule,
        MatMenuModule,
        MatSnackBarModule,
        MatDialogModule,
        MatSidenavModule,
        MatProgressSpinnerModule,
        SharedModule,
        TableFilterModule,
        VitamUILibraryModule,
        AccessionRegisterRoutingModule,
        MatButtonToggleModule,
        MatSelectModule,
        GroupAttributionModule,
        MatProgressBarModule,
        MatTabsModule,
        RoleToggleModule,
        MatCheckboxModule,
        MatCardModule,
        MatPseudoCheckboxModule,
        MatDatepickerModule,
        TranslateModule.forRoot()
      ],
      declarations: [AccessionRegisterComponent],
      providers: [
        { provide: AccessionRegisterBusiness, useValue: accessionRegisterBusinessMock },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessionRegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
