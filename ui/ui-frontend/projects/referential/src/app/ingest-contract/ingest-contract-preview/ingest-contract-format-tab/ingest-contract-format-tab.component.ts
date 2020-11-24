import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {FileFormat, IngestContract} from 'projects/vitamui-library/src/public-api';
import {Observable, of} from 'rxjs';
import {catchError, filter, map, switchMap} from 'rxjs/operators';
import {diff} from 'ui-frontend-common';
import {extend, isEmpty} from 'underscore';
import {FileFormatService} from '../../../file-format/file-format.service';
import {IngestContractService} from '../../ingest-contract.service';


@Component({
  selector: 'app-ingest-contract-format-tab',
  templateUrl: './ingest-contract-format-tab.component.html',
  styleUrls: ['./ingest-contract-format-tab.component.scss']
})
export class IngestContractFormatTabComponent implements OnInit {

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;
  submited = false;

  @Input() tenantIdentifier: number;

  formatTypeList: FileFormat[];

  // tslint:disable-next-line:variable-name
  private _ingestContract: IngestContract;

  previousValue = (): IngestContract => {
    return this._ingestContract;
  }

  @Input()
  set ingestContract(ingestContract: IngestContract) {
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
    private fileFormatService: FileFormatService,
  ) {
    this.form = this.formBuilder.group({
      everyFormatType: [true, Validators.required],
      formatType: [new Array<string>(), Validators.required],
      formatUnidentifiedAuthorized: [false, Validators.required]
    });

  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    return this.form.get('everyFormatType').value === false && (this.form.get('formatType').invalid || this.form.get('formatType').pending);
  }

  prepareSubmit(): Observable<IngestContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({id: this.previousValue().id, identifier: this.previousValue().identifier}, formData)),
      switchMap(
        (formData: { id: string, [key: string]: any }) => this.ingestContractService.patch(formData).pipe(catchError(() => of(null)))));
  }

  onSubmit() {
    this.submited = true;
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

  ngOnInit() {
    this.fileFormatService.getAllForTenant('' + this.tenantIdentifier).subscribe(files => {
      this.formatTypeList = files;
    });
  }


  resetForm(ingestContract: IngestContract) {
    this.form.reset(ingestContract, {emitEvent: false});
  }
}
