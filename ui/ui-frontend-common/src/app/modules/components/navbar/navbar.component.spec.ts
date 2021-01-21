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
/* tslint:disable directive-selector */
/* tslint:disable:max-classes-per-file */
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component, Directive, HostListener, Input } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBarModule, MatSnackBarRef } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { environment } from './../../../../environments/environment';
import { ENVIRONMENT } from './../../injection-tokens';

import { ApplicationId } from '../../application-id.enum';
import { AuthService } from '../../auth.service';
import { BASE_URL, SUBROGRATION_REFRESH_RATE_MS } from '../../injection-tokens';
import { LoggerModule } from '../../logger';
import { StartupService } from '../../startup.service';
import { VitamUISnackBar } from '../vitamui-snack-bar/vitamui-snack-bar.service';
import { NavbarComponent } from './navbar.component';

@Directive({ selector: '[vitamuiCommonTriggerFor]' })
class TriggerForStubDirective {
  @Input() appTriggerFor: any;
}

@Directive({ selector: '[vitamuiCommonDropdown]', exportAs: 'appDropdown' })
class DropdownStubDirective {
  @Input() appDropdown: any;
}

@Component({
  selector: 'vitamui-common-application-menu',
  template: ''
})
export class ApplicationMenuStubComponent {
  @Input() appId: ApplicationId;
}

@Component({
  selector: 'vitamui-common-tenant-menu',
  template: ''
})
export class TenantMenuStubComponent {
  @Input() appId: ApplicationId;
}

@Component({
  selector: 'vitamui-common-customer-menu',
  template: ''
})
export class CustomerMenuStubComponent {
  @Input() customers: any;
}

@Directive({ selector: '[routerLink]' })
export class RouterLinkStubDirective {
  @Input() routerLink: any;
  navigatedTo: any = null;

  @HostListener('click')
  onClick() {
    this.navigatedTo = this.routerLink;
  }
}


describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;
  const snackBarSpy = jasmine.createSpyObj('VitamUISnackBar', ['open', 'openFromComponent']);

  beforeEach(waitForAsync(() => {
    const authServiceStub = { logout: () => { } };
    const startupServiceStub = { getPortalUrl: () => { }, getLogo: () => { }, getAppLogoURL: () => { } , getCustomerLogoURL: () => { }  };

    TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        NoopAnimationsModule,
        HttpClientTestingModule,
        MatSnackBarModule,
        LoggerModule.forRoot()
      ],
      declarations: [
        DropdownStubDirective,
        NavbarComponent,
        TriggerForStubDirective,
        ApplicationMenuStubComponent,
        TenantMenuStubComponent,
        CustomerMenuStubComponent,
        RouterLinkStubDirective
      ],
      providers: [
        { provide: BASE_URL, useValue: '/fakeapi' },
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: MatSnackBarRef, useValue: { dismiss: () => {} } },
        { provide: AuthService, useValue: authServiceStub },
        { provide: StartupService, useValue: startupServiceStub },
        { provide: ActivatedRoute, useValue: { params: of('11') } },
        { provide: SUBROGRATION_REFRESH_RATE_MS, useValue: 100 },
        { provide: ENVIRONMENT, useValue: environment }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
