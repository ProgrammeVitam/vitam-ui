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
/* tslint:disable:no-use-before-declare */
import { AfterContentChecked, AfterContentInit, Component, ContentChildren, forwardRef, OnInit, QueryList } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { merge } from 'rxjs';
import { map } from 'rxjs/operators';

import { RoleComponent } from './role.component';

export const ROLE_TOGGLE_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => RoleToggleComponent),
  multi: true
};

@Component({
  selector: 'vitamui-common-role-toggle',
  templateUrl: './role-toggle.component.html',
  styleUrls: ['./role-toggle.component.scss'],
  providers: [ROLE_TOGGLE_VALUE_ACCESSOR],
})
export class RoleToggleComponent implements OnInit, ControlValueAccessor, AfterContentChecked, AfterContentInit {

  @ContentChildren(RoleComponent) roleComponents: QueryList<RoleComponent>;

  onChange: (_: any) => void;
  onTouched: () => void;

  private roles: Array<{ name: string }> = [];

  ngAfterContentInit(): void {
    const toggleChangeList = this.roleComponents.map((roleComponent) => {
      return roleComponent.checkedChange.pipe(map((value) => ({ name: roleComponent.name, value })));
    });
    const toggleChangeMerged = merge(...toggleChangeList);

    toggleChangeMerged.subscribe((role) => {
      if (role.value) {
        if (!this.roles.find((r) => r.name === role.name)) {
          this.roles = this.roles.concat([{ name: role.name }]);
        }
      } else {
        this.roles = this.roles.filter((r) => role.name !== r.name);
      }
      this.onChange(this.roles);
    });

    this.roleComponents.forEach((roleComponent) => {
      roleComponent.checked = !!this.roles.find((role) => role.name === roleComponent.name);
    });
  }

  ngAfterContentChecked(): void {
    // TODO
  }

  writeValue(roles: Array<{ name: string, value: boolean }>): void {
    this.roles = roles || [];
    if (this.roleComponents) {
      this.roleComponents.forEach((roleComponent) => {
        roleComponent.checked = !!this.roles.find((role) => role.name === roleComponent.name);
      });
    }
  }
  registerOnChange(fn: any): void {
    this.onChange = fn;
  }
  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.roleComponents.forEach((roleComponent) => {
      roleComponent.forceDisabled = isDisabled;
    });
  }

  constructor() {

  }

  ngOnInit() {
  }

}
