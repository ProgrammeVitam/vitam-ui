import { Component, EventEmitter, Input, OnDestroy, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subscription, throwError } from 'rxjs';
import { filter, switchMap } from 'rxjs/operators';
import { ArchiveUnit, ArchiveUnitEditorComponent, Logger, StartupService } from 'ui-frontend-common';
import { EditObject } from 'ui-frontend-common/app/modules/object-editor/models/edit-object.model';
import { VitamUISnackBarComponent } from '../../shared/vitamui-snack-bar';

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
  private readonly dialogConfig: MatDialogConfig = { panelClass: 'vitamui-dialog' };
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
          switchMap((result) => {
            if (result) return this.archiveUnitEditor.update();

            return throwError(result);
          }),
        )
        .subscribe(
          ({ operationId }) => this.handleUpdateSuccess({ operationId }),
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
          switchMap(() => this.archiveUnitEditor.update()),
        )
        .subscribe(({ operationId }) => this.handleUpdateSuccess({ operationId })),
    );
  }

  private handleUpdateSuccess({ operationId }: { operationId: String }): void {
    const tenantId = this.route.snapshot.params.tenantIdentifier;

    if (!operationId) return this.logger.error(this, 'Operation id is mandatory to build logbook operation link');
    if (!tenantId) return this.logger.error(this, 'Tenant id is mandatory to build logbook operation link');

    const serviceUrl = `${this.startupService.getReferentialUrl()}/logbook-operation/tenant/${tenantId}?guid=${operationId}`;
    const translationKey = 'ARCHIVE_UNIT.DIALOGS.SAVE.MESSAGES.SUCCESS';
    const message = this.translateService.instant(translationKey);

    this.snackBar.openFromComponent(VitamUISnackBarComponent, {
      ...this.snackBarConfig,
      data: {
        ...this.snackBarConfig.data,
        message,
        serviceUrl,
      },
    });
    this.editObject.control.markAsPristine();
  }

  private backToDisplayMode(): void {
    this.archiveUnit = { ...this.archiveUnit };
    this.editMode = false;
    this.editModeChange.emit(this.editMode);
  }
}
