/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { EMPTY, of } from 'rxjs';
import { BASE_URL, ConfirmDialogService, ExternalParameters, ExternalParametersService, LoggerModule } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { AccessContractService } from '../../access-contract/access-contract.service';
import { ArchiveProfileApiService } from '../../core/api/archive-profile-api.service';
import { ManagementContractApiService } from '../../core/api/management-contract-api.service';
import { FileFormatService } from '../../file-format/file-format.service';
import { IngestContractService } from '../ingest-contract.service';
import { IngestContractCreateComponent } from './ingest-contract-create.component';
import { IngestContractCreateValidators } from './ingest-contract-create.validators';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';

describe('IngestContractCreateComponent', () => {
  let component: IngestContractCreateComponent;
  let fixture: ComponentFixture<IngestContractCreateComponent>;

  beforeEach(waitForAsync(() => {
    const ingestContractCreateValidatorsSpy = jasmine.createSpyObj('IngestContractCreateValidators', {
      uniqueName: () => of(null),
      uniqueIdentifier: () => of(null),
      identifierToIgnore: '',
    });

    const accessContractServiceMock = {
      getAll: () => of([]),
    };

    const parameters: Map<string, string> = new Map<string, string>();
    parameters.set(ExternalParameters.PARAM_ACCESS_CONTRACT, '1');
    const externalParametersServiceMock = {
      getUserExternalParameters: () => of(parameters),
    };

    const fileFormatServiceMock = {
      getAllForTenant: () => of([]),
    };
    const managementContractApiServiceMock = {
      getAllByParams: (_params: any) => of(null),
    };
    const archiveProfileApiServiceMock = {
      getAllByParams: (_params: any) => of(null),
    };

    TestBed.configureTestingModule({
      imports: [
        VitamUICommonTestModule,
        MatSnackBarModule,
        HttpClientTestingModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot(),
        MatButtonToggleModule,
      ],
      declarations: [IngestContractCreateComponent],
      providers: [
        FormBuilder,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: IngestContractService, useValue: {} },
        { provide: IngestContractCreateValidators, useValue: ingestContractCreateValidatorsSpy },
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
        { provide: FileFormatService, useValue: fileFormatServiceMock },
        { provide: ManagementContractApiService, useValue: managementContractApiServiceMock },
        { provide: ArchiveProfileApiService, useValue: archiveProfileApiServiceMock },
        { provide: ExternalParametersService, useValue: externalParametersServiceMock },
        { provide: AccessContractService, useValue: accessContractServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
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
