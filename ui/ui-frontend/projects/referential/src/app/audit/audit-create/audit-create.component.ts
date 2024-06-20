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
import { HttpHeaders } from '@angular/common/http';
import { Component, Inject, Input, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { EMPTY, Subject } from 'rxjs';
import { map, switchMap, take, takeUntil } from 'rxjs/operators';
import {
  AccessionRegisterSummary,
  ConfirmDialogService,
  ExternalParameters,
  ExternalParametersService,
  FilingPlanMode,
  StartupService,
  VitamUISnackBarService,
} from 'vitamui-library';
import { AccessContractService } from '../../access-contract/access-contract.service';
import { AuditAction, AuditType } from '../../models/audit.interface';
import { AuditService } from '../audit.service';
import { AuditCreateValidators } from './audit-create-validator';

@Component({
  selector: 'app-audit-create',
  templateUrl: './audit-create.component.html',
  styleUrls: ['./audit-create.component.scss'],
})
export class AuditCreateComponent implements OnInit, OnDestroy {
  @Input() tenantIdentifier: number;

  public AuditAction = AuditAction;
  public form: FormGroup;
  public stepIndex = 0;
  public stepCount = 2;
  public allProducerServices = new FormControl(true);
  public allNodes = new FormControl(true);
  public selectedNodes = new FormControl({ included: [], excluded: [] });
  public accessContractId: string = null;
  public accessionRegisterSummaries: AccessionRegisterSummary[];
  public isDisabledButton = false;
  public FILLING_PLAN_MODE_INCLUDE = FilingPlanMode.INCLUDE_ONLY;

  private destroyer$ = new Subject<void>();

  constructor(
    public dialogRef: MatDialogRef<AuditCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private confirmDialogService: ConfirmDialogService,
    private auditService: AuditService,
    private startupService: StartupService,
    protected accessContractService: AccessContractService,
    private auditCreateValidator: AuditCreateValidators,
    private externalParameterService: ExternalParametersService,
    private snackBarService: VitamUISnackBarService,
  ) {}

  ngOnInit() {
    this.form = this.formBuilder.group({
      auditActions: [AuditAction.AUDIT_FILE_EXISTING, Validators.required],
      auditType: ['tenant', Validators.required],
      evidenceAudit: [null, null, this.auditCreateValidator.checkEvidenceAuditId()],
      objectId: [this.startupService.getTenantIdentifier()],
      query: [this.getRootQuery(null)],
    });

    this.externalParameterService
      .getUserExternalParameters()
      .pipe(switchMap((params: Map<string, string>) => this.extractAccesContractIdAndGetAccessionRegisterSummaries(params)))
      .pipe(take(1))
      .subscribe((accessContractAndRegisterSummary) => {
        this.accessContractId = accessContractAndRegisterSummary.accessContractId;
        this.accessionRegisterSummaries = accessContractAndRegisterSummary.accessionRegisterSummaries;
      });

    this.confirmDialogService
      .listenToEscapeKeyPress(this.dialogRef)
      .pipe(takeUntil(this.destroyer$))
      .subscribe(() => this.onCancel());

    this.form.controls.auditActions.valueChanges
      .pipe(takeUntil(this.destroyer$))
      .subscribe((auditActions) => this.changeDefaultOnActionSelection(auditActions));

    this.allProducerServices.valueChanges
      .pipe(takeUntil(this.destroyer$))
      .subscribe((value) => this.updateFieldsOnAllProducerServicesChange(value));

    this.selectedNodes.valueChanges.pipe(takeUntil(this.destroyer$)).subscribe((value) => this.changeQueryOnNodesSelection(value));

    this.form.controls.evidenceAudit.valueChanges.pipe(takeUntil(this.destroyer$)).subscribe((value) => {
      if (this.form.get('auditActions').value === AuditAction.AUDIT_FILE_RECTIFICATION) {
        this.form.controls.objectId.setValue(value);
      }
    });
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public getAuditActions(): String[] {
    return Object.keys(AuditAction);
  }

  public showProducerToggle(): boolean {
    const selectedAuditAction = this.form.get('auditActions').value;
    return selectedAuditAction === AuditAction.AUDIT_FILE_EXISTING || selectedAuditAction === AuditAction.AUDIT_FILE_INTEGRITY;
  }

  public showAllNodesToggle(): boolean {
    const selectedAuditAction = this.form.get('auditActions').value;
    return selectedAuditAction === AuditAction.AUDIT_FILE_CONSISTENCY;
  }

  public showProducerSelection(): boolean {
    return this.showProducerToggle() && this.allProducerServices.value === false;
  }

  public showEvidenceAuditInput(): boolean {
    const selectedAuditAction = this.form.get('auditActions').value;
    return selectedAuditAction === AuditAction.AUDIT_FILE_RECTIFICATION;
  }

  public getStepCount(): number {
    return this.allNodes.value ? 1 : 2;
  }

  private extractAccesContractIdAndGetAccessionRegisterSummaries(params: Map<string, string>) {
    const accessContractId = params.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
    if (!accessContractId || accessContractId.length < 1) {
      this.snackBarService.open({ message: 'SNACKBAR.NO_ACCESS_CONTRACT_LINKED' });
      return EMPTY;
    }

    return this.auditService
      .getAllAccessionRegister(accessContractId)
      .pipe(map((accessionRegisterSummaries) => ({ accessContractId, accessionRegisterSummaries })));
  }

  private changeDefaultOnActionSelection(auditActions: AuditAction): void {
    // Update the validators
    if (auditActions === AuditAction.AUDIT_FILE_RECTIFICATION) {
      this.allProducerServices.setValue(false);
      this.form.get('evidenceAudit').setValidators(Validators.required);
    } else {
      this.allProducerServices.setValue(true);
      this.form.get('evidenceAudit').clearValidators();
      this.form.get('evidenceAudit').setValue(null);
      this.form.get('evidenceAudit').markAsUntouched();
    }

    this.allNodes.setValue(true);
    this.form.get('evidenceAudit').updateValueAndValidity();
    this.updateObjectIdValidators();
    this.form.updateValueAndValidity();

    // Update the audit type
    if (auditActions === AuditAction.AUDIT_FILE_EXISTING || auditActions === AuditAction.AUDIT_FILE_INTEGRITY) {
      this.form.controls.auditType.setValue(this.allProducerServices.value ? AuditType.tenant : AuditType.originatingagency);
    } else {
      this.form.controls.auditType.setValue(AuditType.dsl);
    }
  }

  private changeQueryOnNodesSelection(value: { included: Array<string>; excluded: Array<string> }): void {
    if (value && value.included && value.included.length > 0) {
      this.form.controls.query.setValue(this.getRootQuery(value.included));
    } else {
      this.form.controls.query.setValue(this.getRootQuery(null));
    }
  }

  private updateFieldsOnAllProducerServicesChange(allProducerServices: boolean): void {
    if (this.form.controls.auditActions.value !== AuditAction.AUDIT_FILE_RECTIFICATION) {
      this.form.controls.auditType.setValue(allProducerServices ? AuditType.tenant : AuditType.originatingagency);
    }
    this.form.controls.objectId.setValue(allProducerServices ? this.startupService.getTenantIdentifier() : null);
    this.updateObjectIdValidators();
    this.form.updateValueAndValidity();
  }

  /**
   * Add or remove the required validator on the field 'objectId'
   */
  private updateObjectIdValidators() {
    if (
      !this.allProducerServices.value &&
      this.accessionRegisterSummaries &&
      (this.form.value.auditActions === AuditAction.AUDIT_FILE_EXISTING ||
        this.form.value.auditActions === AuditAction.AUDIT_FILE_INTEGRITY)
    ) {
      this.form.get('objectId').setValidators(Validators.required);
    } else {
      this.form.get('objectId').clearValidators();
    }
  }

  isStepValid(): boolean {
    const isEvidenceAuditValid = this.form.value.auditActions === AuditAction.AUDIT_FILE_CONSISTENCY && this.accessContractId != null;
    const isRectificationAuditValid =
      this.form.value.auditActions === AuditAction.AUDIT_FILE_RECTIFICATION &&
      this.accessContractId != null &&
      !this.form.get('evidenceAudit').invalid &&
      !this.form.get('evidenceAudit').pending;
    const isOtherAuditValid =
      (this.form.value.auditActions === AuditAction.AUDIT_FILE_INTEGRITY ||
        this.form.value.auditActions === AuditAction.AUDIT_FILE_EXISTING) &&
      this.accessContractId != null &&
      !this.form.get('auditType').invalid &&
      !this.form.get('auditType').pending &&
      !this.form.get('objectId').invalid &&
      !this.form.get('objectId').pending;
    return isEvidenceAuditValid || isRectificationAuditValid || isOtherAuditValid;
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.form.invalid) {
      this.isDisabledButton = true;
      return;
    }
    this.isDisabledButton = true;

    this.auditService.create(this.form.value, new HttpHeaders({ 'X-Access-Contract-Id': this.accessContractId })).subscribe(
      () => {
        this.isDisabledButton = false;
        this.dialogRef.close({ success: true, action: 'none' });
      },
      () => {
        this.dialogRef.close({ success: false, action: 'none' });
      },
    );
  }

  getRootQuery(includedRoots: string[]) {
    if (includedRoots === null) {
      return {
        $query: [
          {
            $or: [{ $exists: '#id' }],
          },
        ],
        $filter: {},
        $projection: {},
      };
    }

    return {
      $query: [
        {
          $or: [{ $in: { '#allunitups': includedRoots } }],
        },
      ],
      $filter: {},
      $projection: {},
    };
  }
}
