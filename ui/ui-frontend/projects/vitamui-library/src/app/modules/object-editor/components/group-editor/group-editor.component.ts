import { AfterViewInit, Component, forwardRef, Input, OnChanges, OnDestroy, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import {
  MatLegacyDialog as MatDialog,
  MatLegacyDialogConfig as MatDialogConfig,
  MatLegacyDialogModule,
} from '@angular/material/legacy-dialog';
import { of, Subscription } from 'rxjs';
import { filter, switchMap } from 'rxjs/operators';
import { FavoriteEntryService } from '../../../object-viewer/services/favorite-entry.service';
import { LayoutService } from '../../../object-viewer/services/layout.service';
import { TypeService } from '../../../object-viewer/services/type.service';
import { DisplayObjectType } from '../../../object-viewer/types';
import { Action, EditObject } from '../../models/edit-object.model';
import { AppendStarPipe } from '../../required.pipe';
import { EmptyPipe } from '../../../pipes/empty.pipe';
import { TranslateModule } from '@ngx-translate/core';
import { PrimitiveEditorComponent } from '../primitive-editor/primitive-editor.component';
import { MatLegacyMenuModule } from '@angular/material/legacy-menu';
import { VitamuiMenuButtonComponent } from '../../../components/vitamui-menu-button/vitamui-menu-button.component';
import { AccordionComponent } from '../../../components/accordion/accordion.component';
import { NgFor, NgIf, NgSwitch, NgSwitchCase, NgTemplateOutlet } from '@angular/common';
import { ListEditorComponent } from '../list-editor/list-editor.component';

@Component({
  selector: 'vitamui-common-group-editor',
  templateUrl: './group-editor.component.html',
  styleUrls: ['./group-editor.component.scss'],
  standalone: true,
  imports: [
    NgIf,
    AccordionComponent,
    VitamuiMenuButtonComponent,
    NgFor,
    MatLegacyMenuModule,
    NgTemplateOutlet,
    NgSwitch,
    NgSwitchCase,
    PrimitiveEditorComponent,
    MatLegacyDialogModule,
    TranslateModule,
    EmptyPipe,
    AppendStarPipe,
    forwardRef(() => ListEditorComponent),
  ],
})
export class GroupEditorComponent implements OnChanges, AfterViewInit, OnDestroy {
  @Input() editObject: EditObject;

  @ViewChild('removeDialog') removeDialog: TemplateRef<GroupEditorComponent>;

  entries: [key: string, value: any][] = [];
  favoriteEntry: [key: string, value: any];
  favoritePath: string;
  rows: EditObject[][] = [[]];
  actionList: Action[] = [];

  readonly DisplayObjectType = DisplayObjectType;
  private readonly dialogConfig: MatDialogConfig = { panelClass: 'vitamui-dialog', width: '800px', autoFocus: false };
  private subscription: Subscription;

  constructor(
    private layoutService: LayoutService,
    private favoriteEntryService: FavoriteEntryService,
    private typeService: TypeService,
    private matDialog: MatDialog,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    const { editObject } = changes;

    if (editObject) {
      if (this.editObject?.childrenChange) {
        if (this.subscription) this.subscription.unsubscribe();
        this.subscription = this.editObject.childrenChange.subscribe(() => {
          this.computeLayout();
          this.computeActions();
        });
      }

      this.computeLayout();
    }
  }

  ngAfterViewInit(): void {
    this.computeActions();
  }

  ngOnDestroy(): void {
    if (this.subscription) this.subscription.unsubscribe();
  }

  computeLayout() {
    this.favoriteEntry = this.favoriteEntryService.favoriteEntry(this.editObject);
    this.favoritePath = this.favoriteEntryService.favoritePath(this.editObject);
    this.rows = this.layoutService.compute(this.editObject) as EditObject[][];
  }

  computeActions() {
    if (!this.editObject?.actions) return;

    const current: Action = this.editObject.actions.remove;
    if (current) {
      const next: Action = this.withValidation(current, () => this.typeService.isConsistent(this.editObject.control.value));
      this.replaceAction(current, next);
    }

    this.actionList = Object.values(this.editObject.actions);
  }

  toggle(): void {
    this.editObject.open = !this.editObject.open;
  }

  stopPropagation(event: Event) {
    event.stopPropagation();
  }

  private withValidation(action: Action, predicate: () => boolean): Action {
    const { label, handler } = action;

    return {
      name: `${action.name}WithValidation`,
      label,
      handler: () => {
        if (!predicate()) return handler();

        const subscription: Subscription = this.matDialog
          .open(this.removeDialog, this.dialogConfig)
          .afterClosed()
          .pipe(
            filter((value) => value),
            switchMap(() => of(handler())),
          )
          .subscribe(() => subscription.unsubscribe());
      },
    };
  }

  private replaceAction(current: Action, next: Action): void {
    delete this.editObject.actions[current.name];
    this.editObject.actions[next.name] = next;
  }
}
