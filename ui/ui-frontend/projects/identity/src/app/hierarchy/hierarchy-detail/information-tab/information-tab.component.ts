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
import { merge, of, Subscription } from 'rxjs';
import { catchError, debounceTime, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';

import { AuthService, buildValidators, diff, Profile } from 'ui-frontend-common';
import { HierarchyService } from '../../hierarchy.service';
import { ProfileValidators } from '../../profile.validators';

const DEBOUNCE_TIME = 400;

@Component({
  selector: 'app-information-tab',
  templateUrl: './information-tab.component.html',
  styleUrls: ['./information-tab.component.scss']
})
export class InformationTabComponent implements OnDestroy, OnInit, OnChanges {

  form: FormGroup;
  userLevel: string;
  previousValue: {
    name: string,
    level: string,
    description: string,
    enabled: boolean,
  };

  @Input() profile: Profile;

  @Input() readOnly: boolean;

  private updateSub: Subscription;

  constructor(
    private formBuilder: FormBuilder,
    private hierarchyService: HierarchyService,
    private profileValidators: ProfileValidators,
    private authService: AuthService
  ) {
    this.userLevel = this.authService.user.level;
    this.form = this.formBuilder.group({
      name: [null, Validators.required],
      identifier: [{value: null, disabled : true}, Validators.required],
      description: [null, Validators.required],
      level: [null, buildValidators(this.authService.user)],
      enabled: null
    });

    this.updateSub = merge(this.form.valueChanges, this.form.statusChanges)
    .pipe(
      debounceTime(DEBOUNCE_TIME),
      map(() => diff(this.form.value, this.previousValue)),
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.profile.id , customerId: this.profile.customerId,
                                 tenantIdentifier: this.profile.tenantIdentifier},
                               formData)),
      switchMap((formData) => {
        return this.hierarchyService.patch(formData).pipe(catchError(() => of(null)));
      })
    )
    .subscribe((profile) => this.resetForm(this.form, profile, this.readOnly)
    );
  }

  ngOnInit() {
  }

  ngOnDestroy() {
    this.updateSub.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.hasOwnProperty('profile') || changes.hasOwnProperty('readOnly')) {
      if (this.profile) {
       this.resetForm(this.form, this.profile, this.readOnly);
       this.previousValue = this.form.value;
      }
    }
  }

  private resetForm(form: FormGroup, profile: Profile, readOnly: boolean) {
    form.reset(profile, { emitEvent: false });
    this.initFormValidators(form, profile);
    this.initFormActivationState(form, profile, readOnly);
    form.updateValueAndValidity({ emitEvent: false });
  }

  private initFormValidators(form: FormGroup, profile: Profile) {
    form.get('name').setAsyncValidators(this.profileValidators.nameExists(profile.tenantIdentifier, profile.level, profile.applicationName, profile.name));
  }

  private initFormActivationState(form: FormGroup, profile: Profile, readOnly: boolean) {
    if (readOnly) {
      form.disable({ emitEvent: false });

      return;
    }

    form.enable({ emitEvent: false });

    form.get('identifier').disable({emitEvent : false});
    if (profile.groupsCount && profile.groupsCount > 0) {
      form.get('enabled').disable({emitEvent : false});
      form.get('level').disable({emitEvent : false});
    }
  }

}
