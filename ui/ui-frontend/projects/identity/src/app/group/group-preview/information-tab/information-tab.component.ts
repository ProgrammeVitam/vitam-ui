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

import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { merge, of, Subscription } from 'rxjs';
import { catchError, debounceTime, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';

import { AuthService, buildValidators, diff, Group } from 'ui-frontend-common';

import { GroupService } from '../../group.service';
import { GroupValidators } from '../../group.validators';

const DEBOUNCE_TIME = 200;

@Component({
  selector: 'app-information-tab',
  templateUrl: './information-tab.component.html',
  styleUrls: ['./information-tab.component.scss']
})
export class InformationTabComponent implements OnDestroy, OnChanges {

  form: FormGroup;
  groupsCount: number;
  previousValue: {
    id: string,
    enabled: boolean;
    name: string,
    level: string,
    description: string,
  };

  private updateSub: Subscription;

  @Input() group: Group;

  @Input() readOnly: boolean;

  constructor(
    private formBuilder: FormBuilder,
    private groupService: GroupService,
    private groupValidators: GroupValidators,
    public authService: AuthService,
  ) {
    this.form = this.formBuilder.group({
      id: [null, Validators.required],
      identifier: [{value: null, disabled : true}, Validators.required],
      enabled: [false],
      name: [null, Validators.required],
      description: [null, Validators.required],
      level: [null, buildValidators(this.authService.user)]
    });

    this.updateSub = merge(this.form.valueChanges, this.form.statusChanges)
      .pipe(
        debounceTime(DEBOUNCE_TIME),
        map(() => diff(this.form.value, this.previousValue)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => extend({ id: this.group.id }, formData)),
        switchMap((formData) => this.groupService.patch(formData).pipe(catchError(() => of(null))))
      )
      .subscribe(() => this.previousValue = this.form.value);

  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.hasOwnProperty('group') || changes.hasOwnProperty('readOnly')) {
      if (this.group) {
       this.resetForm(this.form, this.group, this.readOnly);
       this.previousValue = this.form.value;
      }
    }
  }

  private resetForm(form: FormGroup, group: Group, readOnly: boolean) {
    form.reset(group, { emitEvent: false });
    this.initFormValidators(form, group);
    this.initFormActivationState(form, group, readOnly);
    form.updateValueAndValidity({ emitEvent: false });
  }

  private initFormValidators(form: FormGroup, group: Group) {
    form.get('name').setAsyncValidators(this.groupValidators.nameExists(group.customerId, group.name));
  }

  private initFormActivationState(form: FormGroup, group: Group, readOnly: boolean) {
    if (readOnly) {
      form.disable({ emitEvent: false });

      return;
    }

    form.enable({ emitEvent: false });
    form.get('identifier').disable({emitEvent: false});

    if (group.usersCount) {
      this.form.get('enabled').disable({emitEvent : false});
    }
    if (group.profileIds && group.profileIds.length) {
      this.form.get('level').disable({emitEvent : false});
    }
  }

  ngOnDestroy() {
    this.updateSub.unsubscribe();
  }

}
