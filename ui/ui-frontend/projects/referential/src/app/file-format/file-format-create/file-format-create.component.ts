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
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import {
  ConfirmDialogService,
  FILE_FORMAT_EXTERNAL_PREFIX,
  FileFormat,
  Option,
  StartupService,
  VitamuiSelectOptions,
  DialogHeaderComponent,
  InputComponent,
  SelectComponent,
} from 'vitamui-library';
import { FileFormatService } from '../file-format.service';
import { FileFormatCreateValidators } from './file-format-create.validators';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-file-format-create',
  templateUrl: './file-format-create.component.html',
  styleUrls: ['./file-format-create.component.scss'],
  imports: [
    DialogHeaderComponent,
    ReactiveFormsModule,
    CdkScrollable,
    MatDialogContent,
    InputComponent,
    SelectComponent,
    MatDialogActions,
    TranslatePipe,
  ],
})
export class FileFormatCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<FileFormatCreateComponent>>(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  private formBuilder = inject(FormBuilder);
  private startupService = inject(StartupService);
  private confirmDialogService = inject(ConfirmDialogService);
  private fileFormatService = inject(FileFormatService);
  private fileFormatCreateValidators = inject(FileFormatCreateValidators);

  form: FormGroup;
  tenantIdentifier: string;
  hasPriorityOverFileFormatIDsOptions: VitamuiSelectOptions;
  hasCustomGraphicIdentity = false;
  hasError = true;
  message: string;
  isCreationPending = false;
  isDisabledButton = false;

  private keyPressSubscription: Subscription;

  @ViewChild('fileSearch', { static: false }) fileSearch: any;

  constructor() {
    this.tenantIdentifier = this.startupService.getTenantIdentifier();
  }

  ngOnInit() {
    this.form = this.formBuilder.group({
      name: [null, [Validators.required, Validators.minLength(2), Validators.maxLength(100)], this.fileFormatCreateValidators.uniqueName()],
      puid: [null, [Validators.required, Validators.minLength(2), Validators.maxLength(100)], this.fileFormatCreateValidators.uniquePuid()],
      version: [null, Validators.required],
      mimeType: [null],
      extensions: [null, Validators.required],
      hasPriorityOverFileFormatIDs: [null],
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());

    this.fileFormatService
      .getAllForTenant(this.tenantIdentifier)
      .pipe(
        map((fileFormats: FileFormat[]) => {
          const options: Option[] = fileFormats.map((fileFormat) => ({
            label: fileFormat.puid + ' - ' + fileFormat.name,
            key: fileFormat.puid,
          }));
          return { options };
        }),
        tap((options: VitamuiSelectOptions) => (this.hasPriorityOverFileFormatIDsOptions = options)),
      )
      .subscribe();
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
    this.isDisabledButton = true;
    const format: FileFormat = this.form.value;
    format.puid = FILE_FORMAT_EXTERNAL_PREFIX + this.form.value.puid;

    // Disable the submit button to prevent double submit
    this.isCreationPending = true;
    this.fileFormatService.create(format).subscribe(
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
}
