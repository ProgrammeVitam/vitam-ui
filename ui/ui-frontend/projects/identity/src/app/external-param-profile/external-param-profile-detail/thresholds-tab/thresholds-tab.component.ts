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
import { Component, inject, OnChanges, OnInit, SimpleChanges, signal, input } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { disabled, form, FormField } from '@angular/forms/signals';
import { filter } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { diff, ExternalParamProfile, Option, SelectComponent, SlideToggleComponent, TooltipDirective } from 'vitamui-library';
import { ExternalParamProfileService } from '../../external-param-profile.service';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { DecimalPipe } from '@angular/common';

interface ThresholdsModel {
  usePlatformThreshold: boolean;
  bulkOperationsThreshold: number;
}

@Component({
  selector: 'app-thresholds-tab',
  templateUrl: './thresholds-tab.component.html',
  styleUrls: ['./thresholds-tab.component.css'],
  imports: [FormField, SlideToggleComponent, SelectComponent, TooltipDirective, TranslatePipe],
})
export class ThresholdsTabComponent implements OnInit, OnChanges {
  private readonly externalParamProfileService = inject(ExternalParamProfileService);
  private readonly translateService = inject(TranslateService);
  private readonly decimalPipe = inject(DecimalPipe);

  previousValue: ThresholdsModel;
  thresholdValues: Option[] = [100, 10000, 100000, 1000000, 10000000, 100000000, 1000000000].map((thresholdValue) => ({
    key: thresholdValue,
    label: this.translateService.instant('EXTERNAL_PARAM_PROFILE.MAX_BULK_OPERATIONS_THRESHOLD_VALUES', {
      threshold: this.decimalPipe.transform(thresholdValue),
    }),
  }));
  readonly isUpdated = signal(false);

  readonly externalParamProfile = input<ExternalParamProfile>(undefined);
  readonly readOnly = input<boolean>(undefined);
  readonly tenantIdentifier = input<string>(undefined);

  // The profile is applied to the form only after the initialization (as when the form was created in ngOnInit)
  private initialized = false;
  private readonly model = signal<ThresholdsModel>({ usePlatformThreshold: true, bulkOperationsThreshold: null });
  // Value set by resetForm: like a reset with emitEvent: false, it must not update isUpdated
  private resetValue = this.model();
  private readonly formReadOnly = signal(false);
  readonly form = form(this.model, (path) => {
    disabled(path, () => this.formReadOnly());
  });

  constructor() {
    toObservable(this.model)
      .pipe(
        filter((value) => value !== this.resetValue),
        takeUntilDestroyed(),
      )
      .subscribe(() => {
        const updatedModel: any = diff(this.model(), this.previousValue);
        this.isUpdated.set(!isEmpty(updatedModel));
      });
  }

  ngOnInit() {
    this.initialized = true;
    this.isUpdated.set(false);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.hasOwnProperty('externalParamProfile') && this.initialized) {
      this.resetForm(this.externalParamProfile(), this.readOnly());
      this.previousValue = this.model();
    }
  }

  private resetForm(externalParamProfile: ExternalParamProfile, readOnly: boolean) {
    this.resetValue = {
      usePlatformThreshold: externalParamProfile?.usePlatformThreshold ?? null,
      bulkOperationsThreshold: externalParamProfile?.bulkOperationsThreshold ?? null,
    };
    this.model.set(this.resetValue);
    this.form().reset();
    this.formReadOnly.set(!!readOnly);
  }

  submitModification() {
    let updated: any = diff(this.model(), this.previousValue);
    if (!isEmpty(updated)) {
      updated = extend(
        {
          id: this.externalParamProfile().id,
          idExternalParam: this.externalParamProfile().idExternalParam,
          idProfile: this.externalParamProfile().idProfile,
        },
        updated,
      );
      if (updated.usePlatformThreshold) {
        delete updated.bulkOperationsThreshold;
      }

      this.externalParamProfileService.patch(updated).subscribe((externalParamProfile: ExternalParamProfile) => {
        this.resetForm(externalParamProfile, this.readOnly());
        this.isUpdated.set(false);
      });
    }
  }
}
