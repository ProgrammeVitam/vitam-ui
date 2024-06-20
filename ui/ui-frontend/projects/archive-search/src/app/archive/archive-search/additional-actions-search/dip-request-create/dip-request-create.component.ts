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

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import * as uuid from 'uuid';
import {
  ConfirmDialogService,
  Logger,
  ObjectQualifierTypeList,
  ObjectQualifierTypeType,
  SearchCriteriaEltDto,
  StartupService,
  UsageVersionEnum,
} from 'vitamui-library';
import { ArchiveService } from '../../../archive.service';
import { ExportDIPRequestDto, QualifierVersion } from '../../../models/dip.interface';

@Component({
  selector: 'app-dip-request-create',
  templateUrl: './dip-request-create.component.html',
  styleUrls: ['./dip-request-create.component.scss'],
})
export class DipRequestCreateComponent implements OnInit, OnDestroy {
  stepIndex = 0;
  stepCount = 2;
  formGroups: FormGroup[];

  constructor(
    private translate: TranslateService,
    public dialogRef: MatDialogRef<DipRequestCreateComponent>,
    private fb: FormBuilder,
    private archiveService: ArchiveService,
    private startupService: StartupService,
    private confirmDialogService: ConfirmDialogService,
    private logger: Logger,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      itemSelected: number;
      exportDIPSearchCriteria: SearchCriteriaEltDto[];
      accessContract: string;
      tenantIdentifier: string;
      selectedItemCountKnown?: boolean;
    },
  ) {}

  itemSelected: number;
  selectedItemCountKnown: boolean;
  keyPressSubscription: Subscription;
  dataObjectVersions = ObjectQualifierTypeList;
  UsageVersionEnum = UsageVersionEnum;

  ngOnInit(): void {
    this.itemSelected = this.data.itemSelected;
    this.selectedItemCountKnown = this.data.selectedItemCountKnown;
    this.initForms();
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  private initForms() {
    const messageRequestIdentifier = uuid.v4();
    this.formGroups = [
      this.fb.group({
        messageRequestIdentifier: [{ value: messageRequestIdentifier, disabled: true }, Validators.required],
        requesterIdentifier: [null, Validators.required],
        archivalAgencyIdentifier: [null, Validators.required],
        authorizationRequestReplyIdentifier: [null],
        submissionAgencyIdentifier: [null],
        comment: [null],
        archivalAgreement: [this.data.accessContract],
      }),
      this.fb.group({
        includeLifeCycleLogs: [false],
        sedaVersion: ['2.2'],
        includeObjects: [UsageVersionEnum.ALL],
        usages: this.fb.array([
          this.fb.group({
            usage: ['BinaryMaster', Validators.required],
            version: ['FIRST'],
          }),
        ]),
      }),
    ];
  }

  get messageRequestIdentifier(): FormControl {
    return this.formGroups[0].get('messageRequestIdentifier') as FormControl;
  }

  get requesterIdentifier(): FormControl {
    return this.formGroups[0].get('requesterIdentifier') as FormControl;
  }

  get archivalAgencyIdentifier(): FormControl {
    return this.formGroups[0].get('archivalAgencyIdentifier') as FormControl;
  }

  get usages(): FormArray {
    return this.formGroups[1].get('usages') as FormArray;
  }

  listUsages(i: number): string[] {
    const otherUsages = (this.usages.value as { usage: string }[]).filter((_, index) => i !== index).map((v) => v.usage);
    return this.dataObjectVersions.filter((usage) => !otherUsages.includes(usage));
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

  onSubmit() {
    if (this.formGroups.some((fg) => fg.invalid)) {
      return;
    }

    const step1Values = this.formGroups[0].getRawValue();
    const step2Values = this.formGroups[1].getRawValue();
    const usages: { usage: ObjectQualifierTypeType; version: QualifierVersion }[] =
      step2Values.includeObjects === UsageVersionEnum.SELECTION ? step2Values.usages : [];
    const exportDIPRequestDto: ExportDIPRequestDto = {
      dipRequestParameters: step1Values,
      exportDIPSearchCriteria: this.data.exportDIPSearchCriteria,
      dataObjectVersionsPatterns: usages.reduce(
        (acc: ExportDIPRequestDto['dataObjectVersionsPatterns'], uv) => {
          acc[uv.usage] = [uv.version];
          return acc;
        },
        {} as ExportDIPRequestDto['dataObjectVersionsPatterns'],
      ),
      lifeCycleLogs: step2Values.includeLifeCycleLogs,
      withoutObjects: step2Values.includeObjects === UsageVersionEnum.NONE,
      sedaVersion: step2Values.sedaVersion,
    };

    this.archiveService.exportDIPService(exportDIPRequestDto).subscribe(
      (response) => {
        this.dialogRef.close(true);
        const serviceUrl = `${this.startupService.getReferentialUrl()}/logbook-operation/tenant/${
          this.data.tenantIdentifier
        }?guid=${response}`;

        this.archiveService.openSnackBarForWorkflow(this.translate.instant('ARCHIVE_SEARCH.DIP.DIP_REQUEST_MESSAGE'), serviceUrl);
      },
      (error: any) => {
        this.logger.error('Error message :', error);
      },
    );
  }
}
