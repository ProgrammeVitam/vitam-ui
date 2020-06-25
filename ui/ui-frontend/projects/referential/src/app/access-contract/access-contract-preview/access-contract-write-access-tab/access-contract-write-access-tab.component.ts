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
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {AccessContract} from 'projects/vitamui-library/src/public-api';
import {Observable, of} from 'rxjs';
import {catchError, filter, map, switchMap} from 'rxjs/operators';
import {diff} from 'ui-frontend-common';
import {extend, isEmpty} from 'underscore';

import {AccessContractService} from '../../access-contract.service';

@Component({
  selector: 'app-access-contract-write-access-tab',
  templateUrl: './access-contract-write-access-tab.component.html',
  styleUrls: ['./access-contract-write-access-tab.component.scss']
})
export class AccessContractWriteAccessTabComponent implements OnInit {

  @Input()
  set accessContract(accessContract: AccessContract) {
    this._accessContract = accessContract;

    if (!accessContract.writingPermission) {
      accessContract.writingPermission = false;
      accessContract.writingRestrictedDesc = null;
    } else if (!accessContract.writingRestrictedDesc) {
      accessContract.writingRestrictedDesc = false;
    }

    this.resetForm(this.accessContract);
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

  constructor(private formBuilder: FormBuilder, private accessContractService: AccessContractService) {
    this.form = this.formBuilder.group({
      writingRestrictedDesc: [true],
      writingPermission: [false]
    });
  }

  form: FormGroup;
  submited = false;

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  // tslint:disable-next-line:variable-name
  private _accessContract: AccessContract;
  previousValue = (): AccessContract => {
    return this._accessContract;
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';

    this.updated.emit(!unchanged);

    return unchanged;
  }

  prepareSubmit(): Observable<AccessContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({id: this.previousValue().id, identifier: this.previousValue().identifier}, formData)),
      switchMap(
        (formData: { id: string, [key: string]: any }) => this.accessContractService.patch(formData).pipe(catchError(() => of(null)))));
  }

  onSubmit() {
    this.submited = true;
    // if (this.isInvalid()) { return; }
    this.prepareSubmit().subscribe(() => {
      this.accessContractService.get(this._accessContract.identifier).subscribe(
        response => {
          this.submited = false;
          this.accessContract = response;
        }
      );
    }, () => {
      this.submited = false;
    });
  }

  ngOnInit() {

  }

  resetForm(accessContract: AccessContract) {
    this.form.reset(accessContract, {emitEvent: false});
  }
}
