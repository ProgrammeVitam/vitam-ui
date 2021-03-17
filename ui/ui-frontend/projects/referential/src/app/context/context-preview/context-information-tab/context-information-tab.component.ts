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
import {Context} from 'projects/vitamui-library/src/public-api';
import {Observable, of} from 'rxjs';
import {catchError, filter, map, switchMap} from 'rxjs/operators';
import {diff, Option} from 'ui-frontend-common';
import {extend, isEmpty} from 'underscore';

import {ContextService} from '../../context.service';

import {SecurityProfileService} from '../../../security-profile/security-profile.service';

@Component({
  selector: 'app-context-information-tab',
  templateUrl: './context-information-tab.component.html',
  styleUrls: ['./context-information-tab.component.scss']
})
export class ContextInformationTabComponent {

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;

  submited = false;

  statusControl = new FormControl();

  securityProfiles: Option[] = [];

  // tslint:disable-next-line:variable-name
  private _context: Context;


  // FIXME: Get list from common var ?
  rules: Option[] = [
    {key: 'StorageRule', label: 'Durée d\'utilité courante', info: ''},
    {key: 'ReuseRule', label: 'Durée de réutilisation', info: ''},
    {key: 'ClassificationRule', label: 'Durée de classification', info: ''},
    {key: 'DisseminationRule', label: 'Délai de diffusion', info: ''},
    {key: 'AdministrationRule', label: 'Durée d\'utilité administrative', info: ''},
    {key: 'AppraisalRule', label: 'Délai de communicabilité', info: ''}
  ];

  previousValue = (): Context => {
    return this._context;
  }

  @Input()
  // tslint:disable-next-line:no-shadowed-variable
  set context(Context: Context) {
    this._context = Context;
    this.resetForm(this.context);
    this.updated.emit(false);
  }

  get context(): Context {
    return this._context;
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
    private contextService: ContextService,
    private securityProfileService: SecurityProfileService
  ) {
    this.form = this.formBuilder.group({
      name: [null],
      status: [null, Validators.required],
      securityProfile: [null, Validators.required],
      enableControl: [null, Validators.required]
    });

    this.securityProfileService.getAll().subscribe(
      securityProfiles => {
        this.securityProfiles = securityProfiles.map(x => ({label: x.name, key: x.identifier}));
      });

    this.statusControl.valueChanges.subscribe((value) => {
      this.form.controls.status.setValue(value = (value === false) ? 'INACTIVE' : 'ACTIVE');
    });
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    return false;
  }

  getEnableControl(): boolean {
    return this.form.getRawValue().enableControl;
  }

  prepareSubmit(): Observable<Context> {
    let diffValue = diff(this.form.getRawValue(), this.previousValue());
    if (!diffValue.enableControl) {
      diffValue.permissions = [];
    }

    return of(diffValue).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({id: this.previousValue().id, identifier: this.previousValue().identifier}, formData)),
      switchMap((formData: { id: string, [key: string]: any }) => {
        // Update the activation and deactivation dates if the context status has changed before sending the data
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
        return this.contextService.patch(formData).pipe(catchError(() => of(null)));
      }));
  }

  onSubmit() {
    this.submited = true;
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit().subscribe(() => {
      this.contextService.get(this._context.identifier).subscribe(
        response => {
          this.submited = false;
          this.context = response;
        }
      );
    }, () => {
      this.submited = false;
    });
  }

  resetForm(context: Context) {
    this.statusControl.setValue(context.status === 'ACTIVE');
    this.form.reset(context, {emitEvent: false});
  }
}
