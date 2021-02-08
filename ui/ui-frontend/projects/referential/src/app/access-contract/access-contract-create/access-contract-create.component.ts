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
import { Component, Inject, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { ConfirmDialogService, ExternalParameters, ExternalParametersService, Option } from 'ui-frontend-common';

import { AccessContract, FilingPlanMode } from 'projects/vitamui-library/src/public-api';
import { AgencyService } from '../../agency/agency.service';
import { AccessContractCreateValidators } from './access-contract-create.validators';
import '@angular/localize/init';
import { AccessContractService } from '../access-contract.service';

const PROGRESS_BAR_MULTIPLICATOR = 100;

@Component({
  selector: 'app-access-contract-create',
  templateUrl: './access-contract-create.component.html',
  styleUrls: ['./access-contract-create.component.scss']
})
export class AccessContractCreateComponent implements OnInit, OnDestroy {

  @Input() tenantIdentifier: number;
  @Input() isSlaveMode: boolean;

  form: FormGroup;
  stepIndex = 0;
  hasCustomGraphicIdentity = false;
  hasError = true;
  message: string;

  FILLING_PLAN_MODE = FilingPlanMode;

  // stepCount is the total number of steps and is used to calculate the advancement of the progress bar.
  // We could get the number of steps using ViewChildren(StepComponent) but this triggers a
  // "Expression has changed after it was checked" error so we instead manually define the value.
  // Make sure to update this value whenever you add or remove a step from the  template.
  private stepCount = 4;
  private keyPressSubscription: Subscription;

  @ViewChild('fileSearch', { static: false }) fileSearch: any;

  constructor(
    public dialogRef: MatDialogRef<AccessContractCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private accessContractCreateValidators: AccessContractCreateValidators,
    private accessContractService: AccessContractService,
    private agencyService: AgencyService,
    private confirmDialogService: ConfirmDialogService,
    private externalParameterService: ExternalParametersService,
    private snackBar: MatSnackBar
  ) {
  }

  statusControl = new FormControl(false);
  accessLogControl = new FormControl(true);
  allNodes = new FormControl(false);
  ruleFilter = new FormControl(false);
  selectNodesControl = new FormControl({ included: [], excluded: [] });
  accessContractSelect = new FormControl(null, Validators.required);

  originatingAgencies: Option[] = [];

  // FIXME: Get list from common var ?
  rules: Option[] = [
    { key: 'StorageRule', label: 'Durée d\'utilité courante', info: '' },
    { key: 'ReuseRule', label: 'Durée de réutilisation', info: '' },
    { key: 'ClassificationRule', label: 'Durée de classification', info: '' },
    { key: 'DisseminationRule', label: 'Délai de diffusion', info: '' },
    { key: 'AccessRule', label: 'Durée d\'utilité administrative', info: '' },
    { key: 'AppraisalRule', label: 'Délai de communicabilité', info: '' }
  ];

  // FIXME: Get list from common var ?
  usages: Option[] = [
    { key: 'BinaryMaster', label: 'Archives numériques originales', info: '' },
    { key: 'Dissemination', label: 'Copies de diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignettes', info: '' },
    { key: 'TextContent', label: 'Contenu textuel', info: '' },
    { key: 'PhysicalMaster', label: 'Archives physiques', info: '' }
  ];

  ngOnInit() {

    this.externalParameterService.getUserExternalParameters().subscribe(parameters => {
      const accessContratId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessContratId && accessContratId.length > 0) {
        this.accessContractSelect.setValue(accessContratId);
      } else {
        this.snackBar.open(
          $localize`:access contrat not set message@@accessContratNotSetErrorMessage:Aucun contrat d'accès n'est associé à l'utiisateur`, 
          null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000
        });
      }
    });

    this.agencyService.getAll().subscribe(agencies =>
      this.originatingAgencies = agencies.map(x => ({ label: x.name, key: x.identifier }))
    );

    this.form = this.formBuilder.group({
      identifier: [null, Validators.required, this.accessContractCreateValidators.uniqueIdentifier()],
      status: ['INACTIVE'],
      name: [null, [Validators.required], this.accessContractCreateValidators.uniqueName()],
      description: [null],
      accessLog: ['ACTIVE'],
      ruleCategoryToFilter: [new Array<string>(), Validators.required],
      /* <- step 2 -> */
      everyOriginatingAgency: [true],
      originatingAgencies: [null],
      everyDataObjectVersion: [true],
      dataObjectVersion: [new Array<string>()],
      /* <- step 3 -> */
      writingPermission: [false],
      writingRestrictedDesc: [true],
      /* <- step 4 -> */
      rootUnits: [[], Validators.required],
      excludedRootUnits: [[]]
    });

    this.statusControl.valueChanges.subscribe((value) => {
      this.form.controls.status.setValue(value = (value === false) ? 'INACTIVE' : 'ACTIVE');
    });

    this.accessLogControl.valueChanges.subscribe((value) => {
      this.form.controls.accessLog.setValue(value = (value === false) ? 'INACTIVE' : 'ACTIVE');
    });

    this.selectNodesControl.valueChanges.subscribe((value: { included: string[], excluded: string[] }) => {
      this.form.controls.rootUnits.setValue(value.included);
      this.form.controls.excludedRootUnits.setValue(value.excluded);
    });

    this.form.controls.name.valueChanges.subscribe((value) => {
      if (!this.isSlaveMode) {
        this.form.controls.identifier.setValue(value);
      }
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.lastStepInvalid()) {
      return;
    }
    const accessContract = this.form.value as AccessContract;
    accessContract.status === 'ACTIVE' ? accessContract.activationDate = new Date().toISOString() : accessContract.deactivationDate = new Date().toISOString();
    this.accessContractService.create(accessContract).subscribe(
      () => {
        this.dialogRef.close(true);
      },
      (error) => {
        this.dialogRef.close(false);
        console.error(error);
      });
  }

  firstStepInvalid(): boolean {
    return this.form.get('name').invalid || this.form.get('name').pending ||
      this.form.get('description').invalid || this.form.get('description').pending ||
      this.form.get('status').invalid || this.form.get('status').pending ||
      this.form.get('accessLog').invalid || this.form.get('accessLog').pending ||
      (this.ruleFilter.value === true && (this.form.get('ruleCategoryToFilter').invalid || this.form.get('ruleCategoryToFilter').pending));
  }

  secondStepInvalid(): boolean {
    return (this.form.get('everyOriginatingAgency').value === false &&
      (this.form.get('originatingAgencies').invalid ||
        this.form.get('originatingAgencies').pending)) ||
      (this.form.get('everyDataObjectVersion').value === false &&
        (this.form.get('dataObjectVersion').invalid ||
          this.form.get('dataObjectVersion').pending));
  }

  lastStepInvalid(): boolean {
    return this.allNodes.invalid || this.allNodes.pending ||
      (this.allNodes.value === false && (this.form.controls.rootUnits.invalid || this.form.controls.rootUnits.value.length === 0));
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

}
