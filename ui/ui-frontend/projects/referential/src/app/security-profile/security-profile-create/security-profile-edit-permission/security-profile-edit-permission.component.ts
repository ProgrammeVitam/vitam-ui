/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { Component, computed, inject, input, linkedSignal, model, OnInit } from '@angular/core';
import { disabled, form, FormField, FormValueControl } from '@angular/forms/signals';
import { PermissionStructure, PermissionUtils } from '../permission.utils';
import { SlideToggleComponent, TooltipDirective } from 'vitamui-library';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-security-profile-edit-permission',
  templateUrl: './security-profile-edit-permission.component.html',
  styleUrls: ['./security-profile-edit-permission.component.scss'],
  imports: [FormField, TooltipDirective, SlideToggleComponent, TranslatePipe],
})
export class SecurityProfileEditPermissionComponent implements FormValueControl<string[] | null>, OnInit {
  private readonly permissionUtils = inject(PermissionUtils);

  // Allowed permissions, bound to the signal forms field of the parent form with [formField]. All permissions are allowed when null.
  readonly value = model<string[] | null>(null);

  readonly forceDisabled = input<boolean>(undefined);

  readonly permissions = computed<PermissionStructure>(() => {
    const value = this.value();
    return value === null ? this.permissionUtils.getInitPermissions(true) : this.permissionUtils.getPermissionsFromList(value);
  });

  // Activation state of each permission, by permission string
  private readonly permissionsModel = linkedSignal<Record<string, boolean>>(() =>
    this.permissionUtils.getFormConfigFromPermission(this.permissions()),
  );
  protected readonly permissionsForm = form(this.permissionsModel, (path) => {
    disabled(path, () => !!this.forceDisabled());
  });

  getPermissionString = this.permissionUtils.getPermissionString;

  ngOnInit() {
    // Normalizes the bound value, as the former ControlValueAccessor did when registered
    this.onSubmit();
  }

  onSubmit() {
    this.value.set(this.getAllowedPermissions());
  }

  getAllowedPermissions() {
    const values = this.permissionsModel();
    return Object.keys(values).filter((permission) => values[permission] === true);
  }
}
