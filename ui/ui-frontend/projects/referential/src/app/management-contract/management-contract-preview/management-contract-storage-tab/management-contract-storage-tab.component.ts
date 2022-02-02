/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import {Component,EventEmitter,Input,OnInit,Output} from '@angular/core';
import {FormBuilder,FormControl,FormGroup,Validators} from '@angular/forms';
import {ManagementContract} from 'projects/vitamui-library/src/public-api';
import {Observable,of, Subscription} from 'rxjs';
import {catchError,filter,map,switchMap} from 'rxjs/operators';
import {diff} from 'ui-frontend-common';
import {extend,isEmpty} from 'underscore';
import {ManagementContractService} from '../../management-contract.service';

@Component({
  selector: 'app-management-contract-storage-tab',
  templateUrl: './management-contract-storage-tab.component.html',
  styleUrls: ['./management-contract-storage-tab.component.scss']
})
export class ManagementContractStorageTabComponent implements OnInit {
  @Output() updated: EventEmitter<boolean>=new EventEmitter<boolean>();
  form: FormGroup;
  submited=false;

  statusControlValueChangesSubscribe: Subscription;

  @Input()
  set inputManagementContract(managementContract: ManagementContract) {
    if (!managementContract.storage.unitStrategy) {
      managementContract.storage.unitStrategy = "";
    }
    if (!managementContract.storage.objectGroupStrategy) {
      managementContract.storage.objectGroupStrategy = "";
    }
    if (managementContract.storage.objectStrategy) {
      managementContract.storage.objectStrategy = "";
    }
    this._inputManagementContract=managementContract;

    if(!managementContract.description) {
      this._inputManagementContract.description='';
    }
    this.form.controls.status.setValue(managementContract.status);
    this.statusControl = new FormControl(managementContract.status === 'ACTIVE');
    this.resetForm(this.inputManagementContract);
    this.updated.emit(false);

    if (this.statusControlValueChangesSubscribe) {
      this.statusControlValueChangesSubscribe.unsubscribe();
    }
    this.statusControlValueChangesSubscribe = this.statusControl.valueChanges.subscribe((value:boolean) => {
      this.form.controls.status.setValue(value === false ? 'INACTIVE' : 'ACTIVE');
    });
  }

  get inputManagementContract(): ManagementContract {
    return this._inputManagementContract;
  }

  public _inputManagementContract: ManagementContract;
  public statusControl = new FormControl();

  @Input()
  set readOnly(readOnly: boolean) {
    if(readOnly&&this.form.enabled) {
      this.form.disable({emitEvent: false});
    } else if(this.form.disabled) {
      this.form.enable({emitEvent: false});
      this.form.get('identifier').disable({emitEvent: false});
    }
  }

  previousValue=(): ManagementContract => {
    return this._inputManagementContract;
  }

  constructor(
    private formBuilder: FormBuilder,
    private managementContractService: ManagementContractService
  ) {
    this.form=this.formBuilder.group({
      identifier: [null,Validators.required],
      name: [null,Validators.required],
      description: [null,Validators.required],
      status: [null],
      storage: this.formBuilder.group({
        unitStrategy: ['', Validators.required],
        objectGroupStrategy: ['', Validators.required],
        objectStrategy: ['', Validators.required],
      }),
    });
    this.form.disable({emitEvent: false});
  }

  ngOnInit(): void {

  }

  unchanged(): boolean {
    const unchanged=JSON.stringify(diff(this.form.getRawValue(),this.previousValue()))==='{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<ManagementContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) => {
        // Update the activation and deactivation dates if the contract status has changed before sending the data
        if (formData.status) {
          if (formData.status === 'ACTIVE') {
            formData.activationDate = new Date();
            formData.deactivationDate = null;
          } else {
            formData.status = 'INACTIVE';
            formData.activationDate = null;
            formData.deactivationDate = new Date();
          }
        }
        return this.managementContractService.patch(formData).pipe(catchError(() => of(null)));
      })
    );
  }

  onSubmit() {
    this.submited=true;
    this.prepareSubmit().subscribe(() => {
      this.managementContractService.get(this._inputManagementContract.identifier).subscribe(
        response => {
          this.submited=false;
          this.inputManagementContract=response;
        }
      );
    },() => {
      this.submited=false;
    });
  }

  resetForm(managementContract: ManagementContract) {
    this.form.reset(managementContract,{emitEvent: false});
  }


  ngOnDestroy() {
    this.statusControlValueChangesSubscribe.unsubscribe();
  }
}
