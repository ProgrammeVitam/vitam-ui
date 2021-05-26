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
import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, of, Subscription } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { AccessContractApiService, ApplicationId, diff, ExternalParamProfile } from 'ui-frontend-common';
import { extend, isEmpty } from 'underscore';
import { ExternalParamProfileService } from '../../external-param-profile.service';
import { HttpHeaders, HttpParams } from '@angular/common/http';
import { ExternalParamProfileValidators } from '../../external-param-profile.validators';

@Component({
  selector: 'app-information-tab',
  templateUrl: './information-tab.component.html',
  styleUrls: ['./information-tab.component.scss'],
})
export class InformationTabComponent implements OnDestroy, OnInit, OnChanges {
  form: FormGroup;
  permissionForm: FormGroup;
  groupsCount: boolean;
  userLevel: string;
  previousValue: ExternalParamProfile;
  accessContracts$: Observable<any[]>;

  @Input() externalParamProfile: ExternalParamProfile;
  @Input() readOnly: boolean;
  @Input() tenantIdentifier: string;

  private updateFormSub: Subscription;

  constructor(
    private formBuilder: FormBuilder,
    private externalParamProfileService: ExternalParamProfileService,
    private accessContractApiService: AccessContractApiService,
    private externalParamProfileValidators: ExternalParamProfileValidators
  ) {}

  ngOnInit() {
    this.initForm();
    this.initListenersOnFormsValuesChanges();
    this.accessContracts$ = this.getAllAccessContracts();
  }

  ngOnDestroy() {
    this.updateFormSub.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.hasOwnProperty('externalParamProfile') && this.form) {
      this.resetForm(this.form, this.externalParamProfile, this.readOnly);
      this.previousValue = this.form.value;
    }
  }

  private initForm() {
    this.form = this.formBuilder.group({
      name: [null, Validators.required],
      description: [null, Validators.required],
      enabled: false,
      accessContract: [null, Validators.required],
    });
  }

  private initListenersOnFormsValuesChanges() {
    this.updateFormSub = this.form.valueChanges
      .pipe(
        map(() => diff(this.form.value, this.previousValue)),
        filter((formData) => !isEmpty(formData)),
        map((formData) =>
          extend(
            {
              id: this.externalParamProfile.id,
              idExternalParam: this.externalParamProfile.idExternalParam,
              idProfile: this.externalParamProfile.idProfile,
            },
            formData
          )
        ),
        switchMap((formData) => this.externalParamProfileService.patch(formData).pipe(catchError((error) => of(error)))),
        catchError((error) => of(error))
      )
      .subscribe((externalParamProfile: ExternalParamProfile) => this.resetForm(this.form, externalParamProfile, this.readOnly));
  }

  private resetForm(form: FormGroup, externalParamProfile: ExternalParamProfile, readOnly: boolean) {
    form.reset(externalParamProfile, { emitEvent: false });
    this.initFormValidators(form, externalParamProfile);
    InformationTabComponent.initFormActivationState(form, readOnly);
  }

  private initFormValidators(form: FormGroup, externalParamProfile: ExternalParamProfile) {
    form
      .get('name')
      .setAsyncValidators(
        this.externalParamProfileValidators.nameExists(
          +this.tenantIdentifier,
          ApplicationId.EXTERNAL_PARAM_PROFILE_APP_REF,
          externalParamProfile.name
        )
      );
  }

  private static initFormActivationState(form: FormGroup, readOnly: boolean) {
    if (readOnly) {
      form.disable({ emitEvent: false });
      return;
    }
    form.enable({ emitEvent: false });
  }

  private getAllAccessContracts(): Observable<any[]> {
    const params = new HttpParams();
    const headers = new HttpHeaders().append('X-Tenant-Id', this.tenantIdentifier);
    return this.accessContractApiService.getAllAccessContracts(params, headers);
  }
}
