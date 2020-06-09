import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessContractNodesTabComponent } from './access-contract-nodes-tab.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { MatDialog } from '@angular/material';
import { AccessContractService } from '../../access-contract.service';
import { SearchUnitApiService } from 'projects/vitamui-library/src/public-api';
import { of } from 'rxjs';

describe('AccessContractNodesTabComponent', () => {
  let component: AccessContractNodesTabComponent;
  let fixture: ComponentFixture<AccessContractNodesTabComponent>;

  const accessContractValue = {
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
    rootUnits: [''],
    excludedRootUnits: ['']
  };

  beforeEach(async(() => {

    const accessContractServiceMock = {
      getAll: ()=> of([])
    };

    TestBed.configureTestingModule({
      declarations: [ AccessContractNodesTabComponent ],
      providers: [
        { provide: MatDialog, useValue: { } },
        { provide: AccessContractService, useValue: accessContractServiceMock },
        { provide: SearchUnitApiService, useValue: { } }
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractNodesTabComponent);
    component = fixture.componentInstance;
    component.accessContract = accessContractValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
