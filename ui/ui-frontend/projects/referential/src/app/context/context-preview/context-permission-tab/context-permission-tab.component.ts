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
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Context } from 'projects/vitamui-library/src/public-api';

import { ContextService } from '../../context.service';
import { map, filter, catchError, switchMap } from 'rxjs/operators';
import { diff, Option } from 'ui-frontend-common';
import { extend, isEmpty } from 'underscore';

import {Observable, of} from 'rxjs';

@Component({
  selector: 'app-context-permission-tab',
  templateUrl: './context-permission-tab.component.html',
  styleUrls: ['./context-permission-tab.component.scss']
})
export class ContextPermissionTabComponent {

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;
  previousValue = (): Context => {
    return this._context
  };

  submited: boolean = false;

  @Input()
  set context(context: Context) {
    this._context = context;
    this.resetForm(this.context);
    this.updated.emit(false);
  }
  get context(): Context { return this._context; }
  // tslint:disable-next-line:variable-name
  private _context: Context;

  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({ emitEvent: false });
    } else if (this.form.disabled) {
      this.form.enable({ emitEvent: false });
      this.form.get('identifier').disable({ emitEvent: false });
    }
  }

  // FIXME: Get list from common var ?
  rules: Option[] = [
    { key: 'StorageRule', label: 'Durée d\'utilité courante', info: '' },
    { key: 'ReuseRule', label: 'Durée de réutilisation', info: '' },
    { key: 'ClassificationRule', label: 'Durée de classification', info: '' },
    { key: 'DisseminationRule', label: 'Délai de diffusion', info: '' },
    { key: 'AdministrationRule', label: 'Durée d\'utilité administrative', info: '' },
    { key: 'AppraisalRule', label: 'Délai de communicabilité', info: '' }
  ];

  constructor(
    private formBuilder: FormBuilder,
    private contextService: ContextService
  ) {
    this.form = this.formBuilder.group({
      permissions: [null, Validators.required]
    });

  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === "{}";
    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    return false;
  }

  prepareSubmit(): Observable<Context> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({id: this.previousValue().id, identifier: this.previousValue().identifier}, formData)),
      switchMap((formData: { id: string, [key: string]: any }) => this.contextService.patch(formData).pipe(catchError(() => of(null)))));
  }

  onSubmit() {
    this.submited = true;
    if (this.isInvalid()) { return; }
    this.prepareSubmit().subscribe(() => {
      this.contextService.get(this._context.identifier).subscribe(
        response => {
          this.submited = false;
          this.context = response;
        }
      );
    },() => {
      this.submited = false;
    });
  }

  resetForm(context: Context) {
    this.form.reset(context, { emitEvent: false });
  }
}
