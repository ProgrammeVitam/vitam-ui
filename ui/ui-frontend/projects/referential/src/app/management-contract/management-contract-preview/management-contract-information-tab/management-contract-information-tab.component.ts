import {Component,EventEmitter,Input,OnInit,Output} from '@angular/core';
import {FormBuilder,FormGroup,Validators} from '@angular/forms';
import {ManagementContract} from 'projects/vitamui-library/src/public-api';
import {Observable,of} from 'rxjs';
import {catchError,filter,map,switchMap} from 'rxjs/operators';
import {diff} from 'ui-frontend-common';
import {extend,isEmpty} from 'underscore';
import {ManagementContractService} from '../../management-contract.service';

@Component({
  selector: 'app-management-contract-information-tab',
  templateUrl: './management-contract-information-tab.component.html',
  styleUrls: ['./management-contract-information-tab.component.scss']
})
export class ManagementContractInformationTabComponent implements OnInit {
  @Output() updated: EventEmitter<boolean>=new EventEmitter<boolean>();
  form: FormGroup;
  submited=false;

  @Input()
  set inputManagementContract(managementContract: ManagementContract) {
    this._inputManagementContract=managementContract;

    if(!managementContract.description) {
      this._inputManagementContract.description='';
    }

    this.resetForm(this.inputManagementContract);
    this.updated.emit(false);
  }

  get inputManagementContract(): ManagementContract {
    return this._inputManagementContract;
  }

  // tslint:disable-next-line:variable-name
  public _inputManagementContract: ManagementContract;

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
      status: [null,Validators.required],
      storage: this.formBuilder.group({ 
        unitStrategy: ['', Validators.required],
        objectGroupStrategy: ['', Validators.required],
        objectStrategy: ['', Validators.required],
      }),
    });
    this.form.disable({emitEvent: false});
  }

  ngOnInit(): void {}

  updateStatus(event: any):void {
    if (event.pointerId === -1) {
      this.form.controls['status'].setValue(((event.target as HTMLInputElement).checked)?'ACTIVE':'INACTIVE');
    }
  }

  unchanged(): boolean {
    const unchanged=JSON.stringify(diff(this.form.getRawValue(),this.previousValue()))==='{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<ManagementContract> {
    return of(diff(this.form.getRawValue(),this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({id: this.previousValue().id,identifier: this.previousValue().identifier},formData)),
      switchMap((formData: {id: string,[key: string]: any}) => this.managementContractService.patch(formData).pipe(catchError(() => of(null)))));
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
}
