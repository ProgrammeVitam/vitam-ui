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
import {HttpHeaders} from '@angular/common/http';
import {Component, Inject, Input, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import '@angular/localize/init';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FilingPlanMode} from 'projects/vitamui-library/src/public-api';
import {EMPTY, Subscription} from 'rxjs';
import {map, switchMap, take} from 'rxjs/operators';
import {
  AccessionRegisterSummary,
  ConfirmDialogService,
  ExternalParameters,
  ExternalParametersService,
  StartupService,
} from 'ui-frontend-common';
import {AccessContractService} from '../../access-contract/access-contract.service';
import {AuditAction, AuditType} from '../../models/audit.interface';
import {AuditService} from '../audit.service';
import {AuditCreateValidators} from './audit-create-validator';

const PROGRESS_BAR_MULTIPLICATOR = 100;

@Component({
  selector: 'app-audit-create',
  templateUrl: './audit-create.component.html',
  styleUrls: ['./audit-create.component.scss'],
})
export class AuditCreateComponent implements OnInit {
  AuditAction = AuditAction;
  @Input() tenantIdentifier: number;

  FILLING_PLAN_MODE_INCLUDE = FilingPlanMode.INCLUDE_ONLY;

  form: FormGroup;
  stepIndex = 0;

  allProducerServices = new FormControl(true);
  allNodes = new FormControl(true);
  selectedNodes = new FormControl({included: [], excluded: []});

  accessContractId: string = null;
  accessionRegisterSummaries: AccessionRegisterSummary[];
  isDisabledButton = false;

  // stepCount is the total number of steps and is used to calculate the advancement of the progress bar.
  // We could get the number of steps using ViewChildren(StepComponent) but this triggers a
  // "Expression has changed after it was checked" error so we instead manually define the value.
  // Make sure to update this value whenever you add or remove a step from the  template.
  private stepCount = 1;
  private subscriptions = new Subscription();

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
    private snackBar: MatSnackBar
  ) {
  }

  ngOnInit() {
    this.externalParameterService.getUserExternalParameters()
      .pipe(switchMap((params: Map<string, string>) => this.extractAccesContractIdAndGetAccessionRegisterSummaries(params)))
      .pipe(take(1))
      .subscribe((accessContractAndRegisterSummary) => {
        this.accessContractId = accessContractAndRegisterSummary.accessContractId;
        this.accessionRegisterSummaries = accessContractAndRegisterSummary.accessionRegisterSummaries;
      });

    this.form = this.formBuilder.group({
      auditActions: [null, Validators.required],
      auditType: ['tenant', Validators.required],
      evidenceAudit: [null, null, this.auditCreateValidator.checkEvidenceAuditId()],
      objectId: [this.startupService.getTenantIdentifier(), Validators.required],
      query: [this.getRootQuery(null)],
    });


    this.subscriptions.add(
      this.form.controls.auditActions.valueChanges
        .subscribe((auditActions) => this.changeDefaultOnActionSelection(auditActions)));

    this.subscriptions.add(
      this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef)
        .subscribe(() => this.onCancel()));

    this.subscriptions.add(
      this.allProducerServices.valueChanges
        .subscribe((value) => this.updateFieldsOnAllProducerServicesChange(value)));

    this.subscriptions.add(
      this.selectedNodes.valueChanges
        .subscribe((value) => this.changeQueryOnNodesSelection(value)));

    this.subscriptions.add(
      this.form.controls.evidenceAudit.valueChanges
        .subscribe((value) => this.form.controls.objectId.setValue(value)));

    this.subscriptions.add(
      this.allNodes.valueChanges
        .subscribe((value) => (this.stepCount = value ? 1 : 2)));
  }

  private extractAccesContractIdAndGetAccessionRegisterSummaries(params: Map<string, string>) {
    const accessContractId = params.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
    if (!accessContractId || accessContractId.length < 1) {
      this.snackBar.open(
        $localize`:access contrat not set message@@accessContratNotSetErrorMessage:Aucun contrat d'accès n'est associé à l'utiisateur`,
        null,
        {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
        }
      );
      return EMPTY;
    }
    return this.auditService.getAllAccessionRegister(accessContractId)
      .pipe(map((accessionRegisterSummaries) => ({accessContractId, accessionRegisterSummaries})));
  }

  private changeDefaultOnActionSelection(auditActions: AuditAction): void {
    // Update the validators
    if (auditActions === AuditAction.AUDIT_FILE_RECTIFICATION) {
      this.allProducerServices.setValue(false);
      this.form.get('evidenceAudit').setValidators(Validators.required);
    } else {
      this.allProducerServices.setValue(true);
      this.form.get('evidenceAudit').clearValidators();
    }
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
   * Add or remove the required validator on the filed 'objectId'
   */
  private updateObjectIdValidators() {
    if (
      this.allProducerServices.value &&
      this.accessionRegisterSummaries &&
      (this.form.value.auditActions === AuditAction.AUDIT_FILE_EXISTING
        || this.form.value.auditActions === AuditAction.AUDIT_FILE_INTEGRITY)
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
      (this.form.value.auditActions === AuditAction.AUDIT_FILE_INTEGRITY
        || this.form.value.auditActions === AuditAction.AUDIT_FILE_EXISTING) &&
      this.accessContractId != null &&
      !this.form.get('auditType').invalid &&
      !this.form.get('auditType').pending &&
      !this.form.get('objectId').invalid &&
      !this.form.get('objectId').pending;
    return isEvidenceAuditValid || isRectificationAuditValid || isOtherAuditValid;
  }

  ngOnDestroy = () => {
    this.subscriptions.unsubscribe();
  };

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

    this.auditService.create(this.form.value, new HttpHeaders({'X-Access-Contract-Id': this.accessContractId}))
      .subscribe(
        () => {
          this.isDisabledButton = false;
          this.dialogRef.close({success: true, action: 'none'});
        },
        (error: any) => {
          this.dialogRef.close({success: false, action: 'none'});
          console.error(error);
        }
      );
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

  getRootQuery(includedRoots: string[]) {
    if (includedRoots === null) {
      return {
        $query: [
          {
            $or: [{$exists: '#id'}],
          },
        ],
        $filter: {},
        $projection: {},
      };
    }

    return {
      $query: [
        {
          $or: [{$in: {'#allunitups': includedRoots}}],
        },
      ],
      $filter: {},
      $projection: {},
    };
  }
}
