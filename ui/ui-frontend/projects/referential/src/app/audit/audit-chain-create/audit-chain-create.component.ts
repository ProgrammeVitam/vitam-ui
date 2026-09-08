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
import { CommonModule } from '@angular/common';
import { HttpHeaders } from '@angular/common/http';
import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';
import {
  ConfirmDialogService,
  ExternalParameters,
  ExternalParametersService,
  Option,
  SelectComponent,
  SnackBarService,
  VitamUICommonModule,
  VitamuiHttpHeaders,
  VitamUILibraryModule,
} from 'vitamui-library';
import { AuditChainType, TraceabilityChainAuditRequest } from '../../models/audit.interface';
import { AuditService } from '../audit.service';

const CURRENT_VERSION_KEY = 'CURRENT';

@Component({
  selector: 'app-audit-chain-create',
  templateUrl: './audit-chain-create.component.html',
  styleUrls: ['./audit-chain-create.component.scss'],
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    ReactiveFormsModule,
    SelectComponent,
    TranslatePipe,
    VitamUICommonModule,
    VitamUILibraryModule,
  ],
})
export class AuditChainCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<AuditChainCreateComponent>>(MatDialogRef);
  private formBuilder = inject(FormBuilder);
  private confirmDialogService = inject(ConfirmDialogService);
  private auditService = inject(AuditService);
  private externalParameterService = inject(ExternalParametersService);
  private snackBarService = inject(SnackBarService);

  public form: FormGroup;
  public versionControl = new FormControl({ value: CURRENT_VERSION_KEY, disabled: true });
  public isDisabledButton = false;
  public accessContractId: string = null;

  public chainTypeOptions: Option[];
  public versionOptions: Option[];

  private destroyer$ = new Subject<void>();

  constructor() {
    const translateService = inject(TranslateService);

    this.chainTypeOptions = Object.keys(AuditChainType).map((key) => ({
      key: (AuditChainType as Record<string, string>)[key],
      label: translateService.instant(`AUDIT.CHAIN_CREATE_DIALOG.CHAIN_TYPES.${key}`),
    }));

    this.versionOptions = [{ key: CURRENT_VERSION_KEY, label: translateService.instant('AUDIT.CHAIN_CREATE_DIALOG.CURRENT_VERSION') }];
  }

  ngOnInit() {
    this.form = this.formBuilder.group({
      chainType: [null, Validators.required],
      wholeChain: [false],
      startDate: [null],
      endDate: [null],
    });

    this.confirmDialogService
      .listenToEscapeKeyPress(this.dialogRef)
      .pipe(takeUntil(this.destroyer$))
      .subscribe(() => this.onCancel());

    this.externalParameterService
      .getUserExternalParameters()
      .pipe(take(1))
      .subscribe((params: Map<string, string>) => {
        const accessContractId = params.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
        if (!accessContractId || accessContractId.length < 1) {
          this.snackBarService.open({ message: 'SNACKBAR.NO_ACCESS_CONTRACT_LINKED' });
          return;
        }
        this.accessContractId = accessContractId;
      });
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public showVersionParagraph(): boolean {
    return !!this.form?.get('chainType')?.value;
  }

  public isStepOneValid(): boolean {
    return !this.form.get('chainType').invalid;
  }

  public canSubmit(): boolean {
    const value = this.form.value;
    return !!this.accessContractId && (value.wholeChain === true || !!value.startDate || !!value.endDate);
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.form.invalid || !this.canSubmit()) {
      return;
    }
    this.isDisabledButton = true;

    const request: TraceabilityChainAuditRequest = {
      chainType: this.form.value.chainType,
      wholeChain: this.form.value.wholeChain,
      startDate: this.form.value.startDate,
      endDate: this.form.value.endDate,
    };

    const headers = new HttpHeaders().set(VitamuiHttpHeaders.X_ACCESS_CONTRACT_ID, this.accessContractId);

    this.auditService.createChainAudit(request, headers).subscribe({
      next: () => {
        this.isDisabledButton = false;
        this.dialogRef.close({ success: true });
      },
      error: () => {
        this.isDisabledButton = false;
        this.dialogRef.close({ success: false });
      },
    });
  }
}
