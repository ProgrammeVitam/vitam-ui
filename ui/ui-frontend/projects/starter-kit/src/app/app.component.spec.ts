import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { waitForAsync } from '@angular/core/testing';
import { TestBed } from '@angular/core/testing';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
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
/* tslint:disable:component-selector max-classes-per-file */
import { of } from 'rxjs';
import { BASE_URL } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { environment } from './../environments/environment.prod';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AuthService, ENVIRONMENT, InjectorModule, LoggerModule, StartupService } from 'ui-frontend-common';
import { AppComponent } from './app.component';

@Component({ selector: 'router-outlet', template: '' })
class RouterOutletStubComponent { }

describe('AppComponent', () => {

  beforeEach(waitForAsync(() => {
    const startupServiceStub = {
      configurationLoaded: () => true,
      printConfiguration: () => { },
      getPlatformName: () => '',
      load: () => { },
      getPortalUrl: () => '',
      getCustomerTechnicalReferentEmail: () => '',
      getCustomerWebsiteUrl: () => ''
    };

    TestBed.configureTestingModule({
      declarations: [
        AppComponent,
        RouterOutletStubComponent,
      ],
      imports: [
        HttpClientTestingModule,
        MatSnackBarModule,
        InjectorModule,
        VitamUICommonTestModule,
        BrowserAnimationsModule,
        LoggerModule.forRoot(),
      ],
      providers: [
        { provide: StartupService, useValue: startupServiceStub },
        { provide: AuthService, useValue: { userLoaded: of(null) } },
        { provide: Router, useValue: { navigate: () => { } } },
        { provide: ENVIRONMENT, useValue: environment },
        { provide: BASE_URL, useValue: '/fake-api' },
      ]
    }).compileComponents();
  }));

  it('should create the app Starter-kit', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    fixture.detectChanges();
    console.log('Create App: ', app);
    expect(app).toBeTruthy();
  });

  it(`should have as title Starter-kit'`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const title = fixture.componentInstance.title;
    console.log('Title App: ', title);
    expect(title).toEqual('Starter-kit App');
  });

});
