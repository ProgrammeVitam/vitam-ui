import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {MatSelectModule} from '@angular/material/select';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {VitamUIInputModule} from 'projects/vitamui-library/src/lib/components/vitamui-input/vitamui-input.module';
import {IngestContract} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {ArchiveProfileApiService} from '../../../core/api/archive-profile-api.service';
import {ManagementContractApiService} from '../../../core/api/management-contract-api.service';
import {IngestContractCreateValidators} from '../../ingest-contract-create/ingest-contract-create.validators';
import {IngestContractService} from '../../ingest-contract.service';
import {IngestContractInformationTabComponent} from './ingest-contract-information-tab.component';


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
  };

  const previousValue: IngestContract = {
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
    managementContractId: 'MC-000001'
  };


  beforeEach(waitForAsync(() => {

    const ingestContractServiceMock = {
      create: of({}),
      getAll: of([]),
      // tslint:disable-next-line:variable-name
      patch: (_data: any) => of(null)
    };
    const managementContractApiServiceMock = {
      // tslint:disable-next-line:variable-name
      getAllByParams: (_params: any) => of(null)
    };
    const archiveProfileApiServiceMock = {
      // tslint:disable-next-line:variable-name
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
      declarations: [IngestContractInformationTabComponent],
      providers: [
        FormBuilder,
        {provide: IngestContractService, useValue: ingestContractServiceMock},
        {provide: ManagementContractApiService, useValue: managementContractApiServiceMock},
        {provide: ArchiveProfileApiService, useValue: archiveProfileApiServiceMock},
        {provide: IngestContractCreateValidators, useValue: ingestContractCreateValidatorsMock}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
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
