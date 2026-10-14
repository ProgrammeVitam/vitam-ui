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
import { Component, inject, OnInit, signal, input, output, model, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { diff, FileFormat, IngestContract, SelectComponent, SlideToggleComponent, VitamuiSelectOptions } from 'vitamui-library';
import { FileFormatService } from '../../../file-format/file-format.service';
import { IngestContractService } from '../../ingest-contract.service';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-ingest-contract-format-tab',
  templateUrl: './ingest-contract-format-tab.component.html',
  styleUrls: ['./ingest-contract-format-tab.component.scss'],
  imports: [ReactiveFormsModule, SlideToggleComponent, SelectComponent, TranslatePipe],
})
export class IngestContractFormatTabComponent implements OnChanges, OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly ingestContractService = inject(IngestContractService);
  private readonly fileFormatService = inject(FileFormatService);

  readonly tenantIdentifier = input<number>(undefined);

  readonly ingestContract = model<IngestContract>(undefined);

  readonly isTabActive = input<boolean>(undefined);

  readonly readOnly = input<boolean>(undefined);

  readonly updated = output<boolean>();
  readonly isFormValid = output<boolean>();

  form: FormGroup;
  submitted = signal(false);
  formatOptions: VitamuiSelectOptions;

  previousValue = (): any => {
    return {
      everyFormatType: this.ingestContract().everyFormatType,
      formatType: this.ingestContract().formatType ? this.ingestContract().formatType.sort() : null,
      formatUnidentifiedAuthorized: this.ingestContract().formatUnidentifiedAuthorized,
    };
  };

  constructor() {
    this.form = this.formBuilder.group({
      everyFormatType: [true, Validators.required],
      formatType: [null],
      formatUnidentifiedAuthorized: [false, Validators.required],
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['ingestContract']) {
      this.setIngestContract(this.ingestContract());
    }
    if (changes['isTabActive']) {
      if (this.isTabActive()) {
        this.resetForm(this.ingestContract());
      }
    }
    if (changes['readOnly']) {
      if (this.readOnly() && this.form.enabled) {
        this.form.disable({ emitEvent: false });
      } else if (this.form.disabled) {
        this.form.enable({ emitEvent: false });
        this.form.get('identifier').disable({ emitEvent: false });
      }
    }
  }

  ngOnInit() {
    this.fileFormatService
      .getAllForTenant('' + this.tenantIdentifier())
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

    if (!this.ingestContract().everyFormatType) {
      this.form.controls['formatType'].setValidators(Validators.required);
    }

    this.form.controls['everyFormatType'].valueChanges.subscribe((value: boolean) => {
      this.form.controls['formatType'].setValue(null);
      if (value) {
        this.form.controls['formatType'].setValidators([]);
      } else {
        this.form.controls['formatType'].setValidators(Validators.required);
        this.form.controls['formatType'].markAllAsTouched();
      }
      this.form.controls['formatType'].updateValueAndValidity();
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
      map((formData) => {
        // prevent having error from backend
        if (formData['formatType'] === null) {
          formData['formatType'] = [];
        }
        return formData;
      }),
      map((formData) => extend({ id: this.ingestContract().id, identifier: this.ingestContract().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) =>
        this.ingestContractService.patch(formData).pipe(catchError(() => of(null))),
      ),
    );
  }

  onSubmit() {
    this.submitted.set(true);
    this.prepareSubmit().subscribe(
      () => {
        this.ingestContractService.get(this.ingestContract().identifier).subscribe((response) => {
          this.submitted.set(false);
          this.setIngestContract(response);
        });
      },
      () => {
        this.submitted.set(false);
      },
    );
  }

  private setIngestContract(ingestContract: IngestContract) {
    this.ingestContract.set(ingestContract);
    this.resetForm(this.ingestContract());
    this.updated.emit(false);
  }

  resetForm(ingestContract: IngestContract) {
    this.form.reset(ingestContract, { emitEvent: false });
  }
}
