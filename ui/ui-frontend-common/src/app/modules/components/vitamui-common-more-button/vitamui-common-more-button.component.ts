import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

declare interface MoreButtonAction {
  identifier: string;
  label: string;
  icon?: string;
}

@Component({
  selector: 'vitamui-common-more-button',
  templateUrl: './vitamui-common-more-button.component.html',
  styleUrls: ['./vitamui-common-more-button.component.scss']
})
export class VitamuiCommonMoreButtonComponent implements OnInit {

  /**
   * Sample (how to use) : the clicked action will be catched by the main component
   * and will process the good action thanks to its identifier.
   * This action array must be build and plugged in the calling component.
   */
  @Input() actions: MoreButtonAction[];

  /**
   * Event raised when an action from the more button is clicked.
   * Will contains the clicked action identifier so calling component can
   * catch and perform the required action.
   */
  @Output() actionBtnClick = new EventEmitter<string>();

  constructor() { }

  ngOnInit() {
  }

}
