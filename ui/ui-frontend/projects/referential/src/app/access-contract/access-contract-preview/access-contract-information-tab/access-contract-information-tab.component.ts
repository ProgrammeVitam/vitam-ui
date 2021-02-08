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
import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {AccessContract} from 'projects/vitamui-library/src/public-api';
import {Observable, of} from 'rxjs';
import {catchError, filter, map, switchMap} from 'rxjs/operators';
import {diff, Option} from 'ui-frontend-common';
import {extend, isEmpty} from 'underscore';

import {AccessContractCreateValidators} from '../../access-contract-create/access-contract-create.validators';
import {AccessContractService} from '../../access-contract.service';

@Component({
  selector: 'app-access-contract-information-tab',
  templateUrl: './access-contract-information-tab.component.html',
  styleUrls: ['./access-contract-information-tab.component.scss']
})
export class AccessContractInformationTabComponent {

  @Input()
  set accessContract(accessContract: AccessContract) {
    this._accessContract = accessContract;

    if (!this._accessContract.ruleCategoryToFilter) {
      this._accessContract.ruleCategoryToFilter = [];
    }

    this.ruleFilter.setValue(this._accessContract.ruleCategoryToFilter && this._accessContract.ruleCategoryToFilter.length > 0);

    if (!this._accessContract.description) {
      this._accessContract.description = '';
    }

    this.resetForm(this.accessContract);
    this.updated.emit(false);
  }

  get accessContract(): AccessContract {
    return this._accessContract;
  }

  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({emitEvent: false});
    } else if (this.form.disabled) {
      this.form.enable({emitEvent: false});
      this.form.get('identifier').disable({emitEvent: false});
    }
  }

  constructor(
    private formBuilder: FormBuilder,
    private accessContractService: AccessContractService,
    private accessContractCreateValidators: AccessContractCreateValidators
  ) {
    this.form = this.formBuilder.group({
      identifier: [null, Validators.required],
      status: ['ACTIVE'],
      name: [null, [], this.accessContractCreateValidators.uniqueNameWhileEdit(this.previousValue)],
      description: [null],
      accessLog: ['ACTIVE'],
      ruleCategoryToFilter: [new Array<string>()],
      creationDate: [null]
    });

    this.statusControl.valueChanges.subscribe((value) => {
      this.form.controls.status.setValue(value = (value === false) ? 'INACTIVE' : 'ACTIVE');
    });

    this.accessLogControl.valueChanges.subscribe((value) => {
      this.form.controls.accessLog.setValue(value = (value === false) ? 'INACTIVE' : 'ACTIVE');
    });

    this.ruleFilter.valueChanges.subscribe((val) => {
      if (val === true) {
        this.form.controls.ruleCategoryToFilter.setValue(this._accessContract.ruleCategoryToFilter);
      } else {
        this.form.controls.ruleCategoryToFilter.setValue([]);
      }
    });
  }

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;

  submited = false;

  ruleFilter = new FormControl(false);
  statusControl = new FormControl();
  accessLogControl = new FormControl();

  // tslint:disable-next-line:variable-name
  private _accessContract: AccessContract;

  // FIXME: Get list from common var ?
  rules: Option[] = [
    {key: 'StorageRule', label: 'Durée d\'utilité courante', info: ''},
    {key: 'ReuseRule', label: 'Durée de réutilisation', info: ''},
    {key: 'ClassificationRule', label: 'Durée de classification', info: ''},
    {key: 'DisseminationRule', label: 'Délai de diffusion', info: ''},
    {key: 'AccessRule', label: 'Durée d\'utilité administrative', info: ''},
    {key: 'AppraisalRule', label: 'Délai de communicabilité', info: ''}
  ];
  previousValue = (): AccessContract => {
    return this._accessContract;
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}' &&
      (this.statusControl.value ? 'ACTIVE' : 'INACTIVE') === this.previousValue().status &&
      (this.accessLogControl.value ? 'ACTIVE' : 'INACTIVE') === this.previousValue().accessLog;

    this.updated.emit(!unchanged);

    return unchanged;
  }

  isInvalid(): boolean {
    return this.form.get('name').invalid || this.form.get('name').pending ||
      this.form.get('description').invalid || this.form.get('description').pending ||
      this.form.get('status').invalid || this.form.get('status').pending ||
      this.form.get('accessLog').invalid || this.form.get('accessLog').pending ||
      (this.ruleFilter.value === false && (this.form.get('ruleCategoryToFilter').invalid || this.form.get('ruleCategoryToFilter').pending));
  }

  prepareSubmit(): Observable<AccessContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({id: this.previousValue().id, identifier: this.previousValue().identifier}, formData)),
      switchMap((formData: { id: string, [key: string]: any }) => {
        // Update the activation and deactivation dates if the contract status has changed before sending the data
        if (formData.status) {
          if (formData.status === 'ACTIVE') {
            formData.activationDate = new Date();
            formData.deactivationDate = '';
          } else {
            formData.status = 'INACTIVE';
            formData.activationDate = '';
            formData.deactivationDate = new Date();
          }
        }
        return this.accessContractService.patch(formData).pipe(catchError(() => of(null)))
    }));
  }

  onSubmit() {
    this.submited = true;
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit().subscribe(() => {
      this.accessContractService.get(this._accessContract.identifier).subscribe(
        response => {
          this.submited = false;
          this.accessContract = response;
          this.resetForm(this.accessContract);
        }
      );
    }, () => {
      this.submited = false;
    });
  }

  resetForm(accessContract: AccessContract) {
    this.statusControl.setValue(accessContract.status === 'ACTIVE');
    this.accessLogControl.setValue(accessContract.accessLog === 'ACTIVE');
    this.form.reset(accessContract, {emitEvent: false});
  }
}
