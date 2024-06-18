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
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { FileFormat, IngestContract, VitamuiAutocompleteMultiselectOptions, diff } from 'vitamui-library';
import { FileFormatService } from '../../../file-format/file-format.service';
import { IngestContractService } from '../../ingest-contract.service';

@Component({
  selector: 'app-ingest-contract-format-tab',
  templateUrl: './ingest-contract-format-tab.component.html',
  styleUrls: ['./ingest-contract-format-tab.component.scss'],
})
export class IngestContractFormatTabComponent implements OnInit {
  @Input() tenantIdentifier: number;

  @Input() set ingestContract(ingestContract: IngestContract) {
    this._ingestContract = ingestContract;
    this.resetForm(this.ingestContract);
    this.updated.emit(false);
  }

  get ingestContract(): IngestContract {
    return this._ingestContract;
  }

  @Input() set isTabActive(isActive: boolean) {
    if (isActive) {
      this.resetForm(this.ingestContract);
    }
  }

  @Input() set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({ emitEvent: false });
    } else if (this.form.disabled) {
      this.form.enable({ emitEvent: false });
      this.form.get('identifier').disable({ emitEvent: false });
    }
  }

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() isFormValid: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;
  submited = false;
  formatTypeList: FileFormat[];
  formatOptions: VitamuiAutocompleteMultiselectOptions;

  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  private _ingestContract: IngestContract;

  previousValue = (): IngestContract => {
    return this._ingestContract;
  };

  constructor(
    private formBuilder: FormBuilder,
    private ingestContractService: IngestContractService,
    private fileFormatService: FileFormatService,
  ) {
    this.form = this.formBuilder.group({
      everyFormatType: [true, Validators.required],
      formatType: [new Array<string>()],
      formatUnidentifiedAuthorized: [false, Validators.required],
    });
  }

  ngOnInit() {
    this.fileFormatService
      .getAllForTenant('' + this.tenantIdentifier)
      .pipe(
        map((files: FileFormat[]) =>
          files.map((file: FileFormat) => {
            return {
              key: file.puid,
              label: file.puid + ' - ' + file.name,
              info: file.description,
            };
          }),
        ),
      )
      .subscribe((options) => (this.formatOptions = { options }));

    if (!this.ingestContract.everyFormatType) {
      this.form.controls.formatType.setValidators(Validators.required);
    }

    this.form.controls.everyFormatType.valueChanges.subscribe((value: boolean) => {
      if (value) {
        this.form.controls.formatType.setValidators([]);
        this.form.controls.formatType.setValue([]);
        this.form.controls.formatType.updateValueAndValidity();
      } else {
        this.form.controls.formatType.setValidators(Validators.required);
        this.form.controls.formatType.markAllAsTouched();
        this.form.controls.formatType.updateValueAndValidity();
      }
    });
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    const isInvalid =
      this.form.get('everyFormatType').value === false && (this.form.get('formatType').invalid || this.form.get('formatType').pending);
    this.isFormValid.emit(!isInvalid);
    return isInvalid;
  }

  prepareSubmit(): Observable<IngestContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) =>
        this.ingestContractService.patch(formData).pipe(catchError(() => of(null))),
      ),
    );
  }

  onSubmit() {
    this.submited = true;
    this.prepareSubmit().subscribe(
      () => {
        this.ingestContractService.get(this._ingestContract.identifier).subscribe((response) => {
          this.submited = false;
          this.ingestContract = response;
        });
      },
      () => {
        this.submited = false;
      },
    );
  }

  resetForm(ingestContract: IngestContract) {
    this.form.reset(ingestContract, { emitEvent: false });
  }
}
