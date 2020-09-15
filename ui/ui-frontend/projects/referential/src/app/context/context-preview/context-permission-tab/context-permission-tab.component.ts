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
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Context, ContextPermission } from 'projects/vitamui-library/src/public-api';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { diff } from 'ui-frontend-common';
import { extend, isEmpty } from 'underscore';

import { ContextCreateValidators } from '../../context-create/context-create.validators';
import { ContextService } from '../../context.service';


@Component({
  selector: 'app-context-permission-tab',
  templateUrl: './context-permission-tab.component.html',
  styleUrls: ['./context-permission-tab.component.scss']
})
export class ContextPermissionTabComponent {

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;

  submited = false;
  isPermissionsOnMultipleOrganisations = false;

  // tslint:disable-next-line:variable-name
  private _context: Context;

  previousValue = (): Context => {
    return this._context;
  }

  @Input()
  set context(context: Context) {
    if (!context.permissions) {
      context.permissions = [];
    }

    for (const permission of context.permissions) {
      if (!permission.accessContracts) {
        permission.accessContracts = [];
      }
      if (!permission.ingestContracts) {
        permission.ingestContracts = [];
      }
    }

    this._context = context;
    this.resetForm(this.context);
    this.updated.emit(false);
  }
  get context(): Context { return this._context; }

  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({ emitEvent: false });
    } else if (this.form.disabled) {
      this.form.enable({ emitEvent: false });
      this.form.get('identifier').disable({ emitEvent: false });
    }
  }

  constructor(
    private formBuilder: FormBuilder,
    private contextService: ContextService,
    private contextCreateValidators: ContextCreateValidators
  ) {
    this.form = this.formBuilder.group({
      permissions: [[], Validators.required, this.contextCreateValidators.permissionInvalid()]
    });
  }

  sameArray(a: string[], b: string[]): boolean {
    if (!a && !b) {
      return true;
    }
    if (!a || !b) {
      return false;
    }

    if (a.filter(item => b.indexOf(item) < 0).length > 0) {
      return false;
    }

    if (b.filter(item => a.indexOf(item) < 0).length > 0) {
      return false;
    }

    return true;
  }

  samePermission(p1: ContextPermission[], p2: ContextPermission[]): boolean {
    if (!p1 && !p2) { return true; }
    if (!p1 || !p2) { return false; }
    if (p1.length !== p2.length) {
      return false;
    }

    for (let i = 0; i < p1.length; i++) {
      if (p1[i].tenant !== p2[i].tenant) {
        return false;
      }
      if (!this.sameArray(p1[i].accessContracts, p2[i].accessContracts)) {
        return false;
      }
      if (!this.sameArray(p1[i].ingestContracts, p2[i].ingestContracts)) {
        return false;
      }
    }

    return true;
  }

  isInvalidOrUnchanged(): boolean {
    const unchanged = this.samePermission(this.previousValue().permissions, this.form.getRawValue().permissions);
    this.updated.emit(!unchanged);

    if (this.isInvalid()) {
      return true;
    }

    return unchanged;
  }

  isInvalid(): boolean {
    return this.form.controls.permissions.invalid;
  }

  prepareSubmit(): Observable<Context> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
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
    }, () => {
      this.submited = false;
    });
  }

  resetForm(context: Context) {
    this.form.reset(context, { emitEvent: false });
    const permissionCopy: ContextPermission[] = [];
    for (const permission of context.permissions) {
      permissionCopy.push(new ContextPermission(permission.tenant,
        [...(permission.accessContracts ? permission.accessContracts : [])],
        [...(permission.ingestContracts ? permission.ingestContracts : [])]));
    }
    this.form.controls.permissions.setValue(permissionCopy);
  }

  onChangeOrganisations(organisations: string[]) {
    this.isPermissionsOnMultipleOrganisations = false;
    if (organisations && organisations.length > 1) {
      let idx = 0;
      let organisationId: string = null;
      while (idx < organisations.length && !this.isPermissionsOnMultipleOrganisations) {
        if (idx === 0) {
          organisationId = organisations[0];
        } else if (organisations[idx] != null && organisations[idx] !== organisationId) {
          this.isPermissionsOnMultipleOrganisations = true;
        }
        idx++;
      }
    }
  }
}
