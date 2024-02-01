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
import { IngestContract, diff } from 'ui-frontend-common';
import { extend, isEmpty } from 'underscore';

import { IngestContractService } from '../../ingest-contract.service';

@Component({
  selector: 'app-ingest-contract-heritage-tab',
  templateUrl: './ingest-contract-heritage-tab.component.html',
  styleUrls: ['./ingest-contract-heritage-tab.component.scss'],
})
export class IngestContractHeritageTabComponent implements OnInit {
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;
  submited = false;
  private _ingestContract: IngestContract;

  @Input()
  readOnly: boolean;

  previousValue = (): IngestContract => {
    return this._ingestContract;
  };

  @Input()
  set ingestContract(ingestContract: IngestContract) {
    this._ingestContract = ingestContract;
    this.resetForm(this.ingestContract);
    this.updated.emit(false);
  }

  get ingestContract(): IngestContract {
    return this._ingestContract;
  }

  constructor(
    private formBuilder: FormBuilder,
    private ingestContractService: IngestContractService,
  ) {}

  ngOnInit() {
    const rule = this.ingestContract !== undefined ? this.ingestContract.computeInheritedRulesAtIngest : false;
    this.form = this.formBuilder.group({
      computeInheritedRulesAtIngest: [rule, Validators.required],
    });
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
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

  prepareSubmit(): Observable<IngestContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) =>
        this.ingestContractService.patch(formData).pipe(catchError(() => of(null))),
      ),
    );
  }

  resetForm(ingestContract: IngestContract) {
    if (this.form === undefined || ingestContract === undefined) {
      return;
    }
    this.form.reset({ computeInheritedRulesAtIngest: ingestContract.computeInheritedRulesAtIngest }, { emitEvent: false });
  }
}
