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
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { of } from 'rxjs';
import { SignedDocumentPolicyEnum } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { IngestContractService } from '../../ingest-contract.service';
import { IngestContractSignatureTabComponent } from './ingest-contract-signature-tab.component';

describe('IngestContractSignatureTabComponent', () => {
  let component: IngestContractSignatureTabComponent;
  let fixture: ComponentFixture<IngestContractSignatureTabComponent>;
  const ingestContractServiceSpy = jasmine.createSpyObj('IngestContractService', { patch: of() });

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, VitamUICommonTestModule, MatButtonToggleModule],
      declarations: [IngestContractSignatureTabComponent],
      providers: [FormBuilder, { provide: IngestContractService, useValue: ingestContractServiceSpy }],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractSignatureTabComponent);
    component = fixture.componentInstance;
    component.ingestContract = {
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
      signaturePolicy: {
        signedDocument: SignedDocumentPolicyEnum.MANDATORY,
        declaredSignature: true,
        declaredTimestamp: true,
        declaredAdditionalProof: true,
      },
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should provide `undefined` value for declared* attributes when signedDocument is FORBIDDEN', () => {
    component.form.patchValue({
      signedDocument: SignedDocumentPolicyEnum.FORBIDDEN,
      declaredSignature: true,
      declaredTimestamp: true,
      declaredAdditionalProof: true,
    });

    component.onSubmit();

    expect(ingestContractServiceSpy.patch).toHaveBeenCalledWith(
      jasmine.objectContaining({
        signaturePolicy: {
          signedDocument: SignedDocumentPolicyEnum.FORBIDDEN,
          declaredSignature: undefined,
          declaredTimestamp: undefined,
          declaredAdditionalProof: undefined,
        },
      }),
    );
  });

  it('should provide `false` value for declared* attributes when signedDocument is not FORBIDDEN and declared* values are unset', () => {
    component.form.patchValue({
      signedDocument: SignedDocumentPolicyEnum.ALLOWED,
      declaredSignature: undefined,
      declaredTimestamp: undefined,
      declaredAdditionalProof: undefined,
    });

    component.onSubmit();

    expect(ingestContractServiceSpy.patch).toHaveBeenCalledWith(
      jasmine.objectContaining({
        signaturePolicy: {
          signedDocument: SignedDocumentPolicyEnum.ALLOWED,
          declaredSignature: false,
          declaredTimestamp: false,
          declaredAdditionalProof: false,
        },
      }),
    );
  });
});
