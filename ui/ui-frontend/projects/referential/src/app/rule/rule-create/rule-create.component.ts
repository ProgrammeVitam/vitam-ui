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
import { Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { ConfirmDialogService } from '../../../../../vitamui-library/src/app/modules/components/common-confirm-dialog/confirm-dialog.service';
import { ManagementRuleValidators } from '../../../../../vitamui-library/src/lib/validators/management-rule.validators';
import { Rule } from '../../../../../vitamui-library/src/lib/models/rule';
import { RuleService } from '../../../../../vitamui-library/src/app/modules/rule/rule.service';
import { RULE_MEASUREMENTS, RULE_TYPES } from '../rules.constants';
import { RuleCreateValidators } from './rule-create.validators';
import { sizes } from '../../ontology/ontology-form-options';

@Component({
  selector: 'app-rule-create',
  templateUrl: './rule-create.component.html',
  styleUrls: ['./rule-create.component.scss'],
  standalone: false,
})
export class RuleCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<RuleCreateComponent>>(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  private formBuilder = inject(FormBuilder);
  private confirmDialogService = inject(ConfirmDialogService);
  private ruleService = inject(RuleService);
  private ruleCreateValidator = inject(RuleCreateValidators);

  form: FormGroup;
  hasCustomGraphicIdentity = false;
  hasError = true;
  message: string;

  private keyPressSubscription: Subscription;

  ruleTypes = RULE_TYPES;
  ruleMeasurements = RULE_MEASUREMENTS;
  isDisabledButton = false;

  @ViewChild('fileSearch', { static: false }) fileSearch: any;
  tenantIdentifier: number;

  ngOnInit() {
    this.form = this.formBuilder.group({
      ruleId: [
        null,
        [Validators.required, Validators.minLength(2), Validators.maxLength(100), ManagementRuleValidators.ruleIdPattern],
        this.ruleCreateValidator.uniqueRuleId(),
      ],
      ruleType: [null, Validators.required],
      ruleValue: [null, [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      ruleDescription: [null, Validators.required],
      ruleDuration: [null, [Validators.required, Validators.maxLength(3), Validators.pattern('[0-9]*')]],
      ruleMeasurement: [null, Validators.required],
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.form.invalid) {
      this.isDisabledButton = true;
      return;
    }

    const format: Rule = this.form.value;
    this.isDisabledButton = true;

    this.ruleService.create(format).subscribe(
      () => {
        this.isDisabledButton = false;

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
