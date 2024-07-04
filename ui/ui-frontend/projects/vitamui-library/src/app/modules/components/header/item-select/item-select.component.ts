import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MenuOption } from '../../navbar/customer-menu/menu-option.interface';
import { MatLegacyOptionModule } from '@angular/material/legacy-core';
import { MatLegacySelectModule } from '@angular/material/legacy-select';
import { NgFor, NgIf } from '@angular/common';
import { MatLegacyFormFieldModule } from '@angular/material/legacy-form-field';

@Component({
  selector: 'vitamui-common-item-select',
  templateUrl: './item-select.component.html',
  styleUrls: ['./item-select.component.scss'],
  standalone: true,
  imports: [MatLegacyFormFieldModule, NgIf, MatLegacySelectModule, NgFor, MatLegacyOptionModule],
})
export class ItemSelectComponent {
  @Input() label: string;

  @Input() selectedLabel: string;

  @Input() items: MenuOption[];

  @Output() itemSelected = new EventEmitter<MenuOption>();

  @Input() set selectedItem(value: MenuOption) {
    if (value) {
      this._selectedItem = value.label;
    }
  }

  public _selectedItem: string;

  constructor() {}

  public selectItem(itemLabel: string): void {
    const item = this.items.find((value) => value.label === itemLabel);
    this.itemSelected.emit(item);
  }
}
