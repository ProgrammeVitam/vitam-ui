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
import { FormBuilder, FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ExternalParamProfile, diff } from 'ui-frontend-common';
import { extend, isEmpty } from 'underscore';
import { ExternalParamProfileService } from '../../external-param-profile.service';

@Component({
  selector: 'app-thresholds-tab',
  templateUrl: './thresholds-tab.component.html',
  styleUrls: ['./thresholds-tab.component.css'],
})
export class ThresholdsTabComponent implements OnDestroy, OnInit, OnChanges {
  constructor(
    private formBuilder: FormBuilder,
    private externalParamProfileService: ExternalParamProfileService,
  ) {}

  form: FormGroup;
  previousValue: ExternalParamProfile;
  thresholdValues: number[] = [100, 10000, 100000, 1000000, 10000000, 100000000, 1000000000];
  isUpdated: boolean;

  @Input() externalParamProfile: ExternalParamProfile;
  @Input() readOnly: boolean;
  @Input() tenantIdentifier: string;

  private updateFormSub: Subscription;

  private static initFormActivationState(form: FormGroup, readOnly: boolean) {
    if (readOnly) {
      form.disable({ emitEvent: false });
      return;
    }
    form.enable({ emitEvent: false });
  }

  ngOnInit() {
    this.initForm();
    this.isUpdated = false;

    this.updateFormSub = this.form.valueChanges.subscribe(() => {
      const updatedModel: any = diff(this.form.value, this.previousValue);
      this.isUpdated = !isEmpty(updatedModel);
    });
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
      usePlatformThreshold: true,
      bulkOperationsThreshold: [null, []],
    });
  }

  private resetForm(form: FormGroup, externalParamProfile: ExternalParamProfile, readOnly: boolean) {
    form.reset(externalParamProfile, { emitEvent: false });
    ThresholdsTabComponent.initFormActivationState(form, readOnly);
  }

  submitModification() {
    let updated: any = diff(this.form.value, this.previousValue);
    if (!isEmpty(updated)) {
      updated = extend(
        {
          id: this.externalParamProfile.id,
          idExternalParam: this.externalParamProfile.idExternalParam,
          idProfile: this.externalParamProfile.idProfile,
        },
        updated,
      );
      if (updated.usePlatformThreshold) {
        delete updated.bulkOperationsThreshold;
      }

      this.externalParamProfileService.patch(updated).subscribe((externalParamProfile: ExternalParamProfile) => {
        this.resetForm(this.form, externalParamProfile, this.readOnly);
        this.isUpdated = false;
      });
    }
  }
}
