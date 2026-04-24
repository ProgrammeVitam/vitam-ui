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
import { Component, OnDestroy, inject } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ConfirmDialogService, Customer } from 'vitamui-library';
import { CustomerService } from '../../../../core/customer.service';

@Component({
  selector: 'app-homepage-message-update',
  templateUrl: './homepage-message-update.component.html',
  styleUrls: ['./homepage-message-update.component.scss'],
  standalone: false,
})
export class HomepageMessageUpdateComponent implements OnDestroy {
  dialogRef = inject<MatDialogRef<HomepageMessageUpdateComponent>>(MatDialogRef);
  data = inject<{
    customer: Customer;
  }>(MAT_DIALOG_DATA);
  private customerService = inject(CustomerService);
  private confirmDialogService = inject(ConfirmDialogService);

  private destroy = new Subject<void>();

  private _customForm: FormGroup;
  public get customForm(): FormGroup {
    return this._customForm;
  }
  public set customForm(form: FormGroup) {
    this._customForm = form;
    this.disabled = !(this._customForm && this._customForm.valid && this.checkValidation(this._customForm.value.translations));
  }

  public disabled = true;

  public portalTitles: {
    [language: string]: string;
  };

  public portalMessages: {
    [language: string]: string;
  };

  ngOnDestroy(): void {
    this.destroy.next();
  }

  onCancel() {
    if (this.customForm.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  private checkValidation(forms: FormGroup[]): boolean {
    let isValid = true;
    forms.forEach((x) => {
      if (!x.valid) {
        isValid = false;
      }
    });
    return isValid;
  }

  public updateHomepageMessage(): void {
    if (this.customForm.valid && this.checkValidation(this.customForm.value.translations)) {
      const form = {
        ...{
          id: this.customForm.get('id').value,
          portalTitles: this.portalTitles,
          portalMessages: this.portalMessages,
        },
      };

      this.customerService
        .patch(form)
        .pipe(takeUntil(this.destroy))
        .subscribe(
          () => {
            this.dialogRef.close(true);
          },
          (error: any) => {
            this.dialogRef.close(false);
            console.error(error);
          },
        );
    }
  }
}
