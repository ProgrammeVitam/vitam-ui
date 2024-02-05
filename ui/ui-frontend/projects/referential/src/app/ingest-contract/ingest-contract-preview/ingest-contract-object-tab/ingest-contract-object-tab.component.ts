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
import { IngestContract } from 'projects/vitamui-library/src/public-api';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { diff, Option } from 'ui-frontend-common';
import { extend, isEmpty } from 'underscore';

import { IngestContractService } from '../../ingest-contract.service';

@Component({
  selector: 'app-ingest-contract-object-tab',
  templateUrl: './ingest-contract-object-tab.component.html',
  styleUrls: ['./ingest-contract-object-tab.component.scss'],
})
export class IngestContractObjectTabComponent implements OnInit {
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;
  submited = false;
  // tslint:disable-next-line:variable-name
  private _ingestContract: IngestContract;

  // FIXME: Get list from common var ?
  usages: Option[] = [
    { key: 'BinaryMaster', label: 'Original numérique', info: '' },
    { key: 'Dissemination', label: 'Diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignette', info: '' },
    { key: 'TextContent', label: 'Contenu brut', info: '' },
    { key: 'PhysicalMaster', label: 'Original papier', info: '' },
  ];

  previousValue = (): IngestContract => {
    return this._ingestContract;
  };

  @Input()
  set ingestContract(ingestContract: IngestContract) {
    this._ingestContract = ingestContract;
    if (!this._ingestContract.dataObjectVersion) {
      this._ingestContract.dataObjectVersion = [];
    }
    this.resetForm(this.ingestContract);
    this.updated.emit(false);
  }

  get ingestContract(): IngestContract {
    return this._ingestContract;
  }

  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({ emitEvent: false });
    } else if (this.form.disabled) {
      this.form.enable({ emitEvent: false });
      this.form.get('identifier').disable({ emitEvent: false });
    }
  }

  constructor(
    private formBuilder: FormBuilder,
    private ingestContractService: IngestContractService,
  ) {
    this.form = this.formBuilder.group({
      masterMandatory: [true],
      everyDataObjectVersion: [true, Validators.required],
      dataObjectVersion: [[], Validators.required],
    });
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
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

  ngOnInit() {}

  isInvalid(): boolean {
    return (
      this.form.get('everyDataObjectVersion').value === false &&
      (this.form.get('dataObjectVersion').invalid || this.form.get('dataObjectVersion').pending)
    );
  }

  resetForm(ingestContract: IngestContract) {
    this.form.reset(ingestContract, { emitEvent: false });
  }
}
