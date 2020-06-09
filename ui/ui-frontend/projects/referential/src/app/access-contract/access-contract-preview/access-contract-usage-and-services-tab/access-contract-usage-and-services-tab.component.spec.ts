import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessContractUsageAndServicesTabComponent } from './access-contract-usage-and-services-tab.component';
import { FormBuilder } from '@angular/forms';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { AccessContractService } from '../../access-contract.service';
import { AgencyService } from '../../../agency/agency.service';
import { of } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';
import { AccessContract } from 'projects/vitamui-library/src/public-api';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';


describe('AccessContractUsageAndServicesTabComponent', () => {
  let component: AccessContractUsageAndServicesTabComponent;
  let fixture: ComponentFixture<AccessContractUsageAndServicesTabComponent>;


  const accessContractValue = {
    everyOriginatingAgency: true,
    originatingAgencies: ['test'],
    everyDataObjectVersion: true,
    dataObjectVersion: ['test']
  };

  const previousValue:AccessContract = {
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
    patch: (_data: any) => of(null)
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: 
      [ 
        ReactiveFormsModule,
        VitamUICommonTestModule
      ],
      declarations: [ AccessContractUsageAndServicesTabComponent ],
      providers: [
        FormBuilder,
        { provide: AccessContractService, useValue: accessContractServiceMock },
        { provide: AgencyService, useValue: agencyServiceMock }
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
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
