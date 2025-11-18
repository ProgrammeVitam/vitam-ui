import {
  Component,
  EmbeddedViewRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarConfig, MatSnackBarRef } from '@angular/material/snack-bar';
import { merge, Observable, Subscription } from 'rxjs';
import { filter, map, startWith, switchMap, tap } from 'rxjs/operators';
import { ArchiveUnit, ArchiveUnitEditorComponent, JsonPatch } from 'ui-frontend-common';
import { EditObject } from 'ui-frontend-common/app/modules/object-editor/models/edit-object.model';
import { SpinnerOverlayService } from 'vitamui-library';
import { ArchiveUnitService } from './archive-unit.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-archive-unit-description-tab',
  templateUrl: './archive-unit-description-tab.component.html',
  styleUrls: ['./archive-unit-description-tab.component.scss'],
})
export class ArchiveUnitDescriptionTabComponent implements OnChanges, OnDestroy {
  @Input() archiveUnit: ArchiveUnit;
  @Input() editMode = false;
  @Input() transactionId: string;
  @Output() editModeChange = new EventEmitter<boolean>();

  @ViewChild('savingOK') savingOK: TemplateRef<any>;
  @ViewChild('updateDialog') updateDialog: TemplateRef<ArchiveUnitDescriptionTabComponent>;
  @ViewChild('cancelDialog') cancelDialog: TemplateRef<ArchiveUnitDescriptionTabComponent>;

  archiveUnitEditor: ArchiveUnitEditorComponent;
  editObject: EditObject;
  canSave = false;
  snackBarRef: MatSnackBarRef<EmbeddedViewRef<any>>;

  private readonly subscriptions = new Subscription();
  private readonly dialogConfig: MatDialogConfig = { panelClass: 'vitamui-dialog' };
  private readonly snackBarConfig: MatSnackBarConfig = {
    panelClass: 'vitamui-snack-bar',
    duration: 10000,
  };

  constructor(
    private dialog: MatDialog,
    private archiveUnitService: ArchiveUnitService,
    private snackBar: MatSnackBar,
    private spinnerOverlayService: SpinnerOverlayService,
    private translateService: TranslateService,
  ) {}

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['editMode']) {
      this.updateCanSave();
    }
  }

  @ViewChild(ArchiveUnitEditorComponent) set editor(editor: ArchiveUnitEditorComponent) {
    if (editor) {
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
              return merge(editObject?.control.valueChanges, editObject?.control.statusChanges).pipe(startWith(null));
            }),
          )
          .subscribe(() => this.updateCanSave()),
      );
    }
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

  async onCancel() {
    if (!this.isModified()) {
      this.backToDisplayMode();
    } else {
      await this.dialog
        .open(this.cancelDialog, this.dialogConfig)
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

      const missingFieldsString = missingFields.join(', ');
      const message = this.translateService.instant('ARCHIVE_UNIT.REQUIRED_FIELDS', { missingFieldsString });
      this.snackBar.open(message, 'close', this.snackBarConfig);
      return;
    }

    this.subscriptions.add(
      this.dialog
        .open(this.updateDialog, this.dialogConfig)
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
    this.canSave = false;
    this.editModeChange.emit(this.editMode);
  }
}
