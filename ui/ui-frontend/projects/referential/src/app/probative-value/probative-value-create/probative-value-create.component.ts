import { HttpHeaders } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { Subscription } from 'rxjs';
import { ConfirmDialogService, Option } from 'ui-frontend-common';
import { FilingPlanMode } from 'vitamui-library';

import { AccessContractService } from '../../access-contract/access-contract.service';
import { ProbativeValueService } from '../probative-value.service';

const PROGRESS_BAR_MULTIPLICATOR = 100;

@Component({
  selector: 'app-probative-value-create',
  templateUrl: './probative-value-create.component.html',
  styleUrls: ['./probative-value-create.component.scss']
})
export class ProbativeValueCreateComponent implements OnInit {
  FILLING_PLAN_MODE_INCLUDE = FilingPlanMode.INCLUDE_ONLY;

  form: FormGroup;
  stepIndex = 0;

  allServices = new FormControl(true);
  allNodes = new FormControl(true);
  selectedNodes = new FormControl();
  accessContractSelect = new FormControl(null, Validators.required);

  accessContracts: Option[];
  usages: Option[] = [
    { key: 'BinaryMaster', label: 'Archives numériques originales', info: '' },
    { key: 'Dissemination', label: 'Copies de diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignette', info: '' },
    { key: 'TextContent', label: 'Contenu textuel', info: '' },
    { key: 'PhysicalMaster', label: 'Archives physiques', info: '' }
  ];

  // stepCount is the total number of steps and is used to calculate the advancement of the progress bar.
  // We could get the number of steps using ViewChildren(StepComponent) but this triggers a
  // "Expression has changed after it was checked" error so we instead manually define the value.
  // Make sure to update this value whenever you add or remove a step from the  template.
  private stepCount = 1;
  private keyPressSubscription: Subscription;

  constructor(
    public dialogRef: MatDialogRef<ProbativeValueCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private confirmDialogService: ConfirmDialogService,
    private probativeValueService: ProbativeValueService,
    protected accessContractService: AccessContractService
  ) { }

  ngOnInit() {
    this.accessContractService.getAll().subscribe((value) => {
      this.accessContracts = value.map(x => ( { key: x.identifier, label: x.name } ));
    });

    this.form = this.formBuilder.group({
      unitId: [null, Validators.required],
      usage: [null, Validators.required],
      version: [null, Validators.required]
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() { this.keyPressSubscription.unsubscribe(); }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    console.log('Form valid ? ', this.form.invalid);
    if (this.form.invalid) { return; }
    console.log('data: ', this.form.value);

    this.probativeValueService.create(this.createDsl(this.form.value), new HttpHeaders({ 'X-Access-Contract-Id': this.accessContractSelect.value })).subscribe(
      () => {
        this.dialogRef.close({ success: true, action: "none" });
      },
      (error: any) => {
        this.dialogRef.close({ success: false, action: "none" });
        console.error(error);
      });
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

  createDsl(values: any) {
    return {
      "dslQuery": {
        "$query": [
          {
            "$or": [
              {
                "$in": {
                  "#id": [ values.unitId ]
                }
              }
            ]
          }
        ],
        "$filter":{},
        "$projection":{}
      },
      "usage": values.usage,
      "version": values.version
    }
  }
}
