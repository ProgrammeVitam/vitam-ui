import { Component, EmbeddedViewRef, EventEmitter, Input, OnDestroy, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarConfig, MatSnackBarRef } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { filter, map, switchMap, tap } from 'rxjs/operators';
import { ArchiveUnit, ArchiveUnitEditorComponent, JsonPatch } from 'ui-frontend-common';
import { EditObject } from 'ui-frontend-common/app/modules/object-editor/models/edit-object.model';
import { SpinnerOverlayService } from 'vitamui-library';
import { ArchiveUnitService } from './archive-unit.service';

@Component({
  selector: 'app-archive-unit-description-tab',
  templateUrl: './archive-unit-description-tab.component.html',
  styleUrls: ['./archive-unit-description-tab.component.scss'],
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

  onCancel(): void {
    if (this.editObject?.control?.pristine) return this.backToDisplayMode();

    this.subscriptions.add(
      this.dialog
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
        .subscribe(
          () => this.handleUpdateSuccess(),
          () => this.backToDisplayMode(),
        ),
    );
  }

  onSave(): void {
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
    this.editModeChange.emit(this.editMode);
  }
}