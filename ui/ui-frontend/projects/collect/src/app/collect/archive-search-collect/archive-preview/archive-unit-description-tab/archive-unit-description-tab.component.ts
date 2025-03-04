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
import { Component, EmbeddedViewRef, EventEmitter, Input, OnDestroy, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarConfig, MatSnackBarRef } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { filter, map, switchMap, tap } from 'rxjs/operators';
import { ArchiveUnit, ArchiveUnitEditorComponent, EditObject, JsonPatch, SpinnerOverlayService } from 'vitamui-library';
import { ArchiveUnitService } from './archive-unit.service';

@Component({
  selector: 'app-archive-unit-description-tab',
  templateUrl: './archive-unit-description-tab.component.html',
  styleUrls: ['./archive-unit-description-tab.component.scss'],
  standalone: false,
})
export class ArchiveUnitDescriptionTabComponent implements OnDestroy {
  @Input() archiveUnit: ArchiveUnit;
  @Input() editMode = false;
  @Input() transactionId: string;
  @Output() editModeChange = new EventEmitter<boolean>();

  @ViewChild('savingOK') savingOK: TemplateRef<any>;
  @ViewChild('updateDialog') updateDialog: TemplateRef<ArchiveUnitDescriptionTabComponent>;
  @ViewChild('cancelDialog') cancelDialog: TemplateRef<ArchiveUnitDescriptionTabComponent>;

  archiveUnitEditor: ArchiveUnitEditorComponent;
  editObject: EditObject;
  snackBarRef: MatSnackBarRef<EmbeddedViewRef<any>>;

  private readonly subscriptions = new Subscription();
  private readonly snackBarConfig: MatSnackBarConfig = {
    duration: 10000,
  };

  constructor(
    private dialog: MatDialog,
    private archiveUnitService: ArchiveUnitService,
    private snackBar: MatSnackBar,
    private spinnerOverlayService: SpinnerOverlayService,
  ) {}

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  @ViewChild(ArchiveUnitEditorComponent) set editor(editor: ArchiveUnitEditorComponent) {
    if (editor) {
      this.archiveUnitEditor = editor;

      const subscription = this.archiveUnitEditor?.editObject$.subscribe((editObject) => {
        this.editObject = editObject;
      });

      if (subscription) this.subscriptions.add(subscription);
    }
  }

  isModified(): boolean {
    return this.editMode && !this.editObject?.control?.pristine;
  }

  async onCancel() {
    if (!this.isModified()) {
      this.backToDisplayMode();
    } else {
      await this.dialog
        .open(this.cancelDialog)
        .afterClosed()
        .pipe(
          map((result) => {
            if (result) return this.archiveUnitEditor.getJsonPatch();
            throw new Error(result);
          }),
          tap(() => this.spinnerOverlayService.open()),
          switchMap((jsonPatchDto) => this.archiveUnitService.asyncPartialUpdateArchiveUnitByCommands(this.transactionId, jsonPatchDto)),
        )
        .toPromise()
        .then(() => this.handleUpdateSuccess())
        .catch(() => this.backToDisplayMode());
    }
  }

  onSave(): void {
    this.subscriptions.add(
      this.dialog
        .open(this.updateDialog)
        .afterClosed()
        .pipe(
          filter((result) => !!result),
          map(() => this.archiveUnitEditor.getJsonPatch()),
          tap(() => this.spinnerOverlayService.open()),
          switchMap((jsonPatchDto) => this.archiveUnitService.asyncPartialUpdateArchiveUnitByCommands(this.transactionId, jsonPatchDto)),
        )
        .subscribe(
          () => this.handleUpdateSuccess(),
          () => this.spinnerOverlayService.close(),
        ),
    );
  }

  private handleUpdateSuccess(): void {
    this.snackBarRef = this.snackBar.openFromTemplate(this.savingOK, this.snackBarConfig);

    this.patchUnit(this.archiveUnit, this.archiveUnitEditor.getJsonPatch().jsonPatch);

    this.backToDisplayMode();
  }

  private patchUnit(archiveUnit: ArchiveUnit, jsonPatch: JsonPatch) {
    jsonPatch.forEach((patch) => {
      const key = patch.path;
      switch (patch.op) {
        case 'add':
        case 'replace':
          archiveUnit[key] = patch.value;
          break;
        case 'remove':
          delete archiveUnit[key];
          break;
      }
    });
  }

  private backToDisplayMode(): void {
    this.spinnerOverlayService.close();
    this.editMode = false;
    this.editModeChange.emit(this.editMode);
  }
}
