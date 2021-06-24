import { HttpHeaders, HttpParams } from '@angular/common/http';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Observable, Subscription } from 'rxjs';
import { AccessContractApiService, ApplicationId, ConfirmDialogService, ExternalParamProfile, Option } from 'ui-frontend-common';
import { ExternalParamProfileService } from '../external-param-profile.service';
import {ExternalParamProfileValidators} from "../external-param-profile.validators";

@Component({
  selector: 'app-external-param-profile-create',
  templateUrl: './external-param-profile-create.component.html',
  styleUrls: ['./external-param-profile-create.component.css'],
})
export class ExternalParamProfileCreateComponent implements OnInit, OnDestroy {
  externalParamProfileForm: FormGroup;
  accessContracts: Option[] = [];
  accessContracts$: Observable<any[]>;
  private keyPressSubscription: Subscription;
  tenantIdentifier: string;

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<ExternalParamProfileCreateComponent>,
    private accessContractApiService: AccessContractApiService,
    private externalParamProfileServiceService: ExternalParamProfileService,
    private externalParamProfileValidators: ExternalParamProfileValidators,
    private confirmDialogService: ConfirmDialogService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  ngOnInit(): void {
    this.initForm(this.data.tenantIdentifier);
    this.tenantIdentifier = this.data.tenantIdentifier;
    this.accessContracts$ = this.getAllAccessContracts();
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  private initForm(tenantIdentifier: string) {
    this.externalParamProfileForm = this.formBuilder.group({
      enabled: true,
      accessContract: [null, Validators.required],
      description: [null, Validators.required],
      name: [null, Validators.required],
    });

    this.externalParamProfileForm
      .get('name')
      .setAsyncValidators(
        this.externalParamProfileValidators.nameExists(+tenantIdentifier, ApplicationId.EXTERNAL_PARAM_PROFILE_APP)
      );
  }

  onSubmit() {
    if (this.externalParamProfileForm.invalid) {
      return;
    }
    const externalParamProfile: ExternalParamProfile = this.externalParamProfileForm.getRawValue();
    this.externalParamProfileServiceService.create(externalParamProfile).subscribe(
      (response: ExternalParamProfile) => {
        console.log('response = ', response);
        this.dialogRef.close(true);
      },
      (error: any) => {
        console.error(error);
      }
    );
  }

  private getAllAccessContracts(): Observable<any[]> {
    const params = new HttpParams();
    const headers = new HttpHeaders().append('X-Tenant-Id', this.tenantIdentifier);
    return this.accessContractApiService.getAllAccessContracts(params, headers);
  }

  onCancel() {
    if (this.externalParamProfileForm.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onValidate() {
    return false;
  }

}
