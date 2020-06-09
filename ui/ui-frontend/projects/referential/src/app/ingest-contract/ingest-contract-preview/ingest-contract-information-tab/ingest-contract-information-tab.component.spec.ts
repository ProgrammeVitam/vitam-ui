import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestContractInformationTabComponent } from './ingest-contract-information-tab.component';
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { IngestContractService } from '../../ingest-contract.service';
import { ManagementContractApiService } from '../../../core/api/management-contract-api.service';
import { ArchiveProfileApiService } from '../../../core/api/archive-profile-api.service';
import { IngestContract } from 'projects/vitamui-library/src/public-api';
import { IngestContractCreateValidators } from '../../ingest-contract-create/ingest-contract-create.validators';
import { of } from 'rxjs';
import { VitamUIInputModule } from 'projects/vitamui-library/src/lib/components/vitamui-input/vitamui-input.module';
import { MatSelectModule } from '@angular/material';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';


describe('IngestContractInformationTabComponent', () => {
  let component: IngestContractInformationTabComponent;
  let fixture: ComponentFixture<IngestContractInformationTabComponent>;

  const ingestContractValue = {
    identifier: 'identifier',
    status: 'ACTIVE',
    name: 'name',
    description: 'descripton',
    archiveProfiles: [new Array<string>()],
    managementContractId: 'MC-000001'
  }

  const previousValue:IngestContract = {
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
    archiveProfiles : [], 
    managementContractId: 'MC-000001'
  };


  beforeEach(async(() => {

    const ingestContractServiceMock = { 
      create: of({}), 
      getAll: of([]),
      patch: (_data: any) => of(null)
    };
    const managementContractApiServiceMock = { 
      getAllByParams: (_params: any) => of(null) 
    };
    const archiveProfileApiServiceMock = { 
      getAllByParams: (_params: any) => of(null) 
    };
    const ingestContractCreateValidatorsMock = { 
      uniqueName: () => () => of({}), 
      uniqueNameWhileEdit: () => () => of({})
    };
    

    TestBed.configureTestingModule({
      imports: 
      [ 
        ReactiveFormsModule,
        VitamUIInputModule,
        VitamUICommonTestModule,
        MatSelectModule,
        NoopAnimationsModule
      ],
      declarations: [ IngestContractInformationTabComponent ],
      providers: [
        FormBuilder,
        { provide: IngestContractService, useValue: ingestContractServiceMock },
        { provide: ManagementContractApiService, useValue: managementContractApiServiceMock },
        { provide: ArchiveProfileApiService, useValue: archiveProfileApiServiceMock },
        { provide: IngestContractCreateValidators, useValue: ingestContractCreateValidatorsMock }
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractInformationTabComponent);
    component = fixture.componentInstance;
    component.form.setValue(ingestContractValue);
    component.previousValue = (): IngestContract => previousValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
