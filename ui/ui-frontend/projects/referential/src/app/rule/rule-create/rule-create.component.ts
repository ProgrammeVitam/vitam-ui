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
import { Component, inject, OnDestroy, OnInit, signal, viewChild } from '@angular/core';
import { form, FormField, FormRoot, maxLength, minLength, pattern, required } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import {
  ConfirmDialogService,
  DialogHeaderComponent,
  InputComponent,
  ruleIdPattern,
  Rule,
  RuleService,
  SelectComponent,
  TooltipDirective,
} from 'vitamui-library';
import { RULE_MEASUREMENTS, RULE_TYPES } from '../rules.constants';
import { RuleCreateValidators } from './rule-create.validators';
import { sizes } from '../../ontology/ontology-form-options';
import { TranslatePipe } from '@ngx-translate/core';

interface RuleCreateForm {
  ruleId: string;
  ruleType: string;
  ruleValue: string;
  ruleDescription: string;
  ruleDuration: string;
  ruleMeasurement: string;
}

@Component({
  selector: 'app-rule-create',
  templateUrl: './rule-create.component.html',
  styleUrls: ['./rule-create.component.scss'],
  imports: [
    DialogHeaderComponent,
    FormRoot,
    FormField,
    MatDialogContent,
    InputComponent,
    TooltipDirective,
    SelectComponent,
    MatDialogActions,
    TranslatePipe,
  ],
})
export class RuleCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<RuleCreateComponent>>(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly ruleService = inject(RuleService);
  private readonly ruleCreateValidator = inject(RuleCreateValidators);

  readonly form = form(
    signal<RuleCreateForm>({
      ruleId: null,
      ruleType: null,
      ruleValue: null,
      ruleDescription: null,
      ruleDuration: null,
      ruleMeasurement: null,
    }),
    (path) => {
      required(path.ruleId);
      minLength(path.ruleId, 2);
      maxLength(path.ruleId, 100);
      ruleIdPattern(path.ruleId);
      this.ruleCreateValidator.uniqueRuleId(path.ruleId);
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
  hasCustomGraphicIdentity = false;
  hasError = true;
  message: string;

  private keyPressSubscription: Subscription;

  ruleTypes = RULE_TYPES;
  ruleMeasurements = RULE_MEASUREMENTS;
  isDisabledButton = signal(false);

  readonly fileSearch = viewChild<any>('fileSearch');
  tenantIdentifier: number;

  ngOnInit() {
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.form().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.form().invalid()) {
      this.isDisabledButton.set(true);
      return;
    }

    const format = this.form().value() as Rule;
    this.isDisabledButton.set(true);

    this.ruleService.create(format).subscribe(
      () => {
        this.isDisabledButton.set(false);

        this.dialogRef.close({ success: true });
      },
      (error: any) => {
        this.dialogRef.close({ success: false });
        console.error(error);
      },
    );
  }

  protected readonly sizes = sizes;
}
