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
import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges, TemplateRef, ViewChild, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { merge, Observable, Subscription } from 'rxjs';
import { filter, map, startWith, switchMap, tap } from 'rxjs/operators';
import { ArchiveUnit, ArchiveUnitEditorComponent, EditObject, JsonPatch, SnackBarService, SpinnerOverlayService } from 'vitamui-library';
import { ArchiveUnitService } from './archive-unit.service';

@Component({
  selector: 'app-archive-unit-description-tab',
  templateUrl: './archive-unit-description-tab.component.html',
  styleUrls: ['./archive-unit-description-tab.component.scss'],
  standalone: false,
})
export class ArchiveUnitDescriptionTabComponent implements OnChanges, OnDestroy {
  private dialog = inject(MatDialog);
  private archiveUnitService = inject(ArchiveUnitService);
  private spinnerOverlayService = inject(SpinnerOverlayService);
  private snackBarService = inject(SnackBarService);
  private translateService = inject(TranslateService);

  @Input() archiveUnit: ArchiveUnit;
  @Input() editMode = false;
  @Input() transactionId: string;
  @Output() editModeChange = new EventEmitter<boolean>();

  @ViewChild('updateDialog') updateDialog: TemplateRef<ArchiveUnitDescriptionTabComponent>;
  @ViewChild('cancelDialog') cancelDialog: TemplateRef<ArchiveUnitDescriptionTabComponent>;

  archiveUnitEditor: ArchiveUnitEditorComponent;
  editObject: EditObject;
  canSave = false;

  private readonly subscriptions = new Subscription();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['editMode']) {
      this.updateCanSave();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  @ViewChild(ArchiveUnitEditorComponent) set editor(editor: ArchiveUnitEditorComponent) {
    if (!editor) return;

    this.archiveUnitEditor = editor;

    this.subscriptions.add(
      this.archiveUnitEditor.editObject$
        .pipe(
          tap((editObject) => (this.editObject = editObject)),
          switchMap((editObject) => {
            if (!editObject?.control) {
              return new Observable<void>();
            }
            // Combine valueChanges and statusChanges into a single stream
            return merge(editObject.control.valueChanges, editObject.control.statusChanges).pipe(startWith(null));
          }),
        )
        .subscribe(() => this.updateCanSave()),
    );
  }

  private updateCanSave(): void {
    if (!this.editObject?.control) {
      this.canSave = false;
      return;
    }

    const isModified = this.isModified();
    const hasTitleFilled = this.isFieldFilled('Title');
    const hasDescriptionLevelFilled = this.isFieldFilled('DescriptionLevel');
    this.canSave = isModified && hasTitleFilled && hasDescriptionLevelFilled;
  }

  private isFieldFilled(fieldKey: string): boolean {
    const field = this.findFieldByKey(this.editObject, fieldKey);
    if (!field) {
      return true; // Si on ne trouve pas le champ, on autorise l'enregistrement
    }

    const fieldValue = field.control.value;
    return fieldValue !== null && fieldValue !== undefined && fieldValue !== '';
  }

  private findFieldByKey(editObj: EditObject, key: string): EditObject | null {
    if (editObj.key === key) {
      return editObj;
    }
    if (editObj.children) {
      for (const child of editObj.children) {
        const found = this.findFieldByKey(child, key);
        if (found) return found;
      }
    }
    return null;
  }

  isModified(): boolean {
    return this.editMode && !this.editObject?.control?.pristine;
  }

  async onCancel(cancelButtonClicked = false) {
    if (cancelButtonClicked && !this.isModified()) this.backToDisplayMode();
    if (!this.isModified() || this.dialog.openDialogs.length > 0) {
      return; // form not modified or dialog already open
    }
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

  onSave(): void {
    // Check if required fields are filled before opening dialog
    if (!this.canSave) {
      const titleFilled = this.isFieldFilled('Title');
      const descriptionLevelFilled = this.isFieldFilled('DescriptionLevel');

      const missingFields = [];
      if (!titleFilled) {
        missingFields.push(this.translateService.instant('COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.TITLE'));
      }
      if (!descriptionLevelFilled) {
        missingFields.push(this.translateService.instant('COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.DescriptionLevel'));
      }

      this.snackBarService.open({
        message: 'ARCHIVE_UNIT.REQUIRED_FIELDS',
        translateParams: { missingFields: missingFields.join(', ') },
        duration: 10_000,
      });
      return;
    }

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
    this.snackBarService.open({
      message: 'ARCHIVE_UNIT.DIALOGS.SAVE.MESSAGES.SUCCESS',
      duration: 10_000,
    });

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
    this.canSave = false;
    this.editModeChange.emit(this.editMode);
  }
}
