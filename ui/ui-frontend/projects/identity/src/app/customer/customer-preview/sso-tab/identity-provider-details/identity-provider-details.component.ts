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
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { merge } from 'rxjs';
import { debounceTime, filter, map, switchMap } from 'rxjs/operators';

import { diff, IdentityProvider, newFile } from 'ui-frontend-common';
import { IdentityProviderService } from '../identity-provider.service';

import { extend, isEmpty } from 'underscore';

const UPDATE_DEBOUNCE_TIME = 200;

@Component({
  selector: 'app-identity-provider-details',
  templateUrl: './identity-provider-details.component.html',
  styleUrls: ['./identity-provider-details.component.scss']
})
export class IdentityProviderDetailsComponent implements OnInit {

  @Input()
  set identityProvider(identityProvider: IdentityProvider) {
    this._identityProvider = identityProvider;

    if (!this._identityProvider.internal) {
      this.idpMetadata.enable({ emitEvent: false });
    }
    this.form.reset(identityProvider, { emitEvent: false });
    this.previousValue = this.form.value;
  }
  get identityProvider(): IdentityProvider { return this._identityProvider; }
  private _identityProvider: IdentityProvider;

  @Input()
  set domains(domains: Array<{ value: string, disabled: boolean }>) {
    this._domains = domains.map((domain: { value: string, disabled: boolean }) => {
      if (this.identityProvider.patterns.includes(domain.value)) {
        return { value: domain.value, disabled: false };
      }

      return domain;
    });
  }
  get domains(): Array<{ value: string, disabled: boolean }> { return this._domains; }
  private _domains: Array<{ value: string, disabled: boolean }> = [];

  private previousValue: {
    id: string,
    identifier?: string,
    enabled: boolean,
    name: string,
    internal: boolean;
    patterns: string[]
  };

  @Input()
  set readOnly(readOnly: boolean) {
    this._readOnly = readOnly || this._identityProvider.internal;
    if (readOnly) {
      if (this.form.enabled) {
        this.form.disable({ emitEvent: false });
      }
      if (this.idpMetadata.enabled) {
        this.idpMetadata.disable({ emitEvent: false });
      }
    } else {
      if (this.form.disabled) {
        this.form.enable({ emitEvent: false });
      }
      if (this.idpMetadata.disabled) {
        this.idpMetadata.enable({ emitEvent: false });
      }
    }
  }
  get readOnly(): boolean { return this._readOnly; }
  private _readOnly: boolean;

  form: FormGroup;
  idpMetadata: FormControl;

  constructor(private formBuilder: FormBuilder, private identityProviderService: IdentityProviderService) {
    this.form = this.formBuilder.group({
      id: [null, Validators.required],
      identifier: [{value: null, disabled : true}, Validators.required],
      enabled: [true, Validators.required],
      name: [null, Validators.required],
      internal: [{ value: false, disabled: true }, Validators.required],
      patterns: [null, Validators.required],
      mailAttribute: [null]
    });
    this.idpMetadata = new FormControl({value: newFile([''], 'metadata.xml'), disabled: true});

    merge(this.form.statusChanges, this.form.valueChanges)
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        filter(() => this.form.valid),
        map(() => diff(this.form.value, this.previousValue)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => extend({ id: this.identityProvider.id }, formData)),
        switchMap((formData) => this.identityProviderService.patch(formData)),
      )
      .subscribe(() => {
        this.previousValue = this.form.value;
      });
    this.idpMetadata.valueChanges
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        filter(() => this.idpMetadata.valid),
        switchMap(() => this.identityProviderService.updateMetadataFile(this.identityProvider.id, this.idpMetadata.value))
      )
      .subscribe();
  }

  ngOnInit() {
  }

}
