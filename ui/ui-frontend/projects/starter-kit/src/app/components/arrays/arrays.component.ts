import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, OnInit } from '@angular/core';
import { Direction, Group } from 'ui-frontend-common';

@Component({
  selector: 'app-arrays',
  templateUrl: './arrays.component.html',
  styleUrls: ['./arrays.component.scss'],
  animations: [
    trigger('expansion', [
      state('collapsed', style({height: '0px', visibility: 'hidden', opacity: '0'})),
      state('expanded', style({height: '*', visibility: 'visible',  opacity: '1'})),
      transition('expanded <=> collapsed', animate('150ms cubic-bezier(0.4,0.0,0.2,1)')),
    ]),

    trigger('arrow', [
      state('collapsed', style({transform: 'rotate(180deg)'})),
      state('expanded', style({transform: 'none'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4,0.0,0.2,1)')),
    ]),
  ]
})
export class ArraysComponent implements OnInit {

  public orderBy = 'name';
  public direction = Direction.ASCENDANT;
  public levelFilterOptions: Array<{ value: string, label: string }> = [];
  public filterMap: { [key: string]: any[] } = { status: ['ENABLED'], level: null };

  public dataSource = [
    {name: 'Sample name', identifier: '0001', description: 'Sample description', level: 'Hero'},
    {name: 'Sample name', identifier: '0002', description: 'Sample description', level: 'Hero'},
    {name: 'Sample name', identifier: '0003', description: 'Sample description', level: 'Hero'},
    {name: 'Sample name', identifier: '0004', description: 'Sample description', level: 'Hero'},
    {name: 'Sample name', identifier: '0005', description: 'Sample description', level: 'Hero'},
    {name: 'Sample name', identifier: '0006', description: 'Sample description', level: 'Hero'},
  ] as Group[];

  constructor() { }

  ngOnInit() { }

  public onFilterChange(key: string, values: any[]): void {
    this.filterMap[key] = values;
  }

  public handleClick(event: any): void {
    console.log('[onClick] : ' + event);
  }

}
