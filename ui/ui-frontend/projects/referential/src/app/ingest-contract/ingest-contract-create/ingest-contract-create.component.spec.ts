import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {EMPTY, of} from 'rxjs';
import {ConfirmDialogService} from 'ui-frontend-common';
import {AccessContractService} from '../../access-contract/access-contract.service';
import {ArchiveProfileApiService} from '../../core/api/archive-profile-api.service';
import {ManagementContractApiService} from '../../core/api/management-contract-api.service';
import {FileFormatService} from '../../file-format/file-format.service';
import {IngestContractService} from '../ingest-contract.service';
import {IngestContractCreateComponent} from './ingest-contract-create.component';
import {IngestContractCreateValidators} from './ingest-contract-create.validators';

describe('IngestContractCreateComponent', () => {
  let component: IngestContractCreateComponent;
  let fixture: ComponentFixture<IngestContractCreateComponent>;

  beforeEach(async(() => {
    const ingestContractCreateValidatorsSpy = jasmine.createSpyObj('IngestContractCreateValidators', {
      uniqueName: () => of(null)
    });

    const accessContractServiceMock = {
      getAll: () => of([])
    };
    const fileFormatServiceMock = {
      getAllForTenant: () => of([])
    };
    const managementContractApiServiceMock = {
      // tslint:disable-next-line:variable-name
      getAllByParams: (_params: any) => of(null)
    };
    const archiveProfileApiServiceMock = {
      // tslint:disable-next-line:variable-name
      getAllByParams: (_params: any) => of(null)
    };

    TestBed.configureTestingModule({
      declarations: [IngestContractCreateComponent],
      providers: [
        FormBuilder,
        {provide: MatDialogRef, useValue: {}},
        {provide: MAT_DIALOG_DATA, useValue: {}},
        {provide: IngestContractService, useValue: {}},
        {provide: IngestContractCreateValidators, useValue: ingestContractCreateValidatorsSpy},
        {provide: ConfirmDialogService, useValue: {listenToEscapeKeyPress: () => EMPTY}},
        {provide: FileFormatService, useValue: fileFormatServiceMock},
        {provide: ManagementContractApiService, useValue: managementContractApiServiceMock},
        {provide: ArchiveProfileApiService, useValue: archiveProfileApiServiceMock},
        {provide: AccessContractService, useValue: accessContractServiceMock}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
