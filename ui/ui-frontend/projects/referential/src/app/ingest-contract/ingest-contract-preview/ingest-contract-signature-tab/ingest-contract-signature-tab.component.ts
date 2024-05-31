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
import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { EMPTY, Observable } from 'rxjs';
import { IngestContract, SignaturePolicy, SignedDocumentPolicyEnum } from 'vitamui-library';
import { IngestContractService } from '../../ingest-contract.service';

@Component({
  selector: 'app-ingest-contract-signature-tab',
  templateUrl: './ingest-contract-signature-tab.component.html',
  styleUrls: ['./ingest-contract-signature-tab.component.scss'],
})
export class IngestContractSignatureTabComponent implements OnChanges {
  readonly SignedDocumentPolicyEnum = SignedDocumentPolicyEnum;

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;
  submitting = false;
  private _ingestContract: IngestContract;

  @Input()
  set ingestContract(ingestContract: IngestContract) {
    this._ingestContract = ingestContract;
    this.resetForm(ingestContract);
    this.updated.emit(false);
  }

  @Input()
  readOnly: boolean;

  constructor(
    private formBuilder: FormBuilder,
    private ingestContractService: IngestContractService,
  ) {}

  ngOnChanges() {
    const defaultSignaturePolicy: SignaturePolicy = {
      signedDocument: SignedDocumentPolicyEnum.ALLOWED,
      declaredSignature: false,
      declaredTimestamp: false,
      declaredAdditionalProof: false,
    };
    if (!this._ingestContract?.signaturePolicy) {
      this._ingestContract.signaturePolicy = defaultSignaturePolicy;
    }
  }

  previousValue = (): SignaturePolicy => {
    return this._ingestContract.signaturePolicy;
  };

  signaturePolicyUnchanged() {
    const actual = this.form.value as SignaturePolicy;
    const previous = this.previousValue();
    const unchanged = JSON.stringify(actual) === JSON.stringify(previous);
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<IngestContract> {
    if (this.signaturePolicyUnchanged()) {
      return EMPTY;
    }
    const signaturePolicy = this.form.value as SignaturePolicy;
    const isForbidden = signaturePolicy.signedDocument === SignedDocumentPolicyEnum.FORBIDDEN;
    const formData = {
      id: this._ingestContract.id,
      identifier: this._ingestContract.identifier,
      signaturePolicy: {
        signedDocument: signaturePolicy.signedDocument,
        declaredSignature: isForbidden ? undefined : !!signaturePolicy.declaredSignature,
        declaredTimestamp: isForbidden ? undefined : !!signaturePolicy.declaredTimestamp,
        declaredAdditionalProof: isForbidden ? undefined : !!signaturePolicy.declaredAdditionalProof,
      },
    };
    return this.ingestContractService.patch(formData);
  }

  onSubmit() {
    this.submitting = true;
    this.prepareSubmit().subscribe(
      (ingestContract: IngestContract) => {
        this._ingestContract.signaturePolicy = ingestContract.signaturePolicy;
        this.submitting = false;
      },
      (error) => {
        this.submitting = false;
        console.error(error);
      },
    );
  }

  changeSignedDocumentPolicy(signedDocumentPolicyEnum: SignedDocumentPolicyEnum): void {
    if (signedDocumentPolicyEnum === SignedDocumentPolicyEnum.FORBIDDEN) {
      this.form.setValue({
        signedDocument: SignedDocumentPolicyEnum.FORBIDDEN,
        declaredSignature: false,
        declaredTimestamp: false,
        declaredAdditionalProof: false,
      });
    }
  }

  signedDocumentPolicyIsDisabled(): boolean {
    if (this.form.value.signedDocument === SignedDocumentPolicyEnum.FORBIDDEN) {
      return true;
    }
    return null;
  }

  resetForm(ingestContract: IngestContract) {
    if (ingestContract.signaturePolicy) {
      this.form = this.formBuilder.group({
        signedDocument: [this._ingestContract.signaturePolicy.signedDocument],
        declaredSignature: [this._ingestContract.signaturePolicy.declaredSignature],
        declaredTimestamp: [this._ingestContract.signaturePolicy.declaredTimestamp],
        declaredAdditionalProof: [this._ingestContract.signaturePolicy.declaredAdditionalProof],
      });
    } else {
      this.form = this.formBuilder.group({
        signedDocument: [SignedDocumentPolicyEnum.ALLOWED],
        declaredSignature: [false],
        declaredTimestamp: [false],
        declaredAdditionalProof: [false],
      });
    }
  }
}
