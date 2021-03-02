import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {SearchUnitApiService} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {AccessContractNodesTabComponent} from './access-contract-nodes-tab.component';
import {ExternalParameters, ExternalParametersService} from 'ui-frontend-common';

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

  beforeEach(waitForAsync(() => {

    const parameters: Map<string, string> = new Map<string, string>();
    parameters.set(ExternalParameters.PARAM_ACCESS_CONTRACT, '1');
    const externalParametersServiceMock = {
      getUserExternalParameters: () => of(parameters)
    };

    const unitValueMock = {
      getByDsl: () => of({})
    };

    TestBed.configureTestingModule({
      declarations: [AccessContractNodesTabComponent],
      imports: [
        MatSnackBarModule
      ],
      providers: [
        {provide: ExternalParametersService, useValue: externalParametersServiceMock},
        {provide: SearchUnitApiService, useValue: unitValueMock},
        {provide: MatDialog, useValue: {}}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
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
