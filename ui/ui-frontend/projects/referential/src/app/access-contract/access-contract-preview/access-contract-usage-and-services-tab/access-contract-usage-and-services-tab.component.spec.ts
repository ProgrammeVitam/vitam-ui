import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {AccessContract} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';
import {AgencyService} from '../../../agency/agency.service';
import {AccessContractService} from '../../access-contract.service';
import {AccessContractUsageAndServicesTabComponent} from './access-contract-usage-and-services-tab.component';


describe('AccessContractUsageAndServicesTabComponent', () => {
  let component: AccessContractUsageAndServicesTabComponent;
  let fixture: ComponentFixture<AccessContractUsageAndServicesTabComponent>;


  const accessContractValue = {
    everyOriginatingAgency: true,
    originatingAgencies: ['test'],
    everyDataObjectVersion: true,
    dataObjectVersion: ['test']
  };

  const previousValue: AccessContract = {
    tenant: 0,
    version: 1,
    description: 'desc',
    status: 'ACTIVE',
    id: 'vitam_id',
    name: 'Name',
    identifier: 'SP-000001',
    everyOriginatingAgency: true,
    originatingAgencies: ['test'],
    everyDataObjectVersion: true,
    dataObjectVersion: ['test'],
    creationDate: '01-01-20',
    lastUpdate: '01-01-20',
    activationDate: '01-01-20',
    deactivationDate: '01-01-20',
    writingPermission: true,
    writingRestrictedDesc: true,
    accessLog: '',
    ruleFilter: true,
    ruleCategoryToFilter: ['rule'],
    rootUnits: [],
    excludedRootUnits: []
  };

  const agencyServiceMock = {
    getAll: () => of([])
  };
  const accessContractServiceMock = {
    // tslint:disable-next-line:variable-name
    patch: (_data: any) => of(null)
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports:
        [
          ReactiveFormsModule,
          VitamUICommonTestModule
        ],
      declarations: [AccessContractUsageAndServicesTabComponent],
      providers: [
        FormBuilder,
        {provide: AccessContractService, useValue: accessContractServiceMock},
        {provide: AgencyService, useValue: agencyServiceMock}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractUsageAndServicesTabComponent);
    component = fixture.componentInstance;
    component.form.setValue(accessContractValue);
    component.previousValue = (): AccessContract => previousValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
