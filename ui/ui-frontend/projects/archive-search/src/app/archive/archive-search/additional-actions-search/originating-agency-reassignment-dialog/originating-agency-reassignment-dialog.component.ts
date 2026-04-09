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
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatRadioModule } from '@angular/material/radio';
import { TranslatePipe } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import {
  AgencyService,
  ConfirmDialogService,
  DialogHeaderComponent,
  Option,
  SelectComponent,
  VitamUICommonModule,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { ReassignmentMode } from '../../../models/reassign-request.interface';

@Component({
  selector: 'app-originating-agency-reassignment-dialog',
  templateUrl: './originating-agency-reassignment-dialog.component.html',
  styleUrls: ['./originating-agency-reassignment-dialog.component.scss'],
  imports: [
    CommonModule,
    TranslatePipe,
    MatDialogModule,
    DialogHeaderComponent,
    VitamUICommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatRadioModule,
    SelectComponent,
  ],
})
export class OriginatingAgencyReassignmentDialogComponent implements OnInit, OnDestroy {
  form: FormGroup;
  originatingAgenciesOptions: VitamuiSelectOptions = { options: [] };
  itemSelected: number;
  reassignmentMode: ReassignmentMode;
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<OriginatingAgencyReassignmentDialogComponent>,
    public dialog: MatDialog,
    private confirmDialogService: ConfirmDialogService,
    private agencyService: AgencyService,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      itemSelected: number;
      reassignmentMode: ReassignmentMode;
    },
  ) {}

  ngOnInit(): void {
    this.itemSelected = this.data.itemSelected;
    this.reassignmentMode = this.data.reassignmentMode;

    this.form = this.fb.group({
      entryOperationIds: ['', this.reassignmentMode === ReassignmentMode.BY_ID ? [] : [Validators.required]],
      propagateToObjectGroups: [true],
      sourceOriginatingAgency: [null, [Validators.required]],
      targetOriginatingAgency: [null, [Validators.required]],
    });

    this.agencyService
      .getOriginatingAgenciesAsOptions()
      .subscribe((opts: Option[]) => (this.originatingAgenciesOptions = { options: opts }));

    this.confirmDialogService
      .listenToEscapeKeyPress(this.dialogRef)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.onCancel());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onCancel(): void {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  reassign(): void {
    if (this.form.invalid) {
      return;
    }

    this.dialogRef.close({ ...this.form.getRawValue() });
  }

  get title(): string {
    return this.reassignmentMode === ReassignmentMode.BY_ID
      ? 'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.TITLE'
      : 'ARCHIVE_SEARCH.ENTRY_OPERATION_REASSIGNMENT.TITLE';
  }

  get subhead(): string {
    return this.reassignmentMode === ReassignmentMode.BY_ID
      ? 'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.SUBTITLE'
      : 'ARCHIVE_SEARCH.ENTRY_OPERATION_REASSIGNMENT.SUBTITLE';
  }

  get sectionTitle(): string {
    return this.reassignmentMode === ReassignmentMode.BY_ID
      ? 'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.SOURCE_ORIGINATING_AGENCY_LABEL'
      : 'ARCHIVE_SEARCH.ENTRY_OPERATION_REASSIGNMENT.SOURCE_ORIGINATING_AGENCY_LABEL';
  }

  protected readonly ReassignmentMode = ReassignmentMode;
}
