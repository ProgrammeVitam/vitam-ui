import { Component, forwardRef, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FavoriteEntryService } from '../../../object-viewer/services/favorite-entry.service';
import { DisplayObjectType } from '../../../object-viewer/types';
import { EditObject } from '../../models/edit-object.model';
import { AppendStarPipe } from '../../required.pipe';
import { EmptyPipe } from '../../../pipes/empty.pipe';
import { TranslateModule } from '@ngx-translate/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { VitamuiRepeatableInputComponent } from '../../../components/vitamui-repeatable-input/vitamui-repeatable-input.component';
import { PrimitiveEditorComponent } from '../primitive-editor/primitive-editor.component';
import { NgClass, NgFor, NgIf, NgTemplateOutlet } from '@angular/common';
import { GroupEditorComponent } from '../group-editor/group-editor.component';

@Component({
  selector: 'vitamui-common-list-editor',
  templateUrl: './list-editor.component.html',
  styleUrls: ['./list-editor.component.scss'],
  standalone: true,
  imports: [
    NgIf,
    NgClass,
    NgTemplateOutlet,
    NgFor,
    PrimitiveEditorComponent,
    VitamuiRepeatableInputComponent,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    EmptyPipe,
    AppendStarPipe,
    forwardRef(() => GroupEditorComponent),
  ],
})
export class ListEditorComponent implements OnChanges {
  @Input() editObject: EditObject;

  favoriteEntry: [key: string, value: any];
  favoritePath: string;

  readonly DisplayObjectType = DisplayObjectType;

  constructor(private favoriteEntryService: FavoriteEntryService) {}

  ngOnChanges(changes: SimpleChanges): void {
    const { editObject } = changes;

    if (editObject) this.favoriteEntryService.favoriteEntry(this.editObject);
  }

  addFirst(): void {
    if (this.editObject.children.length === 0) this.editObject.actions.add.handler();
  }

  add(event: Event): void {
    this.editObject.actions.add.handler();
    event.stopPropagation();
  }

  removeAt(event: Event, i: number): void {
    this.editObject.actions.removeAt.handler(i);
    event.stopPropagation();
  }
}
