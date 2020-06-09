import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestContractFormatTabComponent } from './ingest-contract-format-tab.component';
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { IngestContractService } from '../../ingest-contract.service';
import { FileFormatService } from '../../../file-format/file-format.service';
import { of } from 'rxjs';

describe('IngestContractFormatTabComponent', () => {
  let component: IngestContractFormatTabComponent;
  let fixture: ComponentFixture<IngestContractFormatTabComponent>;

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
    const fileFormatServiceMock = {
      getAllForTenant: ()=> of([])
    };
    TestBed.configureTestingModule({
      imports:
        [
          ReactiveFormsModule,
          VitamUICommonTestModule
        ],
      declarations: [ IngestContractFormatTabComponent ],
      providers:[
        { provide:IngestContractService, useValue:{} },
        { provide:FileFormatService, useValue:fileFormatServiceMock }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractFormatTabComponent);
    component = fixture.componentInstance;
    component.ingestContract = ingestContractValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
