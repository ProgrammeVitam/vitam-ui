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
import { HttpHeaders } from '@angular/common/http';
import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import {
  ConfirmDialogService,
  DialogHeaderComponent,
  ExternalParameters,
  ExternalParametersService,
  InputComponent,
  Option,
  SearchResponse,
  SearchUnitApiService,
  SelectComponent,
  SigningRoleType,
  SnackBarService,
  VitamuiHttpHeaders,
} from 'vitamui-library';
import { ProbativeValueService } from '../probative-value.service';
import { sizes } from '../../ontology/ontology-form-options';
import { MatButtonToggle, MatButtonToggleGroup } from '@angular/material/button-toggle';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-probative-value-create',
  templateUrl: './probative-value-create.component.html',
  styleUrls: ['./probative-value-create.component.scss'],
  imports: [
    DialogHeaderComponent,
    ReactiveFormsModule,
    MatDialogContent,
    InputComponent,
    SelectComponent,
    MatButtonToggleGroup,
    MatButtonToggle,
    MatDialogActions,
    TranslatePipe,
  ],
})
export class ProbativeValueCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<ProbativeValueCreateComponent>>(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  private formBuilder = inject(FormBuilder);
  private confirmDialogService = inject(ConfirmDialogService);
  private probativeValueService = inject(ProbativeValueService);
  private externalParameterService = inject(ExternalParametersService);
  private searchUnitApiService = inject(SearchUnitApiService);
  private snackBarService = inject(SnackBarService);

  public form: FormGroup;
  public isDisabledButton = false;

  public usages: Option[] = [
    { key: 'BinaryMaster', label: 'Archives numériques originales', info: '' },
    { key: 'Dissemination', label: 'Copies de diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignette', info: '' },
    { key: 'TextContent', label: 'Contenu textuel', info: '' },
    { key: 'PhysicalMaster', label: 'Archives physiques', info: '' },
  ];

  private accessContractId: string;
  showWarningMessage = false;

  private destroyer$ = new Subject<void>();

  ngOnInit() {
    this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
      const accessContractId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessContractId && accessContractId.length > 0) {
        this.accessContractId = accessContractId;
      } else {
        this.snackBarService.open({ message: 'PROBATIVE_VALUE.CREATE_DIALOG.ACCESS_CONTACT_NOT_SET' });
      }
    });

    this.form = this.formBuilder.group({
      unitId: [null, [Validators.required, Validators.minLength(36), Validators.maxLength(36)]],
      usage: [null, Validators.required],
      version: [null, Validators.required],
      includeDetachedSigningInformation: [false, Validators.required],
    });

    this.confirmDialogService
      .listenToEscapeKeyPress(this.dialogRef)
      .pipe(takeUntil(this.destroyer$))
      .subscribe(() => this.onCancel());

    this.form.get('unitId').statusChanges.subscribe((unitId) => {
      if (unitId === 'VALID' && this.form.get('includeDetachedSigningInformation').value) {
        this.complianceCheck();
      }
    });
  }

  ngOnDestroy = () => {
    this.destroyer$.next();
    this.destroyer$.complete();
  };

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
    this.isDisabledButton = true;

    this.probativeValueService
      .create(this.createDsl(this.form.value), new HttpHeaders().set(VitamuiHttpHeaders.X_ACCESS_CONTRACT_ID, this.accessContractId))
      .subscribe(
        () => {
          this.isDisabledButton = false;

          this.dialogRef.close({ success: true, action: 'none' });
        },
        () => {
          this.dialogRef.close({ success: false, action: 'none' });
        },
      );
  }

  createDsl(values: any) {
    return {
      dslQuery: {
        $query: [
          {
            $or: [
              {
                $in: {
                  '#id': [values.unitId],
                },
              },
            ],
          },
        ],
        $filter: {},
        $projection: {},
      },
      usage: values.usage,
      version: values.version,
      includeDetachedSigningInformation: values.includeDetachedSigningInformation,
    };
  }

  public complianceCheck(): void {
    if (this.form.get('unitId').valid) {
      this.searchUnitApiService.getById(this.form.get('unitId').value).subscribe((unitIdStatus: SearchResponse) => {
        if (!unitIdStatus.$results[0]) {
          this.snackBarService.open({ message: 'EXCEPTIONS.HTTP_INTERCEPTOR.HTTP_STATUS_CODE_NOT_FOUND' });
        } else {
          this.showWarningMessage =
            !unitIdStatus.$results[0].SigningInformation ||
            !unitIdStatus.$results[0].SigningInformation.SigningRole.includes(SigningRoleType.SIGNED_DOCUMENT);
        }
      });
    }
  }

  protected readonly sizes = sizes;
}
