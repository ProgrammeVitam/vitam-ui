import { Component, Input, OnChanges, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { filter, switchMap } from 'rxjs/operators';
import { FavoriteEntryService } from '../../../object-viewer/services/favorite-entry.service';
import { LayoutService } from '../../../object-viewer/services/layout.service';
import { DisplayObjectType } from '../../../object-viewer/types';
import { Action, EditObject } from '../../models/edit-object.model';

@Component({
  selector: 'vitamui-common-group-editor',
  templateUrl: './group-editor.component.html',
  styleUrls: ['./group-editor.component.scss'],
})
export class GroupEditorComponent implements OnChanges {
  @Input() editObject: EditObject;

  @ViewChild('removeDialog') removeDialog: TemplateRef<GroupEditorComponent>;

  entries: [key: string, value: any][] = [];
  favoriteEntry: [key: string, value: any];
  favoritePath: string;
  rows: EditObject[][] = [[]];
  actionList: Action[] = [];

  readonly DisplayObjectType = DisplayObjectType;

  constructor(
    private layoutService: LayoutService,
    private favoriteEntryService: FavoriteEntryService,
    private matDialog: MatDialog,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    const { editObject } = changes;

    if (editObject) {
      this.favoriteEntry = this.favoriteEntryService.favoriteEntry(this.editObject);
      this.favoritePath = this.favoriteEntryService.favoritePath(this.editObject);
      this.rows = this.layoutService.compute(this.editObject) as EditObject[][];
      if (this.editObject.actions) {
        const removeAction: Action = this.editObject.actions.remove;

        if (removeAction) {
          const removeHandler = removeAction.handler;
          const removeActionWithValidationStep = () => {
            if (this.editObject.control.pristine) return removeHandler();

            const subscription = this.matDialog
              .open(this.removeDialog)
              .afterClosed()
              .pipe(
                filter((value) => value),
                switchMap(() => of(removeHandler())),
              )
              .subscribe(() => subscription.unsubscribe());
          };

          removeAction.handler = removeActionWithValidationStep;
        }

        this.actionList = Object.values(this.editObject.actions);
      }
    }
  }

  toggle(): void {
    this.editObject.open = !this.editObject.open;
  }

  stopPropagation(event: Event) {
    event.stopPropagation();
  }
}