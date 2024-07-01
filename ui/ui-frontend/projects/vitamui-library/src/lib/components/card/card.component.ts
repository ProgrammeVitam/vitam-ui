/* eslint-disable @angular-eslint/component-selector */
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatLegacyButtonModule } from '@angular/material/legacy-button';
import { NgClass, NgIf } from '@angular/common';
import { MatLegacyCardModule } from '@angular/material/legacy-card';

@Component({
  selector: 'vitamui-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.scss'],
  standalone: true,
  imports: [MatLegacyCardModule, NgClass, NgIf, MatLegacyButtonModule],
})
export class CardComponent {
  @Input()
  value: string;

  @Input()
  showAction = true;

  @Output()
  buttonClick = new EventEmitter<any>();

  buttonClicked() {
    this.buttonClick.emit();
  }
}
