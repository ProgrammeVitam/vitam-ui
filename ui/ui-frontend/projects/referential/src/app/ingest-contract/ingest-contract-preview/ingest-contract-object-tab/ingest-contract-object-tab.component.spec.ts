import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { IngestContract } from 'projects/vitamui-library/src/public-api';
import { IngestContractService } from '../../ingest-contract.service';
import { IngestContractObjectTabComponent } from './ingest-contract-object-tab.component';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';

describe('IngestContractObjectTabComponent', () => {
  let component: IngestContractObjectTabComponent;
  let fixture: ComponentFixture<IngestContractObjectTabComponent>;

  const ingestContractValue: IngestContract = {
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
    archiveProfiles: [],
    managementContractId: 'MC-000001',
    computeInheritedRulesAtIngest: false,
  };

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [ReactiveFormsModule, VitamUICommonTestModule],
        declarations: [IngestContractObjectTabComponent],
        providers: [FormBuilder, { provide: IngestContractService, useValue: {} }],
        schemas: [NO_ERRORS_SCHEMA],
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractObjectTabComponent);
    component = fixture.componentInstance;
    component.ingestContract = ingestContractValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
