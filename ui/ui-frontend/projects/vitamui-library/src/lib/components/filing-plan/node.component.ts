/* tslint:disable:component-selector */
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

import {Node} from '../../models/node.interface';

@Component({
  selector: 'vitamui-library-node',
  templateUrl: './node.component.html',
  styleUrls: ['./node.component.scss']
})
export class NodeComponent implements OnInit {

  @Input() tenantIdentifier: number;
  @Input() node: Node;
  @Input() expanded: boolean;
  @Input() disabled: boolean;

  @Output() nodeToggle = new EventEmitter<void>();
  @Output() labelClick = new EventEmitter<void>();

  constructor() {
  }

  ngOnInit() {
  }

}
