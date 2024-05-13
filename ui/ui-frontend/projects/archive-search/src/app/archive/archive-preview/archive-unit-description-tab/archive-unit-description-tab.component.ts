import { Component, EventEmitter, Input, OnDestroy, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { filter, map, switchMap, tap } from 'rxjs/operators';
import { ArchiveUnit, ArchiveUnitEditorComponent, JsonPatch, Logger, SpinnerOverlayService, StartupService } from 'vitamui-library';
import { EditObject } from 'vitamui-library/app/modules/object-editor/models/edit-object.model';
import { VitamUISnackBarComponent } from '../../shared/vitamui-snack-bar';
import { ArchiveUnitService } from './archive-unit.service';

@Component({
  selector: 'app-archive-unit-description-tab',
  templateUrl: './archive-unit-description-tab.component.html',
  styleUrls: ['./archive-unit-description-tab.component.scss'],
})
export class ArchiveUnitDescriptionTabComponent implements OnDestroy {
  @Input() archiveUnit: ArchiveUnit;
  @Input() editMode = false;
  @Output() editModeChange = new EventEmitter<boolean>();

  @ViewChild('updateDialog') updateDialog: TemplateRef<ArchiveUnitDescriptionTabComponent>;
  @ViewChild('cancelDialog') cancelDialog: TemplateRef<ArchiveUnitDescriptionTabComponent>;

  archiveUnitEditor: ArchiveUnitEditorComponent;
  editObject: EditObject;

  private readonly subscriptions = new Subscription();
  private readonly dialogConfig: MatDialogConfig = { panelClass: 'vitamui-dialog', width: '800px', autoFocus: false };
  private readonly snackBarConfig: MatSnackBarConfig = {
    panelClass: 'vitamui-snack-bar',
    data: {
      type: 'WorkflowSuccessSnackBar',
    },
    duration: 100000,
  };

  constructor(
    private logger: Logger,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private startupService: StartupService,
    private translateService: TranslateService,
    private route: ActivatedRoute,
    private archiveUnitService: ArchiveUnitService,
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
        .open(this.cancelDialog, this.dialogConfig)
        .afterClosed()
        .pipe(
          map((result) => {
            if (result) return this.archiveUnitEditor.getJsonPatch();
            throw new Error(result);
          }),
          tap(() => this.spinnerOverlayService.open()),
          switchMap((jsonPatchDto) => this.archiveUnitService.asyncPartialUpdateArchiveUnitByCommands(jsonPatchDto)),
        )
        .toPromise()
        .then(({ operationId }) => this.handleUpdateSuccess({ operationId }))
        .catch(() => this.backToDisplayMode());
    }
  }

  onSave(): void {
    this.subscriptions.add(
      this.dialog
        .open(this.updateDialog, this.dialogConfig)
        .afterClosed()
        .pipe(
          filter((result) => !!result),
          map(() => this.archiveUnitEditor.getJsonPatch()),
          tap(
            () => this.spinnerOverlayService.open(),
            (err) => this.logger.error(this, err),
          ),
          switchMap((jsonPatchDto) => this.archiveUnitService.asyncPartialUpdateArchiveUnitByCommands(jsonPatchDto)),
        )
        .subscribe(
          ({ operationId }) => this.handleUpdateSuccess({ operationId }),
          () => this.spinnerOverlayService.close(),
        ),
    );
  }

  private handleUpdateSuccess({ operationId }: { operationId: String }): void {
    const tenantId = this.route.snapshot.params.tenantIdentifier;

    if (!operationId) return this.logger.error(this, 'Operation id is mandatory to build logbook operation link');
    if (!tenantId) return this.logger.error(this, 'Tenant id is mandatory to build logbook operation link');

    const serviceUrl = `${this.startupService.getReferentialUrl()}/logbook-operation/tenant/${tenantId}?guid=${operationId}`;
    const translationKey = 'ARCHIVE_UNIT.DIALOGS.SAVE.MESSAGES.IN_PROGRESS';
    const message = this.translateService.instant(translationKey);

    this.snackBar.openFromComponent(VitamUISnackBarComponent, {
      ...this.snackBarConfig,
      data: {
        ...this.snackBarConfig.data,
        message,
        serviceUrl,
      },
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
    this.editModeChange.emit(this.editMode);
  }
}
