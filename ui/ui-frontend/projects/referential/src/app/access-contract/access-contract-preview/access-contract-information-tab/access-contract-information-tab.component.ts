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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { AccessContract, Option, diff } from 'vitamui-library';
import { RULE_TYPES } from '../../../rule/rules.constants';
import { AccessContractCreateValidators } from '../../access-contract-create/access-contract-create.validators';
import { AccessContractService } from '../../access-contract.service';

@Component({
  selector: 'app-access-contract-information-tab',
  templateUrl: './access-contract-information-tab.component.html',
  styleUrls: ['./access-contract-information-tab.component.scss'],
})
export class AccessContractInformationTabComponent {
  @Input() set accessContract(accessContract: AccessContract) {
    this.setAccessContract(accessContract);
    this.resetForm(this.accessContract);
    this.updated.emit(false);
  }

  get accessContract(): AccessContract {
    return this._accessContract;
  }

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() isFormValid: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() updatedAccessContract: EventEmitter<AccessContract> = new EventEmitter<AccessContract>();

  public form: FormGroup;
  public submited = false;
  public statusControl = new FormControl();
  public accessLogControl = new FormControl();
  public rules: Option[] = RULE_TYPES;
  public ruleFilter = new FormControl(false);

  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  private _accessContract: AccessContract;

  previousValue = (): AccessContract => {
    return this._accessContract;
  };

  constructor(
    private formBuilder: FormBuilder,
    private accessContractService: AccessContractService,
    private accessContractCreateValidators: AccessContractCreateValidators,
  ) {}

  public onSubmit() {
    this.submited = true;
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit().subscribe(
      () => {
        this.accessContractService.get(this._accessContract.identifier).subscribe((response) => {
          this.submited = false;
          this.accessContract = response;
          this.resetForm(this.accessContract);
          this.updatedAccessContract.emit(response);
        });
      },
      () => {
        this.submited = false;
      },
    );
  }

  public unChanged(): boolean {
    const unchanged =
      JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}' &&
      (this.statusControl.value ? 'ACTIVE' : 'INACTIVE') === this.previousValue().status &&
      (this.accessLogControl.value ? 'ACTIVE' : 'INACTIVE') === this.previousValue().accessLog;

    this.updated.emit(!unchanged);

    return unchanged;
  }

  public isInvalid(): boolean {
    const isInvalid =
      this.form.get('name').invalid ||
      this.form.get('name').pending ||
      this.form.get('description').invalid ||
      this.form.get('description').pending ||
      this.form.get('status').invalid ||
      this.form.get('status').pending ||
      this.form.get('accessLog').invalid ||
      this.form.get('accessLog').pending ||
      (this.ruleFilter.value === false && (this.form.get('ruleCategoryToFilter').invalid || this.form.get('ruleCategoryToFilter').pending));
    this.isFormValid.emit(!isInvalid);
    return isInvalid;
  }

  private prepareSubmit(): Observable<AccessContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) => {
        if (formData.status) {
          if (formData.status === 'ACTIVE') {
            formData.activationDate = new Date();
          } else {
            formData.status = 'INACTIVE';
            formData.deactivationDate = new Date();
          }
        }
        return this.accessContractService.patch(formData).pipe(catchError(() => of(null)));
      }),
    );
  }

  private initForm(): void {
    this.form = this.formBuilder.group({
      identifier: [null, Validators.required],
      status: ['ACTIVE'],
      name: [null, Validators.required, this.accessContractCreateValidators.uniqueNameWhileEdit(this.previousValue)],
      description: [null],
      accessLog: ['ACTIVE'],
      ruleCategoryToFilter: [new Array<string>()],
      creationDate: [null],
    });

    this.statusControl.valueChanges.subscribe((value) => {
      this.form.controls.status.setValue((value = value === false ? 'INACTIVE' : 'ACTIVE'));
    });

    this.accessLogControl.valueChanges.subscribe((value) => {
      this.form.controls.accessLog.setValue((value = value === false ? 'INACTIVE' : 'ACTIVE'));
    });

    this.ruleFilter.valueChanges.subscribe((val) => {
      if (val === true) {
        this.form.controls.ruleCategoryToFilter.setValue(this._accessContract.ruleCategoryToFilter);
      } else {
        this.form.controls.ruleCategoryToFilter.setValue([]);
      }
    });
  }

  private resetForm(accessContract: AccessContract) {
    if (!this.form) {
      this.initForm();
    }

    this.statusControl.setValue(accessContract.status === 'ACTIVE');
    this.accessLogControl.setValue(accessContract.accessLog === 'ACTIVE');
    this.form.reset(accessContract, { emitEvent: false });
  }

  private setAccessContract(accessContract: AccessContract): void {
    this._accessContract = accessContract;

    if (!this._accessContract.ruleCategoryToFilter) {
      this._accessContract.ruleCategoryToFilter = [];
    }

    this.ruleFilter.setValue(this._accessContract.ruleCategoryToFilter?.length > 0);

    if (!this._accessContract.description) {
      this._accessContract.description = '';
    }
  }
}
