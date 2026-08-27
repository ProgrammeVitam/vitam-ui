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
import { Component, Input, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterTestingModule } from '@angular/router/testing';
import { Application, ApplicationService, GlobalEventService, InjectorModule, LoggerModule } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';

import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { EMPTY, of } from 'rxjs';
import { ContextComponent } from './context.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ContextListComponent } from './context-list/context-list.component';

@Component({
  selector: 'app-agency-preview',
  template: '',
  imports: [VitamUICommonTestModule, RouterTestingModule, InjectorModule, MatSidenavModule, MatDialogModule],
})
class ContextPreviewStub {
  @Input()
  accessContract: any;
}

describe('ContextComponent', () => {
  let component: ContextComponent;
  let fixture: ComponentFixture<ContextComponent>;

  const applicationServiceMock = {
    applications: new Array<any>(),
    isApplicationExternalIdentifierEnabled: () => of(true),
    getAppById: () =>
      of({
        name: 'App name',
      } satisfies Partial<Application>),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        VitamUICommonTestModule,
        RouterTestingModule,
        InjectorModule,
        LoggerModule.forRoot(),
        MatSidenavModule,
        MatDialogModule,
        ContextPreviewStub,
      ],
      providers: [
        { provide: ActivatedRoute, useValue: { params: EMPTY, data: EMPTY, paramMap: EMPTY, snapshot: { data: { appId: 'App Id' } } } },
        { provide: GlobalEventService, useValue: { pageEvent: EMPTY, customerEvent: EMPTY, tenantEvent: EMPTY } },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    })
      .overrideProvider(ApplicationService, { useValue: applicationServiceMock })
      .overrideComponent(ContextListComponent, {
        set: { template: '' },
      })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
