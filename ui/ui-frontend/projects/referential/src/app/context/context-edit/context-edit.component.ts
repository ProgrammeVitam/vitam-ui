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
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ContextPermission } from 'projects/vitamui-library/src/public-api';
import { Subscription } from 'rxjs';
import { ConfirmDialogService } from 'ui-frontend-common';
import { ContextCreateValidators } from '../context-create/context-create.validators';

const PROGRESS_BAR_MULTIPLICATOR = 100;

@Component({
  selector: 'app-context-edit',
  templateUrl: './context-edit.component.html',
  styleUrls: ['./context-edit.component.scss']
})

export class ContextEditComponent implements OnInit, OnDestroy {

  form: FormGroup;
  stepIndex = 0;
  hasError = true;
  message: string;
  isPermissionsOnMultipleOrganisations = false;

  // stepCount is the total number of steps and is used to calculate the advancement of the progress bar.
  // We could get the number of steps using ViewChildren(StepComponent) but this triggers a
  // "Expression has changed after it was checked" error so we instead manually define the value.
  // Make sure to update this value whenever you add or remove a step from the  template.
  private stepCount = 1;
  private keyPressSubscription: Subscription;

  private permissions: ContextPermission[];

  constructor(
    public dialogRef: MatDialogRef<ContextEditComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ContextPermission[],
    private formBuilder: FormBuilder,
    private confirmDialogService: ConfirmDialogService,
    private contextCreateValidators: ContextCreateValidators,
  ) {
    this.permissions = data;

    let permissions: any[] = new Array();
    if (this.permissions) {
      permissions = this.permissions.map(permission => {
        return {
          tenant: permission.tenant,
          accessContracts: permission.accessContracts,
          ingestContracts: permission.ingestContracts
        };
      });
    }

    this.form = this.formBuilder.group({
      permissions: [permissions, null, this.contextCreateValidators.permissionInvalid()]
    });
  }

  ngOnInit() {
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

  isPermissionsInvalid(): boolean {
    return this.form.get('permissions').invalid;
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

  onSubmit() {
    if (this.form.invalid) {
      return;
    }

    // Update the context permissions
    this.permissions = this.form.value.permissions.map((item:
      { tenant: string; accessContracts: string[]; ingestContracts: string[]; }) =>
      new ContextPermission('' + item.tenant, item.accessContracts, item.ingestContracts)
    );

    this.dialogRef.close({ permissions: this.permissions });
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }
}
