import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ConfirmDialogService, Customer } from 'vitamui-library';
import { CustomerService } from '../../../../core/customer.service';

@Component({
  selector: 'app-homepage-message-update',
  templateUrl: './homepage-message-update.component.html',
  styleUrls: ['./homepage-message-update.component.scss'],
})
export class HomepageMessageUpdateComponent implements OnInit, OnDestroy {
  private destroy = new Subject<void>();

  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
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

  constructor(
    public dialogRef: MatDialogRef<HomepageMessageUpdateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { customer: Customer },
    private customerService: CustomerService,
    private confirmDialogService: ConfirmDialogService,
  ) {}

  ngOnDestroy(): void {
    this.destroy.next();
  }
  ngOnInit() {}

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
