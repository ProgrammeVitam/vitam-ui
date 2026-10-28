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
import { Component, inject, OnInit, signal, output, input, model, OnChanges, SimpleChanges } from '@angular/core';
import { disabled, form, FormField, FormRoot, maxLength, minLength, pattern, required } from '@angular/forms/signals';
import { ActivatedRoute } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, mergeMap, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { diff, InputComponent, Rule, RuleService, SecurityService, SelectComponent, VitamuiRoles } from 'vitamui-library';
import { RULE_MEASUREMENTS, RULE_TYPES } from '../../rules.constants';
import { AsyncPipe } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';

const RULES_APP = 'RULES_APP';

interface RuleInformationForm {
  ruleType: string;
  ruleValue: string;
  ruleDescription: string;
  ruleDuration: string;
  ruleMeasurement: string;
}

@Component({
  selector: 'app-rule-information-tab',
  templateUrl: './rule-information-tab.component.html',
  styleUrls: ['./rule-information-tab.component.scss'],
  imports: [FormRoot, FormField, SelectComponent, InputComponent, AsyncPipe, TranslatePipe],
})
export class RuleInformationTabComponent implements OnChanges, OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly securityService = inject(SecurityService);
  private readonly ruleService = inject(RuleService);

  readonly updated = output<boolean>();

  private readonly formModel = signal<RuleInformationForm>({
    ruleType: null,
    ruleValue: null,
    ruleDescription: null,
    ruleDuration: null,
    ruleMeasurement: null,
  });
  readonly form = form(
    this.formModel,
    (path) => {
      disabled(path, () => !!this.readOnly());
      required(path.ruleType);
      required(path.ruleValue);
      minLength(path.ruleValue, 2);
      maxLength(path.ruleValue, 100);
      required(path.ruleDescription);
      required(path.ruleDuration);
      maxLength(path.ruleDuration, 3);
      pattern(path.ruleDuration, /^[0-9]*$/);
      required(path.ruleMeasurement);
    },
    {
      submission: {
        action: async () => {
          this.onSubmit();
        },
        ignoreValidators: 'all',
      },
    },
  );

  submited = signal(false);

  ruleTypes = RULE_TYPES;
  ruleMeasurements = RULE_MEASUREMENTS;

  tenantIdentifier: number;
  hasUpdateRuleRole$: Observable<boolean>;

  previousValue(): Rule {
    return this.rule();
  }

  readonly rule = model<Rule>(undefined);

  readonly readOnly = input<boolean>(undefined);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['rule']) {
      this.setRule(this.rule());
    }
  }

  ngOnInit() {
    this.hasUpdateRuleRole$ = this.route.params.pipe(
      mergeMap((params) => {
        this.tenantIdentifier = +params['tenantIdentifier'];
        return this.securityService.hasRole$(RULES_APP, VitamuiRoles.ROLE_UPDATE_RULES, this.tenantIdentifier);
      }),
    );
  }

  unchanged(): boolean {
    if (this.previousValue().ruleDescription === undefined && this.formModel().ruleDescription != null) {
      this.previousValue().ruleDescription = '';
    }
    const unchanged = JSON.stringify(diff(this.formModel(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    return (
      this.form.ruleType().invalid() ||
      this.form.ruleType().pending() ||
      this.form.ruleValue().invalid() ||
      this.form.ruleValue().pending() ||
      this.form.ruleDescription().invalid() ||
      this.form.ruleDescription().pending() ||
      this.form.ruleDuration().invalid() ||
      this.form.ruleDuration().pending() ||
      this.form.ruleMeasurement().invalid() ||
      this.form.ruleMeasurement().pending()
    );
  }

  prepareSubmit(): Observable<Boolean> {
    return of(diff(this.formModel(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().ruleId, ruleId: this.previousValue().ruleId }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) => this.ruleService.patch(formData).pipe(catchError(() => of(null)))),
    );
  }

  onSubmit() {
    this.submited.set(true);
    if (this.isInvalid()) {
      return;
    }

    this.prepareSubmit().subscribe(
      () => {
        this.ruleService.get(this.rule().ruleId).subscribe((response) => {
          this.submited.set(false);
          this.setRule(response);
        });
      },
      () => {
        this.submited.set(false);
      },
    );
  }

  private setRule(rule: Rule) {
    this.rule.set(rule);
    this.resetForm(this.rule());
    this.updated.emit(false);
  }

  resetForm(rule: Rule) {
    // Like reactive forms reset, the missing values are reset to null
    this.form().reset({
      ruleType: rule?.ruleType ?? null,
      ruleValue: rule?.ruleValue ?? null,
      ruleDescription: rule?.ruleDescription ?? null,
      ruleDuration: rule?.ruleDuration ?? null,
      ruleMeasurement: rule?.ruleMeasurement ?? null,
    });
    (Object.keys(this.formModel()) as (keyof RuleInformationForm)[]).forEach((key) => this.form[key]().markAsTouched());
  }
}
