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
import {FormBuilder, FormControl, FormGroup} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FilingPlanMode} from 'projects/vitamui-library/src/lib/components/filing-plan/filing-plan.service';
import {AccessContract} from 'projects/vitamui-library/src/public-api';
import {AccessContractService} from '../../../access-contract.service';


@Component({
  selector: 'app-access-contract-node-update',
  templateUrl: './access-contract-node-update.component.html',
  styleUrls: ['./access-contract-node-update.component.scss']
})
export class AccessContractNodeUpdateComponent implements OnInit {

  accessContract: AccessContract;
  searchAccessContractId: string;
  tenantIdentifier: number;
  selectNodesForm: FormGroup;
  selectedRootsControl = new FormControl();

  hasError = true;
  message: string;

  FILLING_PLAN_MODE_BOTH = FilingPlanMode.BOTH;

  @ViewChild('fileSearch', {static: false}) fileSearch: any;

  constructor(
    public dialogRef: MatDialogRef<AccessContractNodeUpdateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { accessContract: AccessContract, searchAccessContractId: string, tenantIdentifier: number },
    private formBuilder: FormBuilder,
    private accessContractService: AccessContractService
  ) {
    this.searchAccessContractId = this.data.searchAccessContractId;
    this.tenantIdentifier = this.data.tenantIdentifier;
    this.accessContract = this.data.accessContract;
    this.selectNodesForm = this.formBuilder.group({
      rootUnits: null,
      excludedRootUnits: null
    });
  }

  ngOnInit() {
    this.selectedRootsControl.valueChanges.subscribe(value => {
      this.selectNodesForm.get('rootUnits').setValue(value.included);
      this.selectNodesForm.get('excludedRootUnits').setValue(value.excluded);
    });

    this.selectedRootsControl.setValue({
      included: this.accessContract.rootUnits || [],
      excluded: this.accessContract.excludedRootUnits || []
    });
  }

  onCancel() {
    this.dialogRef.close();
  }

  updateAccessContractNodes() {
    const formData = {
      id: this.accessContract.id,
      identifier: this.accessContract.identifier,
      rootUnits: this.selectNodesForm.get('rootUnits').value,
      excludedRootUnits: this.selectNodesForm.get('excludedRootUnits').value
    };

    this.accessContractService.patch(formData)
      .subscribe(
        () => {
          this.dialogRef.close(true);
        },
        (error) => {
          this.dialogRef.close(false);
          console.error(error);
        });
  }
}
