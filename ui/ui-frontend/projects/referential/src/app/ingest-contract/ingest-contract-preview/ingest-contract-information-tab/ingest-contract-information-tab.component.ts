import {HttpHeaders, HttpParams} from '@angular/common/http';
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {IngestContract} from 'projects/vitamui-library/src/public-api';
import {Observable, of} from 'rxjs';
import {catchError, filter, map, switchMap} from 'rxjs/operators';
import {diff} from 'ui-frontend-common';
import {extend, isEmpty} from 'underscore';

import {ArchiveProfileApiService} from '../../../core/api/archive-profile-api.service';
import {ManagementContractApiService} from '../../../core/api/management-contract-api.service';
import {IngestContractCreateValidators} from '../../ingest-contract-create/ingest-contract-create.validators';
import {IngestContractService} from '../../ingest-contract.service';

@Component({
  selector: 'app-ingest-contract-information-tab',
  templateUrl: './ingest-contract-information-tab.component.html',
  styleUrls: ['./ingest-contract-information-tab.component.scss']
})
export class IngestContractInformationTabComponent implements OnInit {
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Input() tenantIdentifier: number;

  form: FormGroup;

  submited = false;

  ruleFilter = new FormControl();
  statusControl = new FormControl();

  managementContracts: any[];
  archiveProfiles: any[];

  // tslint:disable-next-line:variable-name
  private _ingestContract: IngestContract;

  previousValue = (): IngestContract => {
    return this._ingestContract;
  }

  @Input()
  set ingestContract(ingestContract: IngestContract) {
    if (!ingestContract.managementContractId) {
      ingestContract.managementContractId = '';
    }
    this._ingestContract = ingestContract;
    this.resetForm(this.ingestContract);
    this.updated.emit(false);
  }

  get ingestContract(): IngestContract {
    return this._ingestContract;
  }

  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({emitEvent: false});
    } else if (this.form.disabled) {
      this.form.enable({emitEvent: false});
      this.form.get('identifier').disable({emitEvent: false});
    }
  }

  constructor(
    private formBuilder: FormBuilder,
    private ingestContractService: IngestContractService,
    private managementContractService: ManagementContractApiService,
    private archiveProfileService: ArchiveProfileApiService,
    private ingestContractCreateValidators: IngestContractCreateValidators
  ) {
    this.form = this.formBuilder.group({
      identifier: [null, Validators.required],
      status: ['ACTIVE'],
      name: [null, [], this.ingestContractCreateValidators.uniqueNameWhileEdit(this.previousValue)],
      description: [null, Validators.required],
      archiveProfiles: [new Array<string>(), /* Validators.required */],
      managementContractId: [null]
    });

    this.statusControl.valueChanges.subscribe((value) => {
      this.form.controls.status.setValue(value = (value === false) ? 'INACTIVE' : 'ACTIVE');
    });

    this.ruleFilter.valueChanges.subscribe((val) => {
      if (val === true) {
        this.form.controls.ruleCategoryToFilter.setValue(new Array<string>());
      }
    });
  }

  ngOnInit(): void {
    const params = new HttpParams().set('embedded', 'ALL');
    const headers = new HttpHeaders().append('X-Tenant-Id', '' + this.tenantIdentifier);

    this.managementContractService.getAllByParams(params, headers).subscribe(managmentContracts => {
      this.managementContracts = managmentContracts;
    });

    this.archiveProfileService.getAllByParams(params, headers).subscribe(archiveProfiles => {
      this.archiveProfiles = archiveProfiles;
    });
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    return this.form.get('name').invalid || this.form.get('name').pending ||
      this.form.get('description').invalid || this.form.get('description').pending ||
      this.form.get('status').invalid || this.form.get('status').pending ||
      this.form.get('archiveProfiles').invalid || this.form.get('archiveProfiles').pending;
  }

  prepareSubmit(): Observable<IngestContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({id: this.previousValue().id, identifier: this.previousValue().identifier}, formData)),
      switchMap((formData: { id: string, [key: string]: any }) => {
      // Update the activation and deactivation dates if the contract status has changed before sending the data
      if (formData.status) {
        if (formData.status === 'ACTIVE') {
          formData.activationDate = new Date();
          formData.deactivationDate = '';
        } else {
          formData.status = 'INACTIVE';
          formData.activationDate = '';
          formData.deactivationDate = new Date();
          }
        }
        return this.ingestContractService.patch(formData).pipe(catchError(() => of(null)))
      })
    );
  }

  onSubmit() {
    this.submited = true;
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit().subscribe(() => {
      this.ingestContractService.get(this._ingestContract.identifier).subscribe(
        response => {
          this.submited = false;
          this.ingestContract = response;
        }
      );
    }, () => {
      this.submited = false;
    });
  }

  resetForm(ingestContract: IngestContract) {
    this.statusControl.setValue(ingestContract.status === 'ACTIVE');
    this.form.reset(ingestContract, {emitEvent: false});
  }
}
