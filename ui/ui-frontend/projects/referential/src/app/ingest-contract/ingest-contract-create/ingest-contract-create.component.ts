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
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import {
  ConfirmDialogService,
  FilingPlanMode,
  IngestContract,
  Option,
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
import { SharedModule } from '../../../../../identity/src/app/shared/shared.module';
import { TranslateModule } from '@ngx-translate/core';
import { MatCheckbox } from '@angular/material/checkbox';

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
    ReactiveFormsModule,
    SelectComponent,
    SharedModule,
    TranslateModule,
    VitamUICommonModule,
    VitamUILibraryModule,
    MatCheckbox,
  ],
})
export class IngestContractCreateComponent implements OnInit, OnDestroy {
  readonly SignedDocumentPolicyEnum = SignedDocumentPolicyEnum;
  readonly FilingPlanMode = FilingPlanMode;

  protected readonly tenantIdentifier: number;
  protected readonly isSlaveMode: boolean;

  form: FormGroup;
  hasCustomGraphicIdentity = false;
  hasError = true;
  message: string;

  private keyPressSubscription: Subscription;

  constructor(
    public dialogRef: MatDialogRef<IngestContractCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { tenantIdentifier: number; isSlaveMode: boolean },
    private formBuilder: FormBuilder,
    private ingestContractService: IngestContractService,
    private ingestContractCreateValidators: IngestContractCreateValidators,
    private confirmDialogService: ConfirmDialogService,
    private fileFormatService: FileFormatService,
    private managementContractService: ManagementContractApiService,
    private archiveProfileService: ArchiveProfileApiService,
  ) {
    this.tenantIdentifier = data.tenantIdentifier;
    this.isSlaveMode = data.isSlaveMode;
  }

  statusControl = new FormControl(false);
  linkParentIdControl = new FormControl();
  checkParentIdControl = new FormControl();

  formatTypesOptions: VitamuiSelectOptions = { options: [] };
  managementContracts: Option[];
  archiveProfiles: Option[];
  isDisabledButton = false;

  usages: Option[] = [
    { key: 'BinaryMaster', label: 'Original numérique', info: '' },
    { key: 'Dissemination', label: 'Diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignette', info: '' },
    { key: 'TextContent', label: 'Contenu brut', info: '' },
    { key: 'PhysicalMaster', label: 'Original papier', info: '' },
  ];

  ngOnInit() {
    this.form = this.formBuilder.group({
      identifier: [
        null,
        [Validators.required, Validators.minLength(2), Validators.maxLength(100)],
        this.ingestContractCreateValidators.uniqueIdentifier(),
      ],
      status: ['INACTIVE'],
      name: [
        null,
        [Validators.required, Validators.minLength(2), Validators.maxLength(100)],
        this.ingestContractCreateValidators.uniqueName(),
      ],
      description: [null, Validators.required],
      /* <- step 2 -> */
      archiveProfiles: [new Array<string>() /* Validators.required */],
      managementContractId: [null],

      /* <- step 3 -> */
      everyFormatType: [true, Validators.required],
      formatType: [new Array<string>(), Validators.required],
      formatUnidentifiedAuthorized: [false, Validators.required],
      /* <- step 4 -> */
      binaryObjectMandatory: [true],
      everyDataObjectVersion: [true],
      dataObjectVersion: [new Array<string>(), Validators.required],

      /* <- step 5 -> */
      checkParentLink: ['AUTHORIZED', Validators.required],
      linkParentId: [null, Validators.required],
      checkParentId: [new Array<string>(), Validators.required],

      /* <- step 9 -> */
      signaturePolicy: this.formBuilder.group({
        signedDocument: [SignedDocumentPolicyEnum.ALLOWED],
        declaredSignature: [],
        declaredTimestamp: [],
        declaredAdditionalProof: [],
      }),
      signedDocument: [SignedDocumentPolicyEnum.ALLOWED],
      elementsToCheck: [new Array<string>()],

      /* default */
      masterMandatory: [true, Validators.required],
      computeInheritedRulesAtIngest: [false, Validators.required],
    });

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

    this.statusControl.valueChanges.subscribe((value) => {
      this.form.controls.status.setValue(value === false ? 'INACTIVE' : 'ACTIVE');
    });

    this.form.controls.name.valueChanges.subscribe((value) => {
      if (!this.isSlaveMode) {
        this.form.controls.identifier.setValue(value);
      }
    });

    this.linkParentIdControl.valueChanges.subscribe((value: { included: string[]; excluded: string[] }) => {
      if (value.included.length === 1) {
        this.form.controls.linkParentId.setValue(value.included[0]);
      } else {
        this.form.controls.linkParentId.setValue(null);
      }
    });

    this.checkParentIdControl.valueChanges.subscribe((value: { included: string[]; excluded: string[] }) => {
      if (value.included.length > 0) {
        this.form.controls.checkParentId.setValue(value.included);
      } else {
        this.form.controls.checkParentId.setValue([]);
      }
    });

    this.linkParentIdControl.setValue({ included: [], excluded: [] });
    this.checkParentIdControl.setValue({ included: [], excluded: [] });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef, { subTitle: 'INGEST_CONTRACT.CREATE_DIALOG.TITLE' });
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    this.isDisabledButton = true;
    const ingestContract = this.form.value as IngestContract;
    ingestContract.status === 'ACTIVE'
      ? (ingestContract.activationDate = new Date().toISOString())
      : (ingestContract.deactivationDate = new Date().toISOString());
    this.ingestContractService.create(ingestContract).subscribe(
      () => {
        this.isDisabledButton = false;
        this.dialogRef.close(true);
      },
      () => {
        this.isDisabledButton = false;
        this.dialogRef.close(false);
      },
    );
  }

  firstStepInvalid(): boolean {
    const invalid =
      this.form.get('name').invalid ||
      this.form.get('name').pending ||
      this.form.get('description').invalid ||
      this.form.get('description').pending ||
      this.form.get('status').invalid ||
      this.form.get('status').pending;
    if (this.isSlaveMode) {
      return invalid || this.form.get('identifier').invalid || this.form.get('identifier').pending;
    } else {
      return invalid;
    }
  }

  thirdStepInvalid(): boolean {
    return (
      this.form.get('everyFormatType').invalid ||
      this.form.get('everyFormatType').pending ||
      (this.form.get('everyFormatType').value === false && (this.form.get('formatType').invalid || this.form.get('formatType').pending))
    );
  }

  fourthStepInvalid(): boolean {
    return (
      this.form.get('everyDataObjectVersion').invalid ||
      this.form.get('everyDataObjectVersion').pending ||
      (this.form.get('everyDataObjectVersion').value === false &&
        (this.form.get('dataObjectVersion').invalid || this.form.get('dataObjectVersion').pending))
    );
  }

  seventhStepInvalid(): boolean {
    return (
      this.checkParentIdControl.invalid ||
      this.checkParentIdControl.pending ||
      (this.form.get('checkParentLink').value === 'REQUIRED' && this.checkParentIdControl.value.included.length === 0)
    );
  }

  get signaturePolicy(): FormGroup {
    return this.form.controls.signaturePolicy as FormGroup;
  }

  selectedSignedDocumentPolicyInvalid(): boolean {
    const signaturePolicy: SignaturePolicy = this.signaturePolicy.value;
    if (signaturePolicy.signedDocument === SignedDocumentPolicyEnum.FORBIDDEN) {
      return signaturePolicy.declaredSignature || signaturePolicy.declaredTimestamp || signaturePolicy.declaredAdditionalProof;
    }
  }

  changeSignedDocumentPolicy(signedDocumentPolicyEnum: SignedDocumentPolicyEnum): void {
    if (signedDocumentPolicyEnum === SignedDocumentPolicyEnum.FORBIDDEN) {
      this.signaturePolicy.setValue({
        signedDocument: SignedDocumentPolicyEnum.FORBIDDEN,
        declaredSignature: null,
        declaredTimestamp: null,
        declaredAdditionalProof: null,
      });
    }
    const isDisabled = this.signedDocumentPolicyIsDisabled();
    if (isDisabled) {
      this.signaturePolicy.get('declaredSignature').disable();
      this.signaturePolicy.get('declaredTimestamp').disable();
      this.signaturePolicy.get('declaredAdditionalProof').disable();
    } else {
      this.signaturePolicy.get('declaredSignature').enable();
      this.signaturePolicy.get('declaredTimestamp').enable();
      this.signaturePolicy.get('declaredAdditionalProof').enable();
    }
  }

  private signedDocumentPolicyIsDisabled(): boolean {
    return this.signaturePolicy?.value?.signedDocument === SignedDocumentPolicyEnum.FORBIDDEN;
  }
}
