/* eslint-disable @angular-eslint/component-selector */
import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Node } from '../../models/node.interface';
import { FormsModule } from '@angular/forms';
import { MatLegacyCheckboxModule } from '@angular/material/legacy-checkbox';
import { MatLegacyButtonModule } from '@angular/material/legacy-button';

@Component({
  selector: 'vitamui-library-node',
  templateUrl: './node.component.html',
  styleUrls: ['./node.component.scss'],
  standalone: true,
  imports: [MatLegacyButtonModule, MatLegacyCheckboxModule, FormsModule],
})
export class NodeComponent {
  @Input() tenantIdentifier: number;
  @Input() node: Node;
  @Input() expanded: boolean;
  @Input() disabled: boolean;

  @Output() nodeToggle = new EventEmitter<void>();
  @Output() labelClick = new EventEmitter<void>();
}
