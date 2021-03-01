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

import { Component, Directive, Input, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of ,  Subject } from 'rxjs';

import { Customer, IdentityProvider, OtpState } from 'ui-frontend-common';
import { IdentityProviderService } from './identity-provider.service';
import { SsoTabComponent } from './sso-tab.component';

@Component({ selector: 'app-identity-provider-details', template: '' })
class IdentityProviderDetailsStubComponent {
    @Input() identityProvider: IdentityProvider;
    @Input() domains: any;
    @Input() readOnly: boolean;
}

@Directive({ selector: '[matTooltip]' })
class MatTooltipStubDirective {
  @Input() matTooltip: any;
  @Input() matTooltipDisabled: any;
  @Input() matTooltipClass: any;
}

@Component({
  template: `<app-sso-tab [customer]='customer'></app-sso-tab>`
})
class TestHostComponent {
  customer: Customer = {
    id : '5ad5f14c894e6a414edc7b5ffd02766b442f4256b563a6e3909e05b9e3abf9ea',
    identifier : '1',
    code : '015000',
    name : 'TeamVitamUI',
    companyName : 'vitamui',
    enabled : false,
    readonly: false,
    hasCustomGraphicIdentity: false,
    language : 'FRENCH',
    passwordRevocationDelay : 9,
    otp : OtpState.OPTIONAL,
    emailDomains : [
        'vitamui.com',
        '1test.com',
        'test2.com',
        'test3.com',
        'test4.com',
        'test5.com',
        'test6.com',
    ],
    defaultEmailDomain : '1test.com',
    address : {
        street : '73 rue du Faubourg Poissonnière ',
        zipCode : '75009',
        city : 'Paris',
        country : 'DK'
    },
    owners: [],
    themeColors: {},
    gdprAlert : false,
    gdprAlertDelay : 72
  };
  @ViewChild(SsoTabComponent, { static: false }) component: SsoTabComponent;
}

describe('SsoTabComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;
  let providers: any[];

  beforeEach(waitForAsync(() => {
    providers = [
      {
        id: '5ad5f14c894e6a414edc7b60c5397d744f4b4ed8bd86934d0a8e8311add40f3f',
        customerId: '5ad5f14c894e6a414edc7b5ffd02766b442f4256b563a6e3909e05b9e3abf9ea',
        name: 'default',
        internal: true,
        enabled: true,
        patterns: null,
        keystoreBase64: null,
        keystorePassword: null,
        privateKeyPassword: null,
        idpMetadata: null,
        spMetadata: null
      },
      {
        id: '5ad5f14e894e6a414edc7b91dc194c3187f143cbb7593242769a1706fd03d3f3',
        customerId: '5ad5f14c894e6a414edc7b5ffd02766b442f4256b563a6e3909e05b9e3abf9ea',
        name: 'TeamVitamUI',
        internal: true,
        enabled: true,
        patterns: null,
        keystoreBase64: null,
        keystorePassword: null,
        privateKeyPassword: null,
        idpMetadata: null,
        spMetadata: null
      },
      {
        id: '5af95fae636f9114e074100590122991ac38418595cd0dab684fee3bccacd2dd',
        customerId: '5ad5f14c894e6a414edc7b5ffd02766b442f4256b563a6e3909e05b9e3abf9ea',
        name: 'test',
        internal: false,
        enabled: true,
        patterns: ['vitamui.com'],
        keystoreBase64: null,
        keystorePassword: 'test',
        privateKeyPassword: 'test',
        idpMetadata: null,
        spMetadata: null
      },
    ];

    const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

    TestBed.configureTestingModule({
      declarations: [
        SsoTabComponent,
        IdentityProviderDetailsStubComponent,
        TestHostComponent,
        MatTooltipStubDirective,
      ],
      providers: [
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: IdentityProviderService, useValue: {
          getAll: () => of(providers),
          getDomainByCustomerId: () => of(['test1.com', 'test2.com']),
          updated: new Subject()
        } },
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  it('should set the providers', () => {
    expect(testhost.component.providers).toEqual(providers);
  });

  it('should call open', () => {
    const matDialogSpy = TestBed.inject(MatDialog);
    testhost.component.openCreateIDPDialog();
    expect(matDialogSpy.open).toHaveBeenCalled();
  });

  describe('DOM', () => {

    describe('Button "Create IDP"', () => {

      it('should exist', () => {
        const elButton = fixture.nativeElement.querySelector('button');
        expect(elButton).toBeTruthy();
        expect(elButton.textContent).toContain('Créer un IDP');
      });

      it('should call openCreateIDPDialog()', () => {
        spyOn(testhost.component, 'openCreateIDPDialog').and.stub();
        const elButton = fixture.nativeElement.querySelector('button');
        elButton.click();
        expect(testhost.component.openCreateIDPDialog).toHaveBeenCalled();
      });

      it('should be disabled', () => {
        spyOn(testhost.component, 'openCreateIDPDialog').and.stub();
        testhost.component.domains = [];
        fixture.detectChanges();
        const elButton = fixture.nativeElement.querySelector('button');
        expect(elButton.disabled).toBeTruthy();
        elButton.click();
        expect(testhost.component.openCreateIDPDialog).not.toHaveBeenCalled();
      });

      it('should not show up in readonly mode', () => {
        testhost.component.readOnly = true;
        fixture.detectChanges();
        const elButton = fixture.nativeElement.querySelector('button');
        expect(elButton).toBeFalsy();
      });

    });

    describe('Providers List', () => {

      it('should display the list of providers', () => {
        const elProviders = fixture.nativeElement.querySelectorAll('.provider-item-content');
        expect(elProviders.length).toBe(3);
        elProviders.forEach((elProvider: HTMLElement, index: number) => {
          expect(elProvider.textContent).toContain(providers[index].name);
          expect(elProvider.textContent).toContain(providers[index].internal ? 'Interne' : 'Externe');
          expect(elProvider.textContent).toContain(providers[index].enabled ? 'Actif' : 'Inactif');
        });
      });

      it('should select the provider on click', () => {
        const elProviders = fixture.nativeElement.querySelectorAll('.provider-item-content');
        elProviders[0].click();
        fixture.detectChanges();
        expect(testhost.component.selectedIdentityProvider).toBe(testhost.component.providers[0]);
      });
    });

    describe('Provider Details', () => {

      it('should not show if no provider is selected', () => {
        const elProviderDetails = fixture.nativeElement.querySelector('app-identity-provider-details');
        expect(elProviderDetails).toBeFalsy();
      });

      it('should show when a provider is selected', () => {
        testhost.component.selectedIdentityProvider = providers[0];
        fixture.detectChanges();
        const elProviderDetails = fixture.nativeElement.querySelector('app-identity-provider-details');
        expect(elProviderDetails).toBeTruthy();
      });

      it('should have a "back" button', () => {
        testhost.component.selectedIdentityProvider = providers[0];
        fixture.detectChanges();

        const elButton = fixture.nativeElement.querySelector('button');
        expect(elButton).toBeTruthy();
        expect(elButton.textContent).toContain('Retourner à la liste des IDP');
        elButton.click();
        expect(testhost.component.selectedIdentityProvider).toBeFalsy();
      });

    });

  });
});
