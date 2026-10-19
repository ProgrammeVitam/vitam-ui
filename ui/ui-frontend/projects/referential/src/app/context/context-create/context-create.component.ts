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
import { Component, effect, inject, OnDestroy, OnInit, signal, untracked, viewChild } from '@angular/core';
import { form, FormField, FormRoot, maxLength, minLength, required } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import {
  allowedIdentifier,
  ConfirmDialogService,
  Context,
  ContextPermission,
  Option,
  VitamUICommonModule,
  VitamUILibraryModule,
} from 'vitamui-library';
import { SecurityProfileService } from '../../security-profile/security-profile.service';
import { ContextService } from '../context.service';
import { ContextCreateValidators } from './context-create.validators';

import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { ContextEditPermissionComponent } from './context-edit-permission/context-edit-permission.component';
import { TranslatePipe } from '@ngx-translate/core';

interface ContextCreateForm {
  status: string;
  name: string;
  identifier: string;
  securityProfile: string | null;
  enableControl: boolean;
  permissions: ContextPermission[];
}

@Component({
  selector: 'app-context-create',
  templateUrl: './context-create.component.html',
  styleUrls: ['./context-create.component.scss'],
  imports: [
    MatButtonToggleModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    FormRoot,
    FormField,
    ContextEditPermissionComponent,
    VitamUICommonModule,
    VitamUILibraryModule,
    TranslatePipe,
  ],
})
export class ContextCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<ContextCreateComponent>>(MatDialogRef);
  data = inject<{
    isSlaveMode: boolean;
  }>(MAT_DIALOG_DATA);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly contextService = inject(ContextService);
  private readonly contextCreateValidators = inject(ContextCreateValidators);
  private readonly securityProfileService = inject(SecurityProfileService);

  protected readonly isSlaveMode: boolean;

  readonly form = form(
    signal<ContextCreateForm>({
      status: 'INACTIVE',
      name: '',
      identifier: '',
      securityProfile: null,
      enableControl: false,
      permissions: [],
    }),
    (path) => {
      required(path.name);
      minLength(path.name, 2);
      maxLength(path.name, 100);
      this.contextCreateValidators.allowedName(path.name);
      this.contextCreateValidators.uniqueName(path.name);
      required(path.identifier);
      minLength(path.identifier, 2);
      maxLength(path.identifier, 100);
      allowedIdentifier(path.identifier);
      this.contextCreateValidators.uniqueIdentifier(path.identifier);
      required(path.securityProfile);
      // Controls on the permissions are only enabled with enableControl
      this.contextCreateValidators.permissionInvalid(path.permissions, ({ valueOf }) => valueOf(path.enableControl));
    },
    {
      submission: {
        action: async () => {
          this.onSubmit();
        },
        ignoreValidators: 'all',
      },
    },
  );
  hasCustomGraphicIdentity = false;
  hasError = true;
  message: string;
  isPermissionsOnMultipleOrganisations = false;

  private keyPressSubscription: Subscription;
  isDisabledButton = signal(false);

  readonly fileSearch = viewChild<any>('fileSearch');

  securityProfiles: Option[] = [];

  constructor() {
    const data = this.data;

    this.isSlaveMode = data.isSlaveMode;

    effect(() => {
      const name = this.form.name().value();
      if (!this.isSlaveMode) {
        untracked(() => this.form.identifier().value.set(name));
      }
    });
  }

  ngOnInit() {
    this.securityProfileService.getAll().subscribe((securityProfiles) => {
      this.securityProfiles = securityProfiles.map((x) => ({ label: x.name, key: x.identifier }));
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.form().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.form().invalid()) {
      this.isDisabledButton.set(true);
      return;
    }
    this.isDisabledButton.set(true);
    const context = { ...this.form().value() } as unknown as Context;
    context.status === 'ACTIVE'
      ? (context['activationDate'] = new Date().toISOString())
      : (context['deactivationDate'] = new Date().toISOString());
    this.contextService.create(context).subscribe(
      () => {
        this.isDisabledButton.set(false);
        this.dialogRef.close({ success: true, action: 'none' });
      },
      () => {
        this.isDisabledButton.set(false);
      },
    );
  }

  firstStepInvalid() {
    return [this.form.name, this.form.status, this.form.enableControl, this.form.securityProfile].some(
      (field) => field().invalid() || field().pending(),
    );
  }

  lastStepInvalid() {
    return this.form.permissions().invalid();
  }

  hasPermissionsError(kind: string): boolean {
    return this.form
      .permissions()
      .errors()
      .some((error) => error.kind === kind);
  }

  onStatusChange(active: boolean) {
    this.form.status().value.set(active ? 'ACTIVE' : 'INACTIVE');
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
