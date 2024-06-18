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
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { AuthService, Rule, RuleService, SecurityService, WINDOW_LOCATION } from 'vitamui-library';
import { RuleInformationTabComponent } from './rule-information-tab.component';

describe('RuleInformationTabComponent', () => {
  let component: RuleInformationTabComponent;
  let fixture: ComponentFixture<RuleInformationTabComponent>;

  const authServiceMock = {
    getAnyTenantIdentifier: () => '/fake-tenantId',
  };
  const securityServiceMock = {
    hasRole: () => of(true),
  };

  const ruleServiceMock = {
    // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
    patch: (_data: any) => of(true),
  };

  const ruleValue = {
    ruleType: 'AppraisalRule',
    ruleDescription: 'Règle de gestion XXXX',
    ruleDuration: '20',
    ruleMeasurement: 'Day',
    ruleValue: 'RuleValue',
  };

  const previousValue: Rule = {
    id: 'vitam_id',
    tenant: 1,
    version: 1,
    ruleId: 'ruleId',
    ruleType: 'AppraisalRule',
    ruleValue: 'RuleValue',
    ruleDescription: 'Règle de gestion',
    ruleDuration: '10',
    ruleMeasurement: 'Day',
    creationDate: '20/02/2020',
    updateDate: '20/02/2020',
  };

  beforeEach(waitForAsync(() => {
    const activatedRouteMock = {
      params: of({ tenantIdentifier: 1 }),
      data: of({ appId: 'RULES_APP' }),
    };
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      declarations: [RuleInformationTabComponent],
      providers: [
        FormBuilder,
        { provide: ActivatedRoute, useValue: activatedRouteMock },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: AuthService, useValue: authServiceMock },
        { provide: RuleService, useValue: ruleServiceMock },
        { provide: SecurityService, useValue: securityServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RuleInformationTabComponent);
    component = fixture.componentInstance;
    component.form.setValue(ruleValue);
    component.previousValue = (): Rule => previousValue;
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });
});
