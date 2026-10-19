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
import {
  ConfirmDialogService,
  DialogHeaderComponent,
  InputComponent,
  Owner,
  requiredValue,
  SelectComponent,
  Tenant,
} from 'vitamui-library';

import { Component, inject, OnDestroy, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { form, FormField, FormRoot, required } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';

import { finalize, Subscription } from 'rxjs';
import { TenantService } from '../tenant.service';
import { TenantFormValidators } from './tenant-form.validators';
import { TranslatePipe } from '@ngx-translate/core';

interface TenantCreateModel {
  ownerId: string;
  customerId: string;
  name: string;
  identifier: number;
  enabled: boolean;
}

@Component({
  selector: 'app-tenant-create',
  templateUrl: './tenant-create.component.html',
  styleUrls: ['./tenant-create.component.scss'],
  imports: [DialogHeaderComponent, FormField, FormRoot, MatDialogContent, InputComponent, SelectComponent, MatDialogActions, TranslatePipe],
})
export class TenantCreateComponent implements OnInit, OnDestroy {
  private readonly cdr = inject(ChangeDetectorRef);
  dialogRef = inject<MatDialogRef<TenantCreateComponent>>(MatDialogRef);
  data = inject<{
    owner: Owner;
  }>(MAT_DIALOG_DATA);
  private readonly tenantService = inject(TenantService);
  private readonly tenantFormValidators = inject(TenantFormValidators);
  private readonly confirmDialogService = inject(ConfirmDialogService);

  private readonly model = signal<TenantCreateModel>({
    ownerId: this.data.owner.id,
    customerId: this.data.owner.customerId,
    name: null,
    identifier: null,
    enabled: true,
  });
  readonly form = form(
    this.model,
    (path) => {
      required(path.name);
      this.tenantFormValidators.uniqueNameRule(path.name);
      required(path.identifier);
      requiredValue(path.enabled);
    },
    { submission: { action: async () => this.onSubmit(), ignoreValidators: 'all' } },
  );

  private keyPressSubscription: Subscription;
  availableTenants: number[];
  readonly isLoading = signal(false);

  ngOnInit() {
    this.tenantService.getAvailableTenants().subscribe((availableTenants) => {
      this.availableTenants = availableTenants.sort((tenantI1: number, tenantI2: number) => tenantI1 - tenantI2);
      // The default tenant was patched in a 'tenantId' control that does not exist in the form: no tenant is selected by default
      this.cdr.markForCheck();
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
    if (this.form().pending() || this.form().invalid()) {
      return;
    }
    this.isLoading.set(true);
    this.tenantService
      .create(this.model() as Tenant, this.data.owner.name)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (newTenant: Tenant) => {
          this.dialogRef.close(newTenant);
        },
        error: (error) => {
          console.error(error);
          this.dialogRef.close(null);
        },
      });
  }
}
