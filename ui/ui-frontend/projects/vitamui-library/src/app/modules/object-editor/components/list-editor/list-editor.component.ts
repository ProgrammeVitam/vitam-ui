import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FavoriteEntryService } from '../../../object-viewer/services/favorite-entry.service';
import { DisplayObjectType } from '../../../object-viewer/types';
import { EditObject } from '../../models/edit-object.model';

@Component({
  selector: 'vitamui-common-list-editor',
  templateUrl: './list-editor.component.html',
  styleUrls: ['./list-editor.component.scss'],
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
