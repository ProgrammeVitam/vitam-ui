/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { Component, inject, OnChanges, signal, input, output, SimpleChanges } from '@angular/core';
import { disabled, form, FormField, FormRoot } from '@angular/forms/signals';
import { EMPTY, Observable } from 'rxjs';
import { IngestContract, SignaturePolicy, SignedDocumentPolicyEnum } from 'vitamui-library';
import { IngestContractService } from '../../ingest-contract.service';
import { MatButtonToggle, MatButtonToggleGroup } from '@angular/material/button-toggle';
import { MatCheckbox } from '@angular/material/checkbox';
import { TranslatePipe } from '@ngx-translate/core';

const SIGNATURE_POLICY_KEYS: (keyof SignaturePolicy)[] = [
  'signedDocument',
  'declaredSignature',
  'declaredTimestamp',
  'declaredAdditionalProof',
];

@Component({
  selector: 'app-ingest-contract-signature-tab',
  templateUrl: './ingest-contract-signature-tab.component.html',
  styleUrls: ['./ingest-contract-signature-tab.component.scss'],
  imports: [FormRoot, FormField, MatButtonToggleGroup, MatButtonToggle, MatCheckbox, TranslatePipe],
})
export class IngestContractSignatureTabComponent implements OnChanges {
  private readonly ingestContractService = inject(IngestContractService);

  readonly SignedDocumentPolicyEnum = SignedDocumentPolicyEnum;

  readonly updated = output<boolean>();

  readonly ingestContract = input<IngestContract>(undefined);

  readonly readOnly = input<boolean>(undefined);

  readonly form = form(
    signal<SignaturePolicy>({
      signedDocument: SignedDocumentPolicyEnum.ALLOWED,
      declaredSignature: false,
      declaredTimestamp: false,
      declaredAdditionalProof: false,
    }),
    (path) => {
      disabled(path.signedDocument, () => !!this.readOnly());
      for (const checkbox of [path.declaredSignature, path.declaredTimestamp, path.declaredAdditionalProof]) {
        disabled(checkbox, ({ valueOf }) => !!this.readOnly() || valueOf(path.signedDocument) === SignedDocumentPolicyEnum.FORBIDDEN);
      }
    },
    {
      submission: {
        action: async () => {
          this.onSubmit();
        },
        ignoreValidators: 'all',
      },
    },
  );
  submitting = signal(false);
  private _ingestContract: IngestContract;

  ngOnChanges(changes: SimpleChanges) {
    if (changes['ingestContract']) {
      const ingestContract = this.ingestContract();
      this._ingestContract = ingestContract;
      this.resetForm(ingestContract);
      this.updated.emit(false);
    }
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

  /**
   * Value of the form, without the disabled fields (unless they are all disabled) and the unset fields,
   * as the value of the former reactive form.
   */
  private formValue(): SignaturePolicy {
    const allDisabled = SIGNATURE_POLICY_KEYS.every((key) => this.form[key]().disabled());
    return SIGNATURE_POLICY_KEYS.reduce((acc: any, key) => {
      const value = this.form[key]().value();
      if ((allDisabled || !this.form[key]().disabled()) && value != null) {
        acc[key] = value;
      }
      return acc;
    }, {}) as SignaturePolicy;
  }

  signaturePolicyUnchanged() {
    const actual = this.formValue();
    const previous = this.previousValue();
    // Unset values were omitted by JSON.stringify in the former reactive form value
    const definedPrevious = previous
      ? SIGNATURE_POLICY_KEYS.reduce((acc: any, key) => {
          if (previous[key] != null) {
            acc[key] = previous[key];
          }
          return acc;
        }, {})
      : previous;
    const unchanged = JSON.stringify(actual) === JSON.stringify(definedPrevious);
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<IngestContract> {
    if (this.signaturePolicyUnchanged()) {
      return EMPTY;
    }
    const signaturePolicy = this.formValue();
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
    this.submitting.set(true);
    this.prepareSubmit().subscribe(
      (ingestContract: IngestContract) => {
        this._ingestContract.signaturePolicy = ingestContract.signaturePolicy;
        this.submitting.set(false);
      },
      (error) => {
        this.submitting.set(false);
        console.error(error);
      },
    );
  }

  changeSignedDocumentPolicy(signedDocumentPolicyEnum: SignedDocumentPolicyEnum): void {
    // The checkboxes are disabled by the form schema when the signed documents are forbidden
    if (signedDocumentPolicyEnum === SignedDocumentPolicyEnum.FORBIDDEN) {
      this.form().value.set({
        signedDocument: SignedDocumentPolicyEnum.FORBIDDEN,
        declaredSignature: false,
        declaredTimestamp: false,
        declaredAdditionalProof: false,
      });
    }
  }

  resetForm(ingestContract: IngestContract) {
    const signaturePolicy = ingestContract.signaturePolicy ? this._ingestContract.signaturePolicy : undefined;
    this.form().reset({
      signedDocument: signaturePolicy ? (signaturePolicy.signedDocument ?? null) : SignedDocumentPolicyEnum.ALLOWED,
      declaredSignature: signaturePolicy ? (signaturePolicy.declaredSignature ?? null) : false,
      declaredTimestamp: signaturePolicy ? (signaturePolicy.declaredTimestamp ?? null) : false,
      declaredAdditionalProof: signaturePolicy ? (signaturePolicy.declaredAdditionalProof ?? null) : false,
    });
  }
}
