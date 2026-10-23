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
import { HttpHeaders, HttpParams } from '@angular/common/http';
import { Component, effect, inject, OnDestroy, OnInit, signal, untracked } from '@angular/core';
import { disabled, form, FormField, FormRoot, maxLength, minLength, required } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import {
  ConfirmDialogService,
  FilingPlanMode,
  IngestContract,
  Option,
  requiredValue,
  SelectComponent,
  SignaturePolicy,
  SignedDocumentPolicyEnum,
  VitamUICommonModule,
  VitamuiHttpHeaders,
  VitamUILibraryModule,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { ArchiveProfileApiService } from '../../core/api/archive-profile-api.service';
import { ManagementContractApiService } from '../../core/api/management-contract-api.service';
import { FileFormatService } from '../../file-format/file-format.service';
import { IngestContractService } from '../ingest-contract.service';
import { IngestContractCreateValidators } from './ingest-contract-create.validators';

import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckbox } from '@angular/material/checkbox';
import { TranslatePipe } from '@ngx-translate/core';

interface FilingPlanSelection {
  included: string[];
  excluded: string[];
}

interface SignaturePolicyForm {
  signedDocument: SignedDocumentPolicyEnum;
  declaredSignature: boolean | null;
  declaredTimestamp: boolean | null;
  declaredAdditionalProof: boolean | null;
}

interface IngestContractCreateForm {
  identifier: string;
  status: string;
  name: string;
  description: string;
  archiveProfiles: string[];
  managementContractId: string | null;
  everyFormatType: boolean;
  formatType: string[];
  formatUnidentifiedAuthorized: boolean;
  binaryObjectMandatory: boolean;
  everyDataObjectVersion: boolean;
  dataObjectVersion: string[];
  checkParentLink: string;
  signaturePolicy: SignaturePolicyForm;
  signedDocument: SignedDocumentPolicyEnum;
  elementsToCheck: string[];
  masterMandatory: boolean;
  computeInheritedRulesAtIngest: boolean;
}

@Component({
  selector: 'app-ingest-contract-create',
  templateUrl: './ingest-contract-create.component.html',
  styleUrls: ['./ingest-contract-create.component.scss'],
  imports: [
    MatButtonToggleModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatRadioModule,
    MatSelectModule,
    FormRoot,
    FormField,
    SelectComponent,
    VitamUICommonModule,
    VitamUILibraryModule,
    MatCheckbox,
    TranslatePipe,
  ],
})
export class IngestContractCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<IngestContractCreateComponent>>(MatDialogRef);
  data = inject<{
    tenantIdentifier: number;
    isSlaveMode: boolean;
  }>(MAT_DIALOG_DATA);
  private readonly ingestContractService = inject(IngestContractService);
  private readonly ingestContractCreateValidators = inject(IngestContractCreateValidators);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly fileFormatService = inject(FileFormatService);
  private readonly managementContractService = inject(ManagementContractApiService);
  private readonly archiveProfileService = inject(ArchiveProfileApiService);

  readonly SignedDocumentPolicyEnum = SignedDocumentPolicyEnum;
  readonly FilingPlanMode = FilingPlanMode;

  protected readonly tenantIdentifier: number;
  protected readonly isSlaveMode: boolean;

  readonly form = form(
    signal<IngestContractCreateForm>({
      identifier: '',
      status: 'INACTIVE',
      name: '',
      description: '',
      /* <- step 2 -> */
      archiveProfiles: [],
      managementContractId: null,
      /* <- step 3 -> */
      everyFormatType: true,
      formatType: [],
      formatUnidentifiedAuthorized: false,
      /* <- step 4 -> */
      binaryObjectMandatory: true,
      everyDataObjectVersion: true,
      dataObjectVersion: [],
      /* <- step 5 -> */
      checkParentLink: 'AUTHORIZED',
      /* <- step 9 -> */
      signaturePolicy: {
        signedDocument: SignedDocumentPolicyEnum.ALLOWED,
        declaredSignature: null,
        declaredTimestamp: null,
        declaredAdditionalProof: null,
      },
      signedDocument: SignedDocumentPolicyEnum.ALLOWED,
      elementsToCheck: [],
      /* default */
      masterMandatory: true,
      computeInheritedRulesAtIngest: false,
    }),
    (path) => {
      required(path.identifier);
      minLength(path.identifier, 2);
      maxLength(path.identifier, 100);
      this.ingestContractCreateValidators.uniqueIdentifier(path.identifier);
      required(path.name);
      minLength(path.name, 2);
      maxLength(path.name, 100);
      this.ingestContractCreateValidators.uniqueName(path.name);
      required(path.description);
      requiredValue(path.everyFormatType);
      requiredValue(path.formatType);
      requiredValue(path.formatUnidentifiedAuthorized);
      requiredValue(path.dataObjectVersion);
      required(path.checkParentLink);
      requiredValue(path.masterMandatory);
      requiredValue(path.computeInheritedRulesAtIngest);
      const { declaredSignature, declaredTimestamp, declaredAdditionalProof, signedDocument } = path.signaturePolicy;
      for (const checkbox of [declaredSignature, declaredTimestamp, declaredAdditionalProof]) {
        disabled(checkbox, ({ valueOf }) => valueOf(signedDocument) === SignedDocumentPolicyEnum.FORBIDDEN);
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
  // Values of the filing plans, used to compute linkParentId and checkParentId
  readonly filingPlanForm = form(
    signal<{ linkParentId: FilingPlanSelection; checkParentId: FilingPlanSelection }>({
      linkParentId: { included: [], excluded: [] },
      checkParentId: { included: [], excluded: [] },
    }),
  );
  hasCustomGraphicIdentity = false;
  hasError = true;
  message: string;

  private keyPressSubscription: Subscription;

  constructor() {
    const data = this.data;

    this.tenantIdentifier = data.tenantIdentifier;
    this.isSlaveMode = data.isSlaveMode;

    effect(() => {
      const name = this.form.name().value();
      if (!this.isSlaveMode) {
        untracked(() => this.form.identifier().value.set(name));
      }
    });
  }

  formatTypesOptions: VitamuiSelectOptions = { options: [] };
  managementContracts: Option[];
  archiveProfiles: Option[];
  isDisabledButton = signal(false);

  usages: Option[] = [
    { key: 'BinaryMaster', label: 'Original numérique', info: '' },
    { key: 'Dissemination', label: 'Diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignette', info: '' },
    { key: 'TextContent', label: 'Contenu brut', info: '' },
    { key: 'PhysicalMaster', label: 'Original papier', info: '' },
  ];

  ngOnInit() {
    this.fileFormatService.getAllForTenant('' + this.tenantIdentifier).subscribe((fileFormats) => {
      this.formatTypesOptions.options = fileFormats.map((fileFormat) => {
        return { key: fileFormat.puid, label: fileFormat.puid + ' - ' + fileFormat.name };
      });
    });

    const params = new HttpParams().set('embedded', 'ALL');
    const headers = new HttpHeaders().set(VitamuiHttpHeaders.X_TENANT_ID, '' + this.tenantIdentifier);

    this.managementContractService.getAllByParams(params, headers).subscribe((managementContracts) => {
      this.managementContracts = managementContracts.map((managementContract) => ({
        key: managementContract.identifier,
        label: managementContract.name,
      }));
    });

    this.archiveProfileService.getAllByParams(params, headers).subscribe((archiveProfiles) => {
      this.archiveProfiles = archiveProfiles.map((archiveProfile) => ({ key: archiveProfile.identifier, label: archiveProfile.name }));
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.form().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    this.isDisabledButton.set(true);
    const { linkParentId, checkParentId } = this.filingPlanForm().value();
    const formValue = this.form().value();
    const ingestContract = {
      ...formValue,
      // Disabled fields were not part of the value of the former reactive form
      signaturePolicy:
        formValue.signaturePolicy.signedDocument === SignedDocumentPolicyEnum.FORBIDDEN
          ? { signedDocument: formValue.signaturePolicy.signedDocument }
          : formValue.signaturePolicy,
      linkParentId: linkParentId.included.length === 1 ? linkParentId.included[0] : null,
      checkParentId: checkParentId.included.length > 0 ? checkParentId.included : [],
    } as unknown as IngestContract;
    ingestContract.status === 'ACTIVE'
      ? (ingestContract.activationDate = new Date().toISOString())
      : (ingestContract.deactivationDate = new Date().toISOString());
    this.ingestContractService.create(ingestContract).subscribe(
      () => {
        this.isDisabledButton.set(false);
        this.dialogRef.close(true);
      },
      () => {
        this.isDisabledButton.set(false);
        this.dialogRef.close(false);
      },
    );
  }

  private isInvalidOrPending(...fields: (() => { invalid: () => boolean; pending: () => boolean })[]): boolean {
    return fields.some((field) => field().invalid() || field().pending());
  }

  firstStepInvalid(): boolean {
    const invalid = this.isInvalidOrPending(this.form.name, this.form.description, this.form.status);
    if (this.isSlaveMode) {
      return invalid || this.isInvalidOrPending(this.form.identifier);
    } else {
      return invalid;
    }
  }

  thirdStepInvalid(): boolean {
    return (
      this.isInvalidOrPending(this.form.everyFormatType) ||
      (this.form.everyFormatType().value() === false && this.isInvalidOrPending(this.form.formatType))
    );
  }

  fourthStepInvalid(): boolean {
    return (
      this.isInvalidOrPending(this.form.everyDataObjectVersion) ||
      (this.form.everyDataObjectVersion().value() === false && this.isInvalidOrPending(this.form.dataObjectVersion))
    );
  }

  seventhStepInvalid(): boolean {
    return (
      this.isInvalidOrPending(this.filingPlanForm.checkParentId) ||
      (this.form.checkParentLink().value() === 'REQUIRED' && this.filingPlanForm.checkParentId().value().included.length === 0)
    );
  }

  onStatusChange(active: boolean) {
    this.form.status().value.set(active ? 'ACTIVE' : 'INACTIVE');
  }

  selectedSignedDocumentPolicyInvalid(): boolean {
    const signaturePolicy = this.form.signaturePolicy().value() as SignaturePolicy;
    if (signaturePolicy.signedDocument === SignedDocumentPolicyEnum.FORBIDDEN) {
      return signaturePolicy.declaredSignature || signaturePolicy.declaredTimestamp || signaturePolicy.declaredAdditionalProof;
    }
    return false;
  }

  changeSignedDocumentPolicy(signedDocumentPolicyEnum: SignedDocumentPolicyEnum): void {
    if (signedDocumentPolicyEnum === SignedDocumentPolicyEnum.FORBIDDEN) {
      // The checkboxes are disabled by the form schema when the signed documents are forbidden
      this.form.signaturePolicy().value.set({
        signedDocument: SignedDocumentPolicyEnum.FORBIDDEN,
        declaredSignature: null,
        declaredTimestamp: null,
        declaredAdditionalProof: null,
      });
    }
  }
}
