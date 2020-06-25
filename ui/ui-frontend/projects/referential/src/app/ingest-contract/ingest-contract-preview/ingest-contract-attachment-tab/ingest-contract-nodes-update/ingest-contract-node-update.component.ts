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
import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FilingPlanMode} from 'projects/vitamui-library/src/lib/components/filing-plan/filing-plan.service';
import {IngestContract} from 'projects/vitamui-library/src/public-api';
import {IngestContractService} from '../../../ingest-contract.service';


@Component({
  selector: 'app-ingest-contract-node-update',
  templateUrl: './ingest-contract-node-update.component.html',
  styleUrls: ['./ingest-contract-node-update.component.scss']
})
export class IngestContractNodeUpdateComponent implements OnInit {

  ingestContract: IngestContract;
  accessContractId: string;
  tenantIdentifier: number;
  selectNodesForm: FormGroup;
  stepIndex = 0;

  hasError = true;
  message: string;

  FILING_PLAN_MODE_SOLO = FilingPlanMode.SOLO;
  FILING_PLAN_MODE_INC = FilingPlanMode.INCLUDE_ONLY;

  linkParentIdControl = new FormControl();
  checkParentIdControl = new FormControl();

  @ViewChild('fileSearch', {static: false}) fileSearch: any;

  constructor(
    public dialogRef: MatDialogRef<IngestContractNodeUpdateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { ingestContract: IngestContract, accessContractId: string, tenantIdentifier: number },
    private formBuilder: FormBuilder,
    private ingestContractService: IngestContractService
  ) {
    this.accessContractId = this.data.accessContractId;
    this.ingestContract = this.data.ingestContract;
    this.tenantIdentifier = this.data.tenantIdentifier;
    this.selectNodesForm = this.formBuilder.group({
      linkParentId: [{value: null, disabled: true}, Validators.required],
      checkParentLink: ['AUTHORIZED', Validators.required],
      checkParentId: [{value: null, disabled: true}, Validators.required]
    });
  }

  ngOnInit() {
    this.linkParentIdControl.valueChanges.subscribe((value) => {
      if (value.included.length > 0) {
        this.selectNodesForm.controls.linkParentId.setValue(value.included[0]);
      } else {
        this.selectNodesForm.controls.linkParentId.setValue(null);
      }
    });

    this.checkParentIdControl.valueChanges.subscribe((value) => {
      console.log('included: ', value.included);
      this.selectNodesForm.controls.checkParentId.setValue(value.included);
    });

    this.linkParentIdControl.setValue(
      this.ingestContract.linkParentId ? {included: [this.ingestContract.linkParentId], excluded: []} : {
        included: [],
        excluded: []
      }
    );
    this.checkParentIdControl.setValue({
      included: this.ingestContract.checkParentId ? this.ingestContract.checkParentId : [],
      excluded: []
    });
  }

  onCancel() {
    this.dialogRef.close();
  }

  updateIngestContractNodes() {
    console.log('CPI:', this.selectNodesForm.get('checkParentId').value);
    const formData = {
      id: this.ingestContract.id,
      identifier: this.ingestContract.identifier,
      checkParentId: this.selectNodesForm.get('checkParentId').value,
      linkParentId: this.selectNodesForm.get('linkParentId').value,
      checkParentLink: this.selectNodesForm.get('checkParentLink').value
    };

    this.ingestContractService.patch(formData)
      .subscribe(
        () => {
          this.dialogRef.close(true);
        },
        (error: any) => {
          this.dialogRef.close(false);
          console.error(error);
        });
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / 2) * 100;
  }
}
