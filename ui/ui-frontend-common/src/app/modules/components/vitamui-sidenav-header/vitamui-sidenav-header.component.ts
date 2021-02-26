import { EventEmitter, Input } from '@angular/core';
import { Component, Output } from '@angular/core';

@Component({
  selector: 'vitamui-common-sidenav-header',
  templateUrl: './vitamui-sidenav-header.component.html',
  styleUrls: ['./vitamui-sidenav-header.component.scss']
})
export class VitamuiSidenavHeaderComponent {

  @Input() icon: string;

  @Input() loading: boolean;

  @Input() badge: 'green' | 'grey' | 'orange' | 'red' | 'black';

  @Input() title: string;

  @Input() subtitle: string;

  @Output() onclose = new EventEmitter<undefined>();

  @Input() hasToolTipOnTitle: boolean;

  @Input() toolTipTitleText: string;

  @Input() toolTipTitleDuration: number;


  constructor() { }

}
