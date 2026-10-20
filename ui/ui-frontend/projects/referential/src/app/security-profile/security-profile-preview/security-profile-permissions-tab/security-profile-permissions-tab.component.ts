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
import { Component, inject, signal, output, input, model, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { diff, Option, SecurityProfile } from 'vitamui-library';
import { SecurityProfileService } from '../../security-profile.service';
import { SecurityProfileEditPermissionComponent } from '../../security-profile-create/security-profile-edit-permission/security-profile-edit-permission.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-security-profile-permissions-tab',
  templateUrl: './security-profile-permissions-tab.component.html',
  styleUrls: ['./security-profile-permissions-tab.component.scss'],
  imports: [ReactiveFormsModule, SecurityProfileEditPermissionComponent, TranslatePipe],
})
export class SecurityProfilePermissionsTabComponent implements OnChanges {
  private readonly formBuilder = inject(FormBuilder);
  private readonly SecurityProfileService = inject(SecurityProfileService);

  readonly updated = output<boolean>();

  form: FormGroup;

  submited = signal(false);

  ruleFilter = new FormControl();

  // FIXME: Get list from common var ?
  rules: Option[] = [
    { key: 'StorageRule', label: `Durée d'utilité courante`, info: '' },
    { key: 'ReuseRule', label: 'Durée de réutilisation', info: '' },
    { key: 'ClassificationRule', label: 'Durée de classification', info: '' },
    { key: 'DisseminationRule', label: 'Délai de diffusion', info: '' },
    { key: 'AdministrationRule', label: `Durée d'utilité administrative`, info: '' },
    { key: 'AppraisalRule', label: 'Délai de communicabilité', info: '' },
  ];

  previousValue = (): SecurityProfile => {
    return this.SecurityProfile();
  };

  readonly SecurityProfile = model<SecurityProfile>(undefined);

  readonly readOnly = input<boolean>(undefined);

  constructor() {
    this.form = this.formBuilder.group({
      fullAccess: [null],
      permissions: [null],
    });

    this.ruleFilter.valueChanges.subscribe((val) => {
      if (val === true) {
        this.form.controls['ruleCategoryToFilter'].setValue(new Array<string>());
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['SecurityProfile']) {
      this.setSecurityProfile(this.SecurityProfile());
    }
    if (changes['readOnly']) {
      const readOnly = this.readOnly();
      if (readOnly && this.form.enabled) {
        this.form.disable({ emitEvent: false });
      } else if (this.form.disabled) {
        this.form.enable({ emitEvent: false });
        this.form.get('identifier').disable({ emitEvent: false });
      }
    }
  }

  unchanged(): boolean {
    let unchanged = true;

    if (this.form.getRawValue().permissions && this.previousValue().permissions) {
      if (this.form.getRawValue().permissions.length !== this.previousValue().permissions.length) {
        unchanged = false;
      } else {
        const previousPermissions = this.previousValue().permissions;
        const diff = this.form.getRawValue().permissions.filter((permission: string) => {
          return previousPermissions.indexOf(permission) === -1;
        });
        if (diff.length > 0) {
          unchanged = false;
        }
      }
    } else if (
      (!this.form.getRawValue().permissions && this.previousValue().permissions && this.previousValue().permissions.length > 0) ||
      (this.form.getRawValue().permissions && this.form.getRawValue().permissions.length > 0 && !this.previousValue().permissions)
    ) {
      unchanged = false;
    }

    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    return false;
  }

  prepareSubmit(): Observable<SecurityProfile> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) =>
        this.SecurityProfileService.patch(formData).pipe(catchError(() => of(null))),
      ),
    );
  }

  onSubmit() {
    this.submited.set(true);
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit().subscribe(
      () => {
        this.SecurityProfileService.get(this.SecurityProfile().identifier).subscribe((response) => {
          this.submited.set(false);
          this.setSecurityProfile(response);
        });
      },
      () => {
        this.submited.set(false);
      },
    );
  }

  setSecurityProfile(SecurityProfile: SecurityProfile) {
    if (!SecurityProfile.permissions) {
      SecurityProfile.permissions = [];
    }

    this.SecurityProfile.set(SecurityProfile);
    this.resetForm(this.SecurityProfile());
  }

  resetForm(SecurityProfile: SecurityProfile) {
    this.form.reset(SecurityProfile, { emitEvent: false });
  }
}
