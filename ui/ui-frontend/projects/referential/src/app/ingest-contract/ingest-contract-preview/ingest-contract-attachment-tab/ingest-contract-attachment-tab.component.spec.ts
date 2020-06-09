import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestContractAttachmentTabComponent } from './ingest-contract-attachment-tab.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";
import { MatDialog } from '@angular/material';
import { AccessContractService } from '../../../access-contract/access-contract.service';
import { SearchUnitApiService } from 'projects/vitamui-library/src/public-api';
import { of } from 'rxjs';

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
    masterMandatory : true,
    formatUnidentifiedAuthorized : true, 
    everyFormatType: true, 
    formatType: [''], 
    archiveProfiles : [''], 
    managementContractId: 'MC-000001'
  }

  beforeEach(async(() => {

    const accessContractServiceMock = {
      getAll: ()=> of([])
    };

    TestBed.configureTestingModule({
      declarations: [ IngestContractAttachmentTabComponent ],
      providers:[
        { provide: MatDialog, useValue: { } },
        { provide: AccessContractService, useValue: accessContractServiceMock },
        { provide: SearchUnitApiService, useValue: { } }
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
