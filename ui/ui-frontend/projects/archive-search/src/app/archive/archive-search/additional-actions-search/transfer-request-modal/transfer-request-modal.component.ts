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
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import {
  ApplicationId,
  ConfirmDialogService,
  Logger,
  ObjectQualifierTypeList,
  ObjectQualifierTypeType,
  SearchCriteriaEltDto,
  UsageVersionEnum,
  VitamuiSelectOptions,
  SnackBarService,
} from 'vitamui-library';
import { ArchiveService } from '../../../archive.service';
import { QualifierVersion, TransferRequestDto } from '../../../models/dip.interface';
import { delay, distinctUntilChanged, map } from 'rxjs/operators';

@Component({
  selector: 'app-transfer-request-modal',
  templateUrl: './transfer-request-modal.component.html',
  styleUrls: ['./transfer-request-modal.component.scss'],
  standalone: false,
})
export class TransferRequestModalComponent implements OnInit, OnDestroy {
  private translate = inject(TranslateService);
  dialogRef = inject<MatDialogRef<TransferRequestModalComponent>>(MatDialogRef);
  private fb = inject(FormBuilder);
  private archiveService = inject(ArchiveService);
  private confirmDialogService = inject(ConfirmDialogService);
  private logger = inject(Logger);
  data = inject<{
    itemSelected: number;
    searchCriteria: SearchCriteriaEltDto[];
    accessContract: string;
    tenantIdentifier: string;
    selectedItemCountKnown?: boolean;
  }>(MAT_DIALOG_DATA);
  private snackBarService = inject(SnackBarService);

  formGroups: FormGroup[];
  itemSelected: number;
  selectedItemCountKnown: boolean;
  keyPressSubscription: Subscription;
  dataObjectVersions = ObjectQualifierTypeList;
  UsageVersionEnum = UsageVersionEnum;
  usageOptions: VitamuiSelectOptions[] = [];

  ngOnInit(): void {
    this.itemSelected = this.data.itemSelected;
    this.selectedItemCountKnown = this.data.selectedItemCountKnown;
    this.initForms();
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  private initForms() {
    this.formGroups = [
      this.fb.group({
        archivalAgreement: [null, [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
        originatingAgencyIdentifier: [null, [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
        archivalAgencyIdentifier: [null, [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
        transferringAgency: [null, [Validators.maxLength(100)]],
        submissionAgencyIdentifier: [null, [Validators.maxLength(100)]],
        relatedTransferReference: [null, [Validators.maxLength(100)]],
        transferRequestReplyIdentifier: [null, [Validators.maxLength(100)]],
        comment: [null, [Validators.maxLength(300)]],
      }),
      this.fb.group({
        includeLifeCycleLogs: [true],
        sedaVersion: ['2.3'],
        includeObjects: [UsageVersionEnum.ALL],
        usages: this.fb.array([
          this.fb.group({
            usage: ['BinaryMaster', Validators.required],
            version: ['FIRST'],
          }),
        ]),
      }),
    ];

    this.computeUsageOptions();
    this.formGroups[1]
      .get('usages')
      .valueChanges.pipe(
        map((usages) => usages.map((usage: any) => usage.usage)),
        distinctUntilChanged((u1: string[], u2: string[]) => u1.length === u2.length && u1.every((v, i) => u2[i] === v)),
        delay(0),
      )
      .subscribe(() => this.computeUsageOptions());
  }

  get archivalAgreement(): FormControl {
    return this.formGroups[0].get('archivalAgreement') as FormControl;
  }

  get originatingAgencyIdentifier(): FormControl {
    return this.formGroups[0].get('originatingAgencyIdentifier') as FormControl;
  }

  get archivalAgencyIdentifier(): FormControl {
    return this.formGroups[0].get('archivalAgencyIdentifier') as FormControl;
  }

  get usages(): FormArray {
    return this.formGroups[1].get('usages') as FormArray;
  }

  private computeUsageOptions() {
    this.usages.controls.forEach((_, i) => {
      if (!this.usageOptions[i]) {
        this.usageOptions[i] = {
          options: this.dataObjectVersions.map((usage) => ({
            key: usage,
            label: this.translate.instant(`ARCHIVE_SEARCH.DIP.USAGES.${usage}`),
          })),
        };
      }
      const otherUsages = (this.usages.value as any[]).filter((_, index) => i !== index).map((v: { usage: string }) => v.usage);
      this.usageOptions[i].options.forEach((option) => (option.disabled = otherUsages.includes(option.key)));
    });
  }

  addUsage() {
    this.usages.push(
      this.fb.group({
        usage: [null, Validators.required],
        version: ['FIRST'],
      }),
    );
  }

  removeUsage(i: number) {
    this.usages.removeAt(i);
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.formGroups.some((fg) => fg.dirty)) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  hasBinaryMasterSelected() {
    const usagesArray = this.formGroups[1].getRawValue().usages;
    for (const usage of usagesArray) {
      if (usage.usage === 'BinaryMaster') {
        return true; // Si un usage de BinaryMaster est trouvé, retourne vrai
      }
    }
    return false;
  }

  showWarning(): boolean {
    const step2Values = this.formGroups[1].getRawValue(); // Récupère les valeurs du deuxième formGroup
    return step2Values.includeObjects === UsageVersionEnum.NONE || !this.hasBinaryMasterSelected();
  }

  onSubmit() {
    if (this.formGroups.some((fg) => fg.invalid)) {
      return;
    }

    const step1Values = this.formGroups[0].getRawValue();
    const step2Values = this.formGroups[1].getRawValue();
    const usages: { usage: ObjectQualifierTypeType; version: QualifierVersion }[] =
      step2Values.includeObjects === UsageVersionEnum.SELECTION ? step2Values.usages : [];
    const transferRequestDto: TransferRequestDto = {
      transferRequestParameters: step1Values,
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
        this.formGroups.forEach((fg) => fg.reset());
      },
      (error: any) => {
        this.logger.error('Error message:', error);
      },
    );
  }
}
