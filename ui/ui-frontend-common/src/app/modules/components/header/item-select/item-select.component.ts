import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MenuOption } from '../../navbar/customer-menu/menu-option.interface';

@Component({
  selector: 'vitamui-common-item-select',
  templateUrl: './item-select.component.html',
  styleUrls: ['./item-select.component.scss']
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

  // tslint:disable-next-line: variable-name
  public _selectedItem: string;

  constructor() { }

  public selectItem(itemLabel: string): void {
    const item = this.items.find(value => value.label === itemLabel);
    this.itemSelected.emit(item);
  }
}
