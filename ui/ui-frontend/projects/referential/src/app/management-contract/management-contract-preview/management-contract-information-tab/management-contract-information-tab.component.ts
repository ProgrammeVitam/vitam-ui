/**
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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { ManagementContract, diff } from 'vitamui-library';
import { ManagementContractService } from '../../management-contract.service';

@Component({
  selector: 'app-management-contract-information-tab',
  templateUrl: './management-contract-information-tab.component.html',
  styleUrls: ['./management-contract-information-tab.component.scss'],
})
export class ManagementContractInformationTabComponent implements OnInit {
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;
  submitting = false;

  @Input()
  set inputManagementContract(managementContract: ManagementContract) {
    this._inputManagementContract = managementContract;

    if (!managementContract.description) {
      this._inputManagementContract.description = '';
    }

    this.resetForm(this.inputManagementContract);
    this.updated.emit(false);
  }

  get inputManagementContract(): ManagementContract {
    return this._inputManagementContract;
  }

  _inputManagementContract: ManagementContract;

  constructor(
    private formBuilder: FormBuilder,
    private managementContractService: ManagementContractService,
  ) {
    this.form = this.formBuilder.group({
      identifier: [{ value: null, disabled: true }, Validators.required],
      name: [null, Validators.required],
      description: [''],
      status: [null],
    });
  }

  ngOnInit(): void {}

  previousValue = (): any => {
    return { ...this._inputManagementContract, status: this._inputManagementContract.status === 'ACTIVE' };
  };

  unchanged(): boolean {
    const differences = diff(this.form.getRawValue(), this.previousValue());
    const unchanged = JSON.stringify(differences) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<ManagementContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) => {
        // Update the activation and deactivation dates if the contract status has changed before sending the data
        const partialManagementContract = { ...formData };
        const date = new Date().toISOString();
        const statusIsConsistent = formData.status !== undefined && formData.status !== null;

        if (statusIsConsistent && formData.status) {
          partialManagementContract.activationDate = date;
          partialManagementContract.deactivationDate = null;
          partialManagementContract.status = 'ACTIVE';
        }

        if (statusIsConsistent && !formData.status) {
          partialManagementContract.activationDate = null;
          partialManagementContract.deactivationDate = date;
          partialManagementContract.status = 'INACTIVE';
        }

        return this.managementContractService.patch(partialManagementContract).pipe(catchError(() => of(null)));
      }),
    );
  }

  onSubmit() {
    this.submitting = true;
    this.prepareSubmit().subscribe(
      () => {
        this.managementContractService.get(this._inputManagementContract.identifier).subscribe((response) => {
          this.submitting = false;
          this.inputManagementContract = response;
        });
      },
      () => {
        this.submitting = false;
      },
    );
  }

  resetForm(managementContract: ManagementContract) {
    this.form.reset({ ...managementContract, status: managementContract.status === 'ACTIVE' }, { emitEvent: false });
  }
}
