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
/* tslint:disable: no-use-before-declare */

import {Component, forwardRef, Input} from '@angular/core';
import {ControlValueAccessor, FormBuilder, FormGroup, NG_VALUE_ACCESSOR} from '@angular/forms';
import {PermissionStructure, PermissionUtils} from '../permission.utils';

export const PERMISSION_SELECT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => SecurityProfileEditPermissionComponent),
  multi: true
};

@Component({
  selector: 'app-security-profile-edit-permission',
  templateUrl: './security-profile-edit-permission.component.html',
  styleUrls: ['./security-profile-edit-permission.component.scss'],
  providers: [PERMISSION_SELECT_VALUE_ACCESSOR]
})
export class SecurityProfileEditPermissionComponent implements ControlValueAccessor {
  permissions: PermissionStructure;
  loaded = false;
  form: FormGroup;

  getPermissionString = this.permissionUtils.getPermissionString;

  disabled: boolean;

  @Input() small: boolean;
  @Input() forceDisabled: boolean;

  // tslint:disable-next-line:variable-name
  onChange = (_x: any) => {
  }
  onTouched = () => {
  }

  constructor(private permissionUtils: PermissionUtils, private formBuilder: FormBuilder) {

  }

  onSubmit() {
    this.onChange(this.getAllowedPermissions());
  }

  getAllowedPermissions() {
    const allowedPermissions: string[] = [];
    for (const permission in this.form.value) {
      if (this.form.value.hasOwnProperty(permission) && this.form.value[permission] === true) {
        allowedPermissions.push(permission);
      }
    }
    return allowedPermissions;
  }

  getDefaultPermissions() {
    this.permissions = this.permissionUtils.getInitPermissions(true);
    const formConfig = this.permissionUtils.getFormConfigFromPermission(this.permissions);
    this.form = this.formBuilder.group(formConfig);
  }

  initPermissions(initialPermissions: string[]) {
    this.permissions = this.permissionUtils.getPermissionsFromList(initialPermissions);
    const formConfig = this.permissionUtils.getFormConfigFromPermission(this.permissions);
    this.form = this.formBuilder.group(formConfig);
  }

  writeValue(value: any) {
    if (value === null) {
      this.getDefaultPermissions();
    } else {
      this.initPermissions(value);
    }
    this.loaded = true;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
    this.onChange(this.getAllowedPermissions());
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean) {
    this.disabled = isDisabled;
  }

}
