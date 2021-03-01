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
import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { merge, of } from 'rxjs';
import { catchError, debounceTime, filter, map, switchMap } from 'rxjs/operators';
import { diff, Owner, Tenant } from 'ui-frontend-common';
import { extend, isEmpty } from 'underscore';

import { OwnerFormValidators } from '../../owner-form/owner-form.validators';
import { OwnerService } from '../../owner.service';
import { TenantFormValidators } from '../../tenant-create/tenant-form.validators';
import { TenantService } from '../../tenant.service';

const UPDATE_DEBOUNCE_TIME = 200;

@Component({
  selector: 'app-information-tab',
  templateUrl: './information-tab.component.html',
  styleUrls: ['./information-tab.component.scss']
})
export class InformationTabComponent implements OnChanges, OnInit {

  @Input() owner: Owner;
  @Input() tenant: Tenant;
  @Input() readOnly: boolean;

  ownerForm: FormGroup;
  tenantForm: FormGroup;
  previousOwner: {
    id: string,
    identifier: string,
    customerId: string,
    code: string,
    name: string,
    companyName: string,
    address: {
      street: string,
      zipCode: string,
      city: string,
      country: 'FR',
    },
    internalCode: string
  };
  previousTenant: {
    id: string,
    identifier: string,
    customerId: string,
    proof: boolean,
    ownerId: string,
    name: string,
    enabled: boolean,
    ingestContractHoldingIdentifier: string,
    itemIngestContractIdentifier: string,
    accessContractHoldingIdentifier: string,
    accessContractLogbookIdentifier: string,
  };

  constructor(
    private formBuilder: FormBuilder,
    private ownerFormValidators: OwnerFormValidators,
    private ownerService: OwnerService,
    private tenantService: TenantService,
    private tenantFormValidators: TenantFormValidators
  ) {
    this.ownerForm = this.formBuilder.group({
      id: [null, Validators.required],
      identifier: [{value: null, disabled: true}, Validators.required],
      customerId: [null, Validators.required],
      code: [
        null,
        [Validators.required, Validators.pattern(/^[0-9]{6,20}$/)],
        this.ownerFormValidators.uniqueCode(),
      ],
      name: [null, Validators.required],
      companyName: [null, Validators.required],
      address: this.formBuilder.group({
        street: null,
        zipCode: null,
        city: null,
        country: 'FR',
      }),
      internalCode: [null]
    });

    this.tenantForm = this.formBuilder.group({
      id: [null, Validators.required],
      identifier: [null, Validators.required],
      customerId: [null, Validators.required],
      ownerId: [null, Validators.required],
      name: [
        null,
        [Validators.required],
        this.tenantFormValidators.uniqueName(),
      ],
      enabled: [true, Validators.required],
      ingestContractHoldingIdentifier: [null],
      itemIngestContractIdentifier: [null],
      accessContractHoldingIdentifier: [null],
      accessContractLogbookIdentifier: [null]
    });

    merge(this.ownerForm.valueChanges, this.ownerForm.statusChanges)
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.ownerForm.value, this.previousOwner)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => extend({ id: this.owner.id }, formData)),
        switchMap((formData) => this.ownerService.patch(formData).pipe(catchError(() => of(null))))
      )
      .subscribe((owner: Owner) => this.resetOwnerForm(owner));

    merge(this.tenantForm.valueChanges, this.tenantForm.statusChanges)
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.tenantForm.value, this.previousTenant)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => extend({ id: this.tenant.id }, formData)),
        switchMap((formData) => this.tenantService.patch(formData, this.ownerForm.value.name).pipe(catchError(() => of(null))))
      )
      .subscribe((tenant: Tenant) => {
        this.resetTenantForm(tenant);
      });
  }

  ngOnInit() {
  }

  ngOnChanges() {
    if (this.readOnly) {
      this.ownerForm.disable({ emitEvent: false });
    } else if (this.ownerForm.disabled) {
      this.ownerForm.enable({ emitEvent: false });
    }
    if (this.readOnly) {
      this.tenantForm.disable({ emitEvent: false });
    } else if (this.tenantForm.disabled) {
      this.tenantForm.enable({ emitEvent: false });
    }
    if (this.tenant) {
      this.tenantForm.reset(this.tenant, { emitEvent: false });
      this.tenantForm.get('name').setAsyncValidators(this.tenantFormValidators.uniqueName(this.tenant.name));
      this.previousTenant = this.tenantForm.value;
    }
    if (this.owner) {
      this.resetOwnerForm(this.owner);
    }

  }

  private resetOwnerForm(owner: Owner) {
    this.ownerForm.reset(owner, { emitEvent: false });
    this.ownerForm.get('code').setAsyncValidators(this.ownerFormValidators.uniqueCode(owner.code));
    this.ownerForm.get('identifier').disable({ emitEvent: false });

    this.previousOwner = this.ownerForm.value;
  }

  private resetTenantForm(tenant: Tenant) {
    this.tenant.name = tenant.name;
    this.tenantForm.reset(tenant, { emitEvent: false });
    this.tenantForm.get('name').setAsyncValidators(this.tenantFormValidators.uniqueName(tenant.name));

    this.previousTenant = this.tenantForm.value;
  }

}
