import { Component, Inject, Input, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { ConfirmDialogService, Customer } from 'ui-frontend-common';

@Component({
  selector: 'app-homepage-message-update',
  templateUrl: './homepage-message-update.component.html',
  styleUrls: ['./homepage-message-update.component.scss']
})

export class HomepageMessageUpdateComponent implements OnInit, OnDestroy {

  @Input()
  public homepageMessageForm: FormGroup;

  private keyPressSubscription: Subscription;

  constructor(
    public dialogRef: MatDialogRef<HomepageMessageUpdateComponent>,
    private formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: { customer: Customer},
    private confirmDialogService: ConfirmDialogService
  ) {}

  ngOnDestroy(): void {
    this.keyPressSubscription.unsubscribe();
  }

  ngOnInit() {
    this.homepageMessageForm = this.formBuilder.group({
      portalTitle: ['', [Validators.required]],
      portalMessage: ['', [Validators.required, Validators.maxLength(500)]],
    });
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  onCancel() {
    if (this.homepageMessageForm.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }
}
