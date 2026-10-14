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
import { Component, inject, signal, output, input, model, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { diff, IngestContract, Option, SelectComponent, SlideToggleComponent } from 'vitamui-library';

import { IngestContractService } from '../../ingest-contract.service';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-ingest-contract-object-tab',
  templateUrl: './ingest-contract-object-tab.component.html',
  styleUrls: ['./ingest-contract-object-tab.component.scss'],
  imports: [ReactiveFormsModule, SlideToggleComponent, SelectComponent, TranslatePipe],
})
export class IngestContractObjectTabComponent implements OnChanges {
  private readonly formBuilder = inject(FormBuilder);
  private readonly ingestContractService = inject(IngestContractService);

  readonly updated = output<boolean>();
  readonly isFormValid = output<boolean>();

  form: FormGroup;
  submited = signal(false);

  // FIXME: Get list from common var ?
  usages: Option[] = [
    { key: 'BinaryMaster', label: 'Original numérique', info: '' },
    { key: 'Dissemination', label: 'Diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignette', info: '' },
    { key: 'TextContent', label: 'Contenu brut', info: '' },
    { key: 'PhysicalMaster', label: 'Original papier', info: '' },
  ];

  previousValue = (): any => {
    return {
      masterMandatory: this.ingestContract().masterMandatory,
      everyDataObjectVersion: this.ingestContract().everyDataObjectVersion,
      dataObjectVersion: this.ingestContract().dataObjectVersion,
    };
  };

  readonly ingestContract = model<IngestContract>(undefined);

  readonly isTabActive = input<boolean>(undefined);

  readonly readOnly = input<boolean>(undefined);

  constructor() {
    this.form = this.formBuilder.group({
      masterMandatory: [true],
      everyDataObjectVersion: [true, Validators.required],
      dataObjectVersion: [[]],
    });

    this.form.controls['everyDataObjectVersion'].valueChanges.subscribe((value: boolean) => {
      if (value) {
        this.form.controls['dataObjectVersion'].setValidators([]);
        this.form.controls['dataObjectVersion'].setValue([]);
        this.form.controls['dataObjectVersion'].updateValueAndValidity();
      } else {
        this.form.controls['dataObjectVersion'].setValidators(Validators.required);
        this.form.controls['dataObjectVersion'].markAllAsTouched();
        this.form.controls['dataObjectVersion'].updateValueAndValidity();
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['readOnly']) {
      const readOnly = this.readOnly();
      if (readOnly && this.form.enabled) {
        this.form.disable({ emitEvent: false });
      } else if (this.form.disabled) {
        this.form.enable({ emitEvent: false });
        this.form.get('identifier').disable({ emitEvent: false });
      }
    }
    if (changes['ingestContract']) {
      this.setIngestContract(this.ingestContract());
    }
    if (changes['isTabActive']) {
      const isActive = this.isTabActive();
      if (isActive) {
        this.resetForm(this.ingestContract());
      }
    }
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<IngestContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.ingestContract().id, identifier: this.ingestContract().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) =>
        this.ingestContractService.patch(formData).pipe(catchError(() => of(null))),
      ),
    );
  }

  onSubmit() {
    this.submited.set(true);
    this.prepareSubmit().subscribe(
      () => {
        this.ingestContractService.get(this.ingestContract().identifier).subscribe((response) => {
          this.submited.set(false);
          this.setIngestContract(response);
        });
      },
      () => {
        this.submited.set(false);
      },
    );
  }

  isInvalid(): boolean {
    const isInvalid =
      this.form.get('everyDataObjectVersion').value === false &&
      (this.form.get('dataObjectVersion').invalid || this.form.get('dataObjectVersion').pending);
    this.isFormValid.emit(!isInvalid);
    return isInvalid;
  }

  private setIngestContract(ingestContract: IngestContract) {
    this.ingestContract.set(ingestContract);
    if (!this.ingestContract().dataObjectVersion) {
      this.ingestContract().dataObjectVersion = [];
    }
    this.resetForm(this.previousValue());
    this.updated.emit(false);
  }

  resetForm(ingestContract: IngestContract) {
    this.form.reset(ingestContract, { emitEvent: false });
  }
}
