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

  toggle(): void {
    this.editObject.open = !this.editObject.open;
  }

  add(event: Event): void {
    this.editObject.actions.add();
    event.stopPropagation();
  }

  removeAt(event: Event, i: number): void {
    this.editObject.actions.removeAt(i);
  }
}
