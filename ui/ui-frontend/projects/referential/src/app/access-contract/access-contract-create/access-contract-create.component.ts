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
import { Component, effect, inject, OnDestroy, OnInit, signal, untracked } from '@angular/core';
import { form, FormField, FormRoot, maxLength, minLength, required, validate } from '@angular/forms/signals';
import { MatButtonToggleChange } from '@angular/material/button-toggle';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import {
  AccessContract,
  AccessContractService,
  AgencyService,
  ConfirmDialogService,
  FilingPlanMode,
  Option,
  SelectComponent,
  Status,
  VitamUICommonModule,
  VitamUILibraryModule,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { AccessContractCreateValidators } from './access-contract-create.validators';

import { finalize, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { AccessContractAuthorizationsUpdateComponent } from '../access-contract-preview/access-contract-authorizations-tab/access-contract-authorizations-update/access-contract-authorizations-update.component';
import { TranslatePipe } from '@ngx-translate/core';

interface AccessContractCreateForm {
  identifier: string;
  status: boolean;
  name: string;
  description: string | null;
  accessLog: boolean;
  /* <- step 3 -> */
  thirdStep: {
    writingPermission: boolean;
    downloadChoose: string;
    everyDataObjectVersion: boolean;
    dataObjectVersion: string[];
    writingAuthorizedDesc: boolean;
  };
}

interface SelectedNodes {
  included: string[];
  excluded: string[];
}

@Component({
  selector: 'app-access-contract-create',
  templateUrl: './access-contract-create.component.html',
  styleUrls: ['./access-contract-create.component.scss'],
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
    AccessContractAuthorizationsUpdateComponent,
    VitamUICommonModule,
    VitamUILibraryModule,
    TranslatePipe,
  ],
})
export class AccessContractCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<AccessContractCreateComponent>>(MatDialogRef);
  data = inject<{
    tenantIdentifier: number;
    isSlaveMode: boolean;
  }>(MAT_DIALOG_DATA);
  private readonly accessContractCreateValidators = inject(AccessContractCreateValidators);
  private readonly accessContractService = inject(AccessContractService);
  private readonly agencyService = inject(AgencyService);
  private readonly confirmDialogService = inject(ConfirmDialogService);

  protected readonly FILLING_PLAN_MODE = FilingPlanMode;
  protected readonly tenantIdentifier: number;
  protected readonly isSlaveMode: boolean;

  readonly form = form(
    signal<AccessContractCreateForm>({
      identifier: '',
      status: false,
      name: '',
      description: null,
      accessLog: false,
      thirdStep: {
        writingPermission: false,
        downloadChoose: 'ALL',
        everyDataObjectVersion: true,
        dataObjectVersion: [],
        writingAuthorizedDesc: false,
      },
    }),
    (path) => {
      required(path.identifier);
      minLength(path.identifier, 2);
      maxLength(path.identifier, 100);
      this.accessContractCreateValidators.uniqueIdentifier(path.identifier);
      required(path.name);
      minLength(path.name, 2);
      maxLength(path.name, 100);
      this.accessContractCreateValidators.uniqueName(path.name);
      validate(path.thirdStep, ({ value }) =>
        value().downloadChoose === 'SELECTION' && value().dataObjectVersion.length === 0 ? { kind: 'dataObjectVersion' } : undefined,
      );
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

  private readonly unsubscribe = new Subject<void>();

  readonly allNodes = signal(false);
  /* <- step 4 -> */
  readonly selectNodesForm = form(signal<SelectedNodes>({ included: [], excluded: [] }));

  isLoading = signal(false);

  originatingAgenciesOptions: VitamuiSelectOptions = { options: [] };
  // FIXME: Get list from common var ?
  usages: Option[] = [
    { key: 'BinaryMaster', label: 'Archives numériques originales', info: '' },
    { key: 'Dissemination', label: 'Copies de diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignettes', info: '' },
    { key: 'TextContent', label: 'Contenu textuel', info: '' },
    { key: 'PhysicalMaster', label: 'Archives physiques', info: '' },
  ];
  private secondStepData: AccessContract;

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
    this.onWritingRestrictedDescChanges();
  }

  ngOnInit() {
    this.agencyService.getOriginatingAgenciesAsOptions().subscribe((options: Option[]) => (this.originatingAgenciesOptions = { options }));

    this.confirmDialogService
      .listenToEscapeKeyPress(this.dialogRef)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  onCancel(): void {
    if (this.form().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit(): void {
    if (this.lastStepInvalid()) {
      return;
    }
    this.isLoading.set(true);
    const accessContract: AccessContract = this.mapToAccessContract();
    accessContract.status === 'ACTIVE'
      ? (accessContract.activationDate = new Date().toISOString())
      : (accessContract.deactivationDate = new Date().toISOString());

    this.accessContractService
      .create(accessContract)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (_accessContract) => this.dialogRef.close(true),
        error: (e) => console.error(e),
      });
  }

  public firstStepInvalid(): boolean {
    return [this.form.identifier, this.form.name, this.form.description, this.form.status, this.form.accessLog].some(
      (field) => field().invalid() || field().pending(),
    );
  }

  public saveSecondStepData(accessContract: AccessContract): void {
    this.secondStepData = accessContract;
  }

  private onWritingRestrictedDescChanges(): void {
    effect(() => {
      if (this.form.thirdStep.writingAuthorizedDesc().value()) {
        untracked(() => this.form.thirdStep.writingPermission().value.set(true));
      }
    });
    effect(() => {
      if (!this.form.thirdStep.writingPermission().value()) {
        untracked(() => this.form.thirdStep.writingAuthorizedDesc().value.set(false));
      }
    });
  }

  onDownloadChooseChange({ value }: MatButtonToggleChange): void {
    this.form.thirdStep.everyDataObjectVersion().value.set(value === 'ALL');
    if (value !== 'SELECTION') {
      this.form.thirdStep.dataObjectVersion().value.set([]);
    }
  }

  hasThirdStepErrors(): boolean {
    return this.form.thirdStep().errors().length > 0;
  }

  public lastStepInvalid(): boolean {
    return this.allNodes() === false && this.selectNodesForm.included().value().length === 0;
  }

  private mapToAccessContract(): AccessContract {
    const value = this.form().value();
    return {
      ...value,
      rootUnits: this.selectNodesForm.included().value(),
      excludedRootUnits: this.selectNodesForm.excluded().value(),
      ...this.secondStepData,
      writingPermission: value.thirdStep.writingPermission,
      downloadChoose: value.thirdStep.downloadChoose,
      everyDataObjectVersion: value.thirdStep.everyDataObjectVersion,
      dataObjectVersion: value.thirdStep.dataObjectVersion,
      writingRestrictedDesc: !value.thirdStep.writingAuthorizedDesc,
      status: this.mapStatus(value.status),
      accessLog: this.mapStatus(value.accessLog),
    } as unknown as AccessContract;
  }

  private mapStatus(value: boolean): Status {
    return value ? Status.ACTIVE : Status.INACTIVE;
  }
}
