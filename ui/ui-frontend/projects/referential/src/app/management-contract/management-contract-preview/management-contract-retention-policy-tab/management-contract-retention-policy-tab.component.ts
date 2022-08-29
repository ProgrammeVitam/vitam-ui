import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {Observable, of, Subscription} from "rxjs";
import {ManagementContract} from "../../../../../../vitamui-library/src/lib/models/management-contract";
import {ManagementContractService} from "../../management-contract.service";
import {diff} from "ui-frontend-common";
import {catchError, filter, map, switchMap} from "rxjs/operators";
import {extend, isEmpty} from "underscore";

@Component({
  selector: 'app-management-contract-retention-policy-tab',
  templateUrl: './management-contract-retention-policy-tab.component.html',
  styleUrls: ['./management-contract-retention-policy-tab.component.scss']
})
export class ManagementContractRetentionPolicyTabComponent implements OnInit {
  @Output() updated: EventEmitter<boolean>=new EventEmitter<boolean>();
  form: FormGroup;
  submited=false;

  statusControlValueChangesSubscribe: Subscription;

  @Input()
  set inputManagementContract(managementContract: ManagementContract) {
    if (!managementContract.versionRetentionPolicy.initialVersion) {
      managementContract.versionRetentionPolicy.initialVersion = true;
    }
    if (!managementContract.versionRetentionPolicy.intermediaryVersionEnum) {
      managementContract.versionRetentionPolicy.intermediaryVersionEnum = "";
    }
    if (managementContract.versionRetentionPolicy.usages.size == 0) {
      // managementContract.storage.objectStrategy = "";
    }
    this._inputManagementContract=managementContract;

    if(!managementContract.description) {
      this._inputManagementContract.description='';
    }
    this.form.controls.status.setValue(managementContract.status);
    this.statusControl = new FormControl(managementContract.status === 'ACTIVE');
    this.resetForm(this.inputManagementContract);
    this.updated.emit(false);

    if (this.statusControlValueChangesSubscribe) {
      this.statusControlValueChangesSubscribe.unsubscribe();
    }
    this.statusControlValueChangesSubscribe = this.statusControl.valueChanges.subscribe((value:boolean) => {
      this.form.controls.status.setValue(value === false ? 'INACTIVE' : 'ACTIVE');
    });
  }

  get inputManagementContract(): ManagementContract {
    return this._inputManagementContract;
  }

  // tslint:disable-next-line:variable-name
  public _inputManagementContract: ManagementContract;
  public statusControl = new FormControl();
  constructor() { }

  @Input()
  set readOnly(readOnly: boolean) {
    if(readOnly&&this.form.enabled) {
      this.form.disable({emitEvent: false});
    } else if(this.form.disabled) {
      this.form.enable({emitEvent: false});
      this.form.get('identifier').disable({emitEvent: false});
    }
  }

  previousValue=(): ManagementContract => {
    return this._inputManagementContract;
  }

  constructor(
    private formBuilder: FormBuilder,
    private managementContractService: ManagementContractService
  ) {
    this.form=this.formBuilder.group({
      identifier: [null,Validators.required],
      name: [null,Validators.required],
      description: [null,Validators.required],
      status: [null],
      storage: this.formBuilder.group({
        unitStrategy: ['', Validators.required],
        objectGroupStrategy: ['', Validators.required],
        objectStrategy: ['', Validators.required],
      }),
      versionRetentionPolicy: this.formBuilder.group({
        initialVersion: ['True', Validators.required],
        intermediaryVersion: ['ALL', Validators.required],
        usage: this.formBuilder.group(({
          usageName: ['BinaryMaster', Validators.required],
          initialVersion: ['true', Validators.required],
          intermediaryVersion: ['ALL', Validators.required]
        }))
      })
    });
    this.form.disable({emitEvent: false});
  }
  ngOnInit(): void {
  }

  unchanged(): boolean {
    const unchanged=JSON.stringify(diff(this.form.getRawValue(),this.previousValue()))==='{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<ManagementContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) => {
        // Update the activation and deactivation dates if the contract status has changed before sending the data
        if (formData.status) {
          if (formData.status === 'ACTIVE') {
            formData.activationDate = new Date();
            formData.deactivationDate = null;
          } else {
            formData.status = 'INACTIVE';
            formData.activationDate = null;
            formData.deactivationDate = new Date();
          }
        }
        return this.managementContractService.patch(formData).pipe(catchError(() => of(null)));
      })
    );
  }

  onSubmit() {
    this.submited=true;
    this.prepareSubmit().subscribe(() => {
      this.managementContractService.get(this._inputManagementContract.identifier).subscribe(
        response => {
          this.submited=false;
          this.inputManagementContract=response;
        }
      );
    },() => {
      this.submited=false;
    });
  }

  resetForm(managementContract: ManagementContract) {
    this.form.reset(managementContract,{emitEvent: false});
  }


  ngOnDestroy() {
    this.statusControlValueChangesSubscribe.unsubscribe();
  }
}
