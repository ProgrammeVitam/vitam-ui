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
import { Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { applyEach, form, FormField, FormRoot, maxLength, minLength, required } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import {
  ApplicationId,
  ConfirmDialogService,
  DialogHeaderComponent,
  InputComponent,
  Logger,
  NextStepComponent,
  ObjectQualifierType,
  ObjectQualifierTypeList,
  ObjectQualifierTypeType,
  PreviousStepComponent,
  SearchCriteriaEltDto,
  SelectComponent,
  SnackBarService,
  StepperComponent,
  UsageVersionEnum,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { ArchiveService } from '../../../archive.service';
import { DIPSedaVersion, QualifierVersion, TransferRequestDto } from '../../../models/dip.interface';
import { CdkStep } from '@angular/cdk/stepper';
import { MatButtonToggle, MatButtonToggleGroup } from '@angular/material/button-toggle';
import { MatRadioButton, MatRadioGroup } from '@angular/material/radio';

interface TransferRequestParametersForm {
  archivalAgreement: string;
  originatingAgencyIdentifier: string;
  archivalAgencyIdentifier: string;
  transferringAgency: string;
  submissionAgencyIdentifier: string;
  relatedTransferReference: string;
  transferRequestReplyIdentifier: string;
  comment: string;
}

interface UsageForm {
  usage: ObjectQualifierTypeType | null;
  version: QualifierVersion;
}

interface TransferExportParametersForm {
  includeLifeCycleLogs: boolean;
  sedaVersion: DIPSedaVersion;
  includeObjects: UsageVersionEnum;
  usages: UsageForm[];
}

/** Optional text fields are sent as null when they are empty */
function emptyToNull<T extends object>(value: T): T {
  return Object.fromEntries(Object.entries(value).map(([key, fieldValue]) => [key, fieldValue === '' ? null : fieldValue])) as T;
}

@Component({
  selector: 'app-transfer-request-modal',
  templateUrl: './transfer-request-modal.component.html',
  styleUrls: ['./transfer-request-modal.component.scss'],
  imports: [
    DialogHeaderComponent,
    StepperComponent,
    CdkStep,
    FormRoot,
    FormField,
    MatDialogContent,
    InputComponent,
    MatDialogActions,
    NextStepComponent,
    MatButtonToggleGroup,
    MatButtonToggle,
    SelectComponent,
    MatRadioGroup,
    MatRadioButton,
    PreviousStepComponent,
    TranslatePipe,
  ],
})
export class TransferRequestModalComponent implements OnInit, OnDestroy {
  private readonly translate = inject(TranslateService);
  dialogRef = inject<MatDialogRef<TransferRequestModalComponent>>(MatDialogRef);
  private readonly archiveService = inject(ArchiveService);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly logger = inject(Logger);
  data = inject<{
    itemSelected: number;
    searchCriteria: SearchCriteriaEltDto[];
    accessContract: string;
    tenantIdentifier: string;
    selectedItemCountKnown?: boolean;
  }>(MAT_DIALOG_DATA);
  private readonly snackBarService = inject(SnackBarService);

  readonly step1Form = form(
    signal<TransferRequestParametersForm>({
      archivalAgreement: '',
      originatingAgencyIdentifier: '',
      archivalAgencyIdentifier: '',
      transferringAgency: '',
      submissionAgencyIdentifier: '',
      relatedTransferReference: '',
      transferRequestReplyIdentifier: '',
      comment: '',
    }),
    (path) => {
      required(path.archivalAgreement);
      minLength(path.archivalAgreement, 2);
      maxLength(path.archivalAgreement, 100);
      required(path.originatingAgencyIdentifier);
      minLength(path.originatingAgencyIdentifier, 2);
      maxLength(path.originatingAgencyIdentifier, 100);
      required(path.archivalAgencyIdentifier);
      minLength(path.archivalAgencyIdentifier, 2);
      maxLength(path.archivalAgencyIdentifier, 100);
      maxLength(path.transferringAgency, 100);
      maxLength(path.submissionAgencyIdentifier, 100);
      maxLength(path.relatedTransferReference, 100);
      maxLength(path.transferRequestReplyIdentifier, 100);
      maxLength(path.comment, 300);
    },
  );
  readonly step2Form = form(
    signal<TransferExportParametersForm>({
      includeLifeCycleLogs: true,
      sedaVersion: '2.3',
      includeObjects: UsageVersionEnum.ALL,
      usages: [{ usage: ObjectQualifierType.BINARYMASTER, version: 'FIRST' }],
    }),
    (path) => {
      applyEach(path.usages, (usage) => required(usage.usage));
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
  itemSelected: number;
  selectedItemCountKnown: boolean;
  keyPressSubscription: Subscription;
  dataObjectVersions = ObjectQualifierTypeList;
  UsageVersionEnum = UsageVersionEnum;
  private readonly selectedUsages = computed(
    (): string[] =>
      this.step2Form
        .usages()
        .value()
        .map((usage) => usage.usage),
    {
      equal: (u1, u2) => u1.length === u2.length && u1.every((v, i) => u2[i] === v),
    },
  );
  // An usage can only be selected once
  readonly usageOptions = computed<VitamuiSelectOptions[]>(() => {
    const selectedUsages = this.selectedUsages();
    return selectedUsages.map((_, i) => {
      const otherUsages = selectedUsages.filter((_, index) => i !== index);
      return {
        options: this.dataObjectVersions.map((usage) => ({
          key: usage,
          label: this.translate.instant(`ARCHIVE_SEARCH.DIP.USAGES.${usage}`),
          disabled: otherUsages.includes(usage),
        })),
      };
    });
  });

  ngOnInit(): void {
    this.itemSelected = this.data.itemSelected;
    this.selectedItemCountKnown = this.data.selectedItemCountKnown;
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  addUsage() {
    this.step2Form.usages().value.update((usages) => [...usages, { usage: null, version: 'FIRST' }]);
  }

  removeUsage(i: number) {
    this.step2Form.usages().value.update((usages) => usages.filter((_, index) => index !== i));
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.step1Form().dirty() || this.step2Form().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  hasBinaryMasterSelected() {
    const usagesArray = this.step2Form.usages().value();
    for (const usage of usagesArray) {
      if (usage.usage === 'BinaryMaster') {
        return true; // Si un usage de BinaryMaster est trouvé, retourne vrai
      }
    }
    return false;
  }

  showWarning(): boolean {
    const step2Values = this.step2Form().value(); // Récupère les valeurs du deuxième formulaire
    return step2Values.includeObjects === UsageVersionEnum.NONE || !this.hasBinaryMasterSelected();
  }

  onSubmit() {
    if (this.step1Form().invalid() || this.step2Form().invalid()) {
      return;
    }

    const step1Values = emptyToNull(this.step1Form().value());
    const step2Values = this.step2Form().value();
    const usages: UsageForm[] = step2Values.includeObjects === UsageVersionEnum.SELECTION ? step2Values.usages : [];
    const transferRequestParameters = {
      ...step1Values,
      relatedTransferReference: step1Values.relatedTransferReference ? [step1Values.relatedTransferReference] : [],
    };
    const transferRequestDto: TransferRequestDto = {
      transferRequestParameters,
      searchCriteria: this.data.searchCriteria,
      dataObjectVersionsPatterns: usages.reduce(
        (acc: TransferRequestDto['dataObjectVersionsPatterns'], uv) => {
          acc[uv.usage] = [uv.version];
          return acc;
        },
        {} as TransferRequestDto['dataObjectVersionsPatterns'],
      ),
      lifeCycleLogs: step2Values.includeLifeCycleLogs,
      withoutObjects: step2Values.includeObjects === UsageVersionEnum.NONE,
      sedaVersion: step2Values.sedaVersion,
    };

    this.archiveService.transferRequestService(transferRequestDto).subscribe(
      (response) => {
        this.dialogRef.close(true);
        this.snackBarService.open({
          message: 'ARCHIVE_SEARCH.DIP.TRANSFER_REQUEST_MESSAGE',
          buttons: [
            {
              appId: ApplicationId.LOGBOOK_OPERATION_APP,
              path: `/tenant/${this.data.tenantIdentifier}?guid=${response}`,
              label: 'SNACK_BAR.TO_OPERATION_APP',
            },
          ],
          duration: 100_000,
        });
        this.step1Form().reset();
        this.step2Form().reset();
      },
      (error: any) => {
        this.logger.error('Error message:', error);
      },
    );
  }
}
