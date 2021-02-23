import {NO_ERRORS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {SearchUnitApiService} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';

import {IngestContractAttachmentTabComponent} from './ingest-contract-attachment-tab.component';
import {ExternalParameters, ExternalParametersService} from 'ui-frontend-common';

describe('IngestContractAttachmentTabComponent', () => {
  let component: IngestContractAttachmentTabComponent;
  let fixture: ComponentFixture<IngestContractAttachmentTabComponent>;

  const ingestContractValue = {
    tenant: 0,
    version: 1,
    description: 'desc',
    status: 'ACTIVE',
    id: 'vitam_id',
    name: 'Name',
    identifier: 'SP-000001',
    everyDataObjectVersion: true,
    dataObjectVersion: ['test'],
    creationDate: '01-01-20',
    lastUpdate: '01-01-20',
    activationDate: '01-01-20',
    deactivationDate: '01-01-20',
    checkParentLink: '',
    linkParentId: '',
    checkParentId: [''],
    masterMandatory: true,
    formatUnidentifiedAuthorized: true,
    everyFormatType: true,
    formatType: [''],
    archiveProfiles: [''],
    managementContractId: 'MC-000001'
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
      declarations: [IngestContractAttachmentTabComponent],
      imports: [
        MatSnackBarModule
      ],
      providers: [
        {provide: MatDialog, useValue: {}},
        {provide: SearchUnitApiService, useValue: unitValueMock},
        {provide: ExternalParametersService, useValue: externalParametersServiceMock},
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractAttachmentTabComponent);
    component = fixture.componentInstance;
    component.ingestContract = ingestContractValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
