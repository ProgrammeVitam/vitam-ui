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
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { VitamUIInputModule } from 'projects/vitamui-library/src/lib/components/vitamui-input/vitamui-input.module';
import { of } from 'rxjs';
import { IngestContract } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { ArchiveProfileApiService } from '../../../core/api/archive-profile-api.service';
import { ManagementContractApiService } from '../../../core/api/management-contract-api.service';
import { IngestContractCreateValidators } from '../../ingest-contract-create/ingest-contract-create.validators';
import { IngestContractService } from '../../ingest-contract.service';
import { IngestContractInformationTabComponent } from './ingest-contract-information-tab.component';

describe('IngestContractInformationTabComponent', () => {
  let component: IngestContractInformationTabComponent;
  let fixture: ComponentFixture<IngestContractInformationTabComponent>;

  const ingestContractValue = {
    identifier: 'identifier',
    status: 'ACTIVE',
    name: 'name',
    description: 'descripton',
    archiveProfiles: [new Array<string>()],
    managementContractId: 'MC-000001',
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
    managementContractId: 'MC-000001',
    computeInheritedRulesAtIngest: false,
    signaturePolicy: undefined,
  };

  beforeEach(waitForAsync(() => {
    const ingestContractServiceMock = {
      create: of({}),
      getAll: of([]),
      // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
      patch: (_data: any) => of(null),
    };
    const managementContractApiServiceMock = {
      getAllByParams: (_params: any) => of(null),
    };
    const archiveProfileApiServiceMock = {
      getAllByParams: (_params: any) => of(null),
    };
    const ingestContractCreateValidatorsMock = {
      uniqueName: () => () => of({}),
      uniqueNameWhileEdit: () => () => of({}),
    };

    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, VitamUIInputModule, VitamUICommonTestModule, MatSelectModule, NoopAnimationsModule],
      declarations: [IngestContractInformationTabComponent],
      providers: [
        FormBuilder,
        { provide: IngestContractService, useValue: ingestContractServiceMock },
        { provide: ManagementContractApiService, useValue: managementContractApiServiceMock },
        { provide: ArchiveProfileApiService, useValue: archiveProfileApiServiceMock },
        { provide: IngestContractCreateValidators, useValue: ingestContractCreateValidatorsMock },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
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
