import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ConfirmDialogService, Customer } from 'ui-frontend-common';
import { CustomerService } from '../../../../core/customer.service';

@Component({
  selector: 'app-homepage-message-update',
  templateUrl: './homepage-message-update.component.html',
  styleUrls: ['./homepage-message-update.component.scss']
})

export class HomepageMessageUpdateComponent implements OnInit, OnDestroy {

  private destroy = new Subject();

  // tslint:disable-next-line: variable-name
  private _customForm: FormGroup;
  public get customForm(): FormGroup { return this._customForm; }
  public set customForm(form: FormGroup) {
    this._customForm = form;
    this.disabled = !(this._customForm && this._customForm.valid && this._customForm.value.isFormValid);
  }

  public disabled = true;

  constructor(
    public dialogRef: MatDialogRef<HomepageMessageUpdateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { customer: Customer },
    private customerService: CustomerService,
    private confirmDialogService: ConfirmDialogService
  ) {}

  ngOnDestroy(): void {
    this.destroy.next();
  }
  ngOnInit() {
  }

  onCancel() {
    console.log(this.customForm);
    if (this.customForm.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  public updateHomepageMessage(): void {
    if (this.customForm.valid) {
      console.log(this.customForm.value);
      this.customerService.patch(this.customForm.value)
      .pipe(takeUntil(this.destroy))
      .subscribe(
        () => {
          this.dialogRef.close(true);
        },
        (error: any) => {
          this.dialogRef.close(false);
          console.error(error);
        });
    }
  }
}
