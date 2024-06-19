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
import { Component, CUSTOM_ELEMENTS_SCHEMA, Input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterTestingModule } from '@angular/router/testing';
import { InjectorModule, LoggerModule, SecurityService } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';

import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { AgencyComponent } from './agency.component';
import { AgencyService } from './agency.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({ selector: 'app-agency-preview', template: '' })
// eslint-disable-next-line @angular-eslint/component-class-suffix
class AgencyPreviewStub {
  @Input()
  accessContract: any;
}

@Component({ selector: 'app-agency-list', template: '' })
// eslint-disable-next-line @angular-eslint/component-class-suffix
class AgencyListStub {}

const securityServiceMock = {
  hasRole: () => of(true),
};

describe('AgencyComponent', () => {
  let component: AgencyComponent;
  let fixture: ComponentFixture<AgencyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AgencyComponent, AgencyListStub, AgencyPreviewStub],
      imports: [
        VitamUICommonTestModule,
        RouterTestingModule,
        InjectorModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot(),
        NoopAnimationsModule,
        MatSidenavModule,
        MatDialogModule,
        MatMenuModule,
      ],
      providers: [
        { provide: AgencyService, useValue: {} },
        { provide: SecurityService, useValue: securityServiceMock },
        {
          provide: ActivatedRoute,
          useValue: { params: of({ tenantIdentifier: 1 }), data: of({ appId: 'AGENCIES_APP' }) },
        },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AgencyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
