import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FavoriteEntryService } from '../../../object-viewer/services/favorite-entry.service';
import { LayoutService } from '../../../object-viewer/services/layout.service';
import { DisplayObjectType } from '../../../object-viewer/types';
import { EditObject } from '../../models/edit-object.model';

@Component({
  selector: 'vitamui-common-group-editor',
  templateUrl: './group-editor.component.html',
  styleUrls: ['./group-editor.component.scss'],
})
export class GroupEditorComponent implements OnChanges {
  @Input() editObject: EditObject;

  entries: [key: string, value: any][] = [];
  favoriteEntry: [key: string, value: any];
  favoritePath: string;
  rows: EditObject[][] = [[]];

  readonly DisplayObjectType = DisplayObjectType;

  constructor(
    private layoutService: LayoutService,
    private favoriteEntryService: FavoriteEntryService,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    const { editObject } = changes;

    if (editObject) {
      this.favoriteEntry = this.favoriteEntryService.favoriteEntry(this.editObject);
      this.favoritePath = this.favoriteEntryService.favoritePath(this.editObject);
      this.rows = this.layoutService.compute(this.editObject) as EditObject[][];
    }
  }

  toggle(): void {
    this.editObject.open = !this.editObject.open;
  }
}
