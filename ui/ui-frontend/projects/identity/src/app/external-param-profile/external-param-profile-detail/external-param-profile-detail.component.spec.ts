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
import { Component, Input, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { MatLegacyTabsModule as MatTabsModule } from '@angular/material/legacy-tabs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of, Subject } from 'rxjs';
import { AuthService, BASE_URL, LoggerModule, WINDOW_LOCATION } from 'vitamui-library';
import { environment } from '../../../environments/environment.prod';
import { TestHostComponent } from '../../shared/domains-input/domains-input.component.spec';
import { ExternalParamProfileService } from '../external-param-profile.service';
import { ExternalParamProfileDetailComponent } from './external-param-profile-detail.component';

@Component({ selector: 'app-information-tab', template: '' })
class InformationTabStubComponent {
  // @Input() profile: ExternalParamProfile;
  @Input() readOnly: boolean;
  @Input() tenantIdentifier: string;
}

const translations: any = { TEST: 'Mock translate test' };

class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('ExternalParamProfilDetailComponent', () => {
  let component: ExternalParamProfileDetailComponent;
  let fixture: ComponentFixture<ExternalParamProfileDetailComponent>;

  const authServiceMock = { user: { level: '' } };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
        MatMenuModule,
        MatTabsModule,
        NoopAnimationsModule,
        HttpClientTestingModule,
        LoggerModule.forRoot(),
      ],
      declarations: [TestHostComponent, ExternalParamProfileDetailComponent, InformationTabStubComponent],
      providers: [
        { provide: ExternalParamProfileService, useValue: { updated: new Subject() } },
        { provide: AuthService, useValue: authServiceMock },
        { provide: WINDOW_LOCATION, useValue: {} },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: environment, useValue: environment },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalParamProfileDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
