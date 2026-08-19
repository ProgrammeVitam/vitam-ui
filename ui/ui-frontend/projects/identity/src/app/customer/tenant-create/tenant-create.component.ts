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
import { ConfirmDialogService, DialogHeaderComponent, InputComponent, Owner, SelectComponent, Tenant } from 'vitamui-library';

import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';

import { finalize, Subscription } from 'rxjs';
import { TenantService } from '../tenant.service';
import { TenantFormValidators } from './tenant-form.validators';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-tenant-create',
  templateUrl: './tenant-create.component.html',
  styleUrls: ['./tenant-create.component.scss'],
  imports: [DialogHeaderComponent, ReactiveFormsModule, MatDialogContent, InputComponent, SelectComponent, MatDialogActions, TranslatePipe],
})
export class TenantCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<TenantCreateComponent>>(MatDialogRef);
  data = inject<{
    owner: Owner;
  }>(MAT_DIALOG_DATA);
  private formBuilder = inject(FormBuilder);
  private tenantService = inject(TenantService);
  private tenantFormValidators = inject(TenantFormValidators);
  private confirmDialogService = inject(ConfirmDialogService);

  form: FormGroup;

  private keyPressSubscription: Subscription;
  availableTenants: number[];
  isLoading = false;

  ngOnInit() {
    this.form = this.formBuilder.group({
      ownerId: [this.data.owner.id],
      customerId: [this.data.owner.customerId],
      name: [null, [Validators.required], this.tenantFormValidators.uniqueName()],
      identifier: [null, [Validators.required]],
      enabled: [true, Validators.required],
    });
    this.tenantService.getAvailableTenants().subscribe((availableTenants) => {
      this.availableTenants = availableTenants.sort((tenantI1: number, tenantI2: number) => tenantI1 - tenantI2);
      if (this.availableTenants && this.availableTenants.length > 0) {
        const tenantIdToSelect =
          this.availableTenants[0] === 0 && this.availableTenants.length > 1 ? this.availableTenants[1] : this.availableTenants[0];
        // To select as default tenant the first one other than 0
        this.form.patchValue({ tenantId: tenantIdToSelect });
      }
    });
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.form.pending || this.form.invalid) {
      return;
    }
    this.isLoading = true;
    this.tenantService
      .create(this.form.value, this.data.owner.name)
      .pipe(finalize(() => (this.isLoading = false)))
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
