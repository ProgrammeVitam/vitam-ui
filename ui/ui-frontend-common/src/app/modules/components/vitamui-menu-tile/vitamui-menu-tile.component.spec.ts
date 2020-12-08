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
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Directive, Input, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ApplicationId } from '../../application-id.enum';
import { BASE_URL } from '../../injection-tokens';
import { Application } from '../../models/application';
import { SUBROGRATION_REFRESH_RATE_MS } from './../../injection-tokens';
import { LoggerModule } from './../../logger/logger.module';
import { StartupService } from './../../startup.service';
import { VitamUIMenuTileComponent } from './vitamui-menu-tile.component';

@Directive({
  selector: '[vitamuiCommonTooltip]',
})
class TooltipStubDirective {
  @Input() vitamuiCommonTooltip: any;
}

describe('VitamUIMenuTileComponent', () => {
  let component: VitamUIMenuTileComponent;
  let fixture: ComponentFixture<VitamUIMenuTileComponent>;

  beforeEach(waitForAsync(() => {
    const startupServiceStub = { getPortalUrl: () => 'https://dev.vitamui.com',
    getConfigStringValue: () => 'https://dev.vitamui.com/identity' };
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        LoggerModule.forRoot()
      ],
      declarations: [
        VitamUIMenuTileComponent,
        TooltipStubDirective,
      ],
      providers: [
        { provide: StartupService, useValue: startupServiceStub },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: SUBROGRATION_REFRESH_RATE_MS, useValue: 100 },
      ],
      schemas: [
        NO_ERRORS_SCHEMA,
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VitamUIMenuTileComponent);
    component = fixture.debugElement.componentInstance;
    const appInfo: Application = {
      id: 'AccountsId',
      identifier: ApplicationId.ACCOUNTS_APP,
      url: 'https://dev.vitamui.com/identity/fake-url',
      icon: 'vitamui-icon vitamui-icon-user',
      name: 'Mon compte',
      tooltip: 'Tooltip de Mon Compte',
      category: 'users',
      position: 7,
      hasCustomerList: false,
      hasHighlight: false,
      hasTenantList: false,
      target: 'self'
    };
    component.application = appInfo;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should compute URL', () => {
    expect(component.url).toBe('/fake-url');
  });

  it('should be same App URL', () => {
    expect(component.sameApp).toBe(true);
  });
});
