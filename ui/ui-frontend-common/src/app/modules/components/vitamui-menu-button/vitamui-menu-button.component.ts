import { Component, Input } from '@angular/core';

@Component({
  selector: 'vitamui-common-menu-button',
  templateUrl: './vitamui-menu-button.component.html',
  styleUrls: ['./vitamui-menu-button.component.scss']
})
export class VitamuiMenuButtonComponent {

  /** Set the menu overlay display (by defaut at start) */
  @Input() overlayPos: 'start' | 'end' = 'start';

  /**
   * Set the icon to display on the button.
   * If there is only an icon, the button will be rounded.
   * WARNING : ONLY USE VITAMUI ICONS, MATERIAL ICONS WONT WORK
   */
  @Input() icon: string;

  /**
   * Set the label that will be displayed on the button.
   * Can be combined with an icon.
   */
  @Input() label: string;

  constructor() { }

}
