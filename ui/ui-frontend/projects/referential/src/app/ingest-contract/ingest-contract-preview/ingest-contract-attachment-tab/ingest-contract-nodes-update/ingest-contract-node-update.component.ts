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
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import {
  FilingPlanMode,
  IngestContract,
  DialogHeaderComponent,
  StepperComponent,
  NextStepComponent,
  TooltipDirective,
  PreviousStepComponent,
  FilingPlanComponent,
} from 'vitamui-library';
import { IngestContractService } from '../../../ingest-contract.service';
import { CdkStep } from '@angular/cdk/stepper';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { TranslatePipe } from '@ngx-translate/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatTreeModule } from '@angular/material/tree';
import { CommonModule } from '@angular/common';
import { MatCheckboxModule } from '@angular/material/checkbox';

@Component({
  selector: 'app-ingest-contract-node-update',
  templateUrl: './ingest-contract-node-update.component.html',
  styleUrls: ['./ingest-contract-node-update.component.scss'],
  imports: [
    DialogHeaderComponent,
    ReactiveFormsModule,
    StepperComponent,
    CdkStep,
    CdkScrollable,
    MatDialogContent,
    MatDialogActions,
    NextStepComponent,
    MatButtonToggleGroup,
    MatButtonToggle,
    TooltipDirective,
    PreviousStepComponent,
    TranslatePipe,
    CommonModule,
    FilingPlanComponent,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatTreeModule,
  ],
})
export class IngestContractNodeUpdateComponent implements OnInit {
  dialogRef = inject<MatDialogRef<IngestContractNodeUpdateComponent>>(MatDialogRef);
  data = inject<{
    ingestContract: IngestContract;
    accessContractId: string;
    tenantIdentifier: number;
  }>(MAT_DIALOG_DATA);
  private formBuilder = inject(FormBuilder);
  private ingestContractService = inject(IngestContractService);

  ingestContract: IngestContract;
  accessContractId: string;
  tenantIdentifier: number;
  selectNodesForm: FormGroup;

  hasError = true;
  message: string;

  FILING_PLAN_MODE_SOLO = FilingPlanMode.SOLO;
  FILING_PLAN_MODE_INC = FilingPlanMode.INCLUDE_ONLY;

  linkParentIdControl = new FormControl();
  checkParentIdControl = new FormControl();
  checkParentLinkControl = new FormControl();

  constructor() {
    this.accessContractId = this.data.accessContractId;
    this.ingestContract = this.data.ingestContract;
    this.tenantIdentifier = this.data.tenantIdentifier;
    this.selectNodesForm = this.formBuilder.group({
      linkParentId: [{ value: null, disabled: true }, Validators.required],
      checkParentLink: [null, Validators.required],
      checkParentId: [{ value: null, disabled: true }, Validators.required],
    });
  }

  ngOnInit() {
    this.linkParentIdControl.valueChanges.subscribe((value) => {
      if (value.included.length > 0) {
        this.selectNodesForm.controls['linkParentId'].setValue(value.included[0]);
      } else {
        this.selectNodesForm.controls['linkParentId'].setValue('');
      }
    });

    this.checkParentIdControl.valueChanges.subscribe((value) => {
      this.selectNodesForm.controls['checkParentId'].setValue(value.included);
    });

    this.checkParentLinkControl.valueChanges.subscribe((value) => {
      this.selectNodesForm.controls['checkParentLink'].setValue(value);
    });

    this.linkParentIdControl.setValue(
      this.ingestContract.linkParentId ? { included: [this.ingestContract.linkParentId], excluded: [] } : { included: [], excluded: [] },
    );

    this.checkParentIdControl.setValue(
      this.ingestContract.checkParentId ? { included: this.ingestContract.checkParentId, excluded: [] } : { included: [], excluded: [] },
    );

    this.checkParentLinkControl.setValue(this.ingestContract.checkParentLink ? this.ingestContract.checkParentLink : 'AUTHORIZED');
  }

  onCancel() {
    this.dialogRef.close();
  }

  updateIngestContractNodes() {
    const formData = {
      id: this.ingestContract.id,
      identifier: this.ingestContract.identifier,
      checkParentId: this.selectNodesForm.get('checkParentId').value,
      linkParentId: this.selectNodesForm.get('linkParentId').value,
      checkParentLink: this.selectNodesForm.get('checkParentLink').value,
    };

    this.ingestContractService.patch(formData).subscribe((updatedIngestContract) => {
      this.dialogRef.close(updatedIngestContract);
    });
  }
}
