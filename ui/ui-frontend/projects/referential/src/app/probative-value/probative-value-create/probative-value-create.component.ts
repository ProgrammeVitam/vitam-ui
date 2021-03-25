/* tslint:disable:object-literal-key-quotes quotemark */
import {HttpHeaders} from '@angular/common/http';
import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FilingPlanMode} from 'projects/vitamui-library/src/public-api';
import {Subscription} from 'rxjs';
import {ConfirmDialogService, Option, ExternalParametersService, ExternalParameters} from 'ui-frontend-common';
import {ProbativeValueService} from '../probative-value.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import '@angular/localize/init';

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
  accessContractId: string;

  usages: Option[] = [
    {key: 'BinaryMaster', label: 'Archives numériques originales', info: ''},
    {key: 'Dissemination', label: 'Copies de diffusion', info: ''},
    {key: 'Thumbnail', label: 'Vignette', info: ''},
    {key: 'TextContent', label: 'Contenu textuel', info: ''},
    {key: 'PhysicalMaster', label: 'Archives physiques', info: ''}
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
    private externalParameterService: ExternalParametersService,
    private snackBar: MatSnackBar
  ) {
  }

  ngOnInit() {
    this.externalParameterService.getUserExternalParameters().subscribe(parameters => {
      const accessContratId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessContratId && accessContratId.length > 0) {
        this.accessContractId = accessContratId;
      } else {
        this.snackBar.open(
          $localize`:access contrat not set message@@accessContratNotSetErrorMessage:Aucun contrat d'accès n'est associé à l'utiisateur`, 
          null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000
        });
      }
    });

    this.form = this.formBuilder.group({
      unitId: [null, Validators.required],
      usage: [null, Validators.required],
      version: [null, Validators.required]
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy = () => {
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
    if (this.form.invalid) {
      return;
    }

    this.probativeValueService.create(
      this.createDsl(this.form.value), 
      new HttpHeaders({'X-Access-Contract-Id': this.accessContractId})
    ).subscribe(
      () => {
        this.dialogRef.close({success: true, action: 'none'});
      },
      () => {
        this.dialogRef.close({success: false, action: 'none'});
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
                  "#id": [values.unitId]
                }
              }
            ]
          }
        ],
        "$filter": {},
        "$projection": {}
      },
      "usage": values.usage,
      "version": values.version
    };
  }
}
