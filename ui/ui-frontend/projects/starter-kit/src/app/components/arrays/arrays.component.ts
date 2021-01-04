
import { Component, OnInit } from '@angular/core';
import { collapseAnimation, Direction, Group, rotateAnimation } from 'ui-frontend-common';

@Component({
  selector: 'app-arrays',
  templateUrl: './arrays.component.html',
  styleUrls: ['./arrays.component.scss'],
  animations: [
    collapseAnimation,
    rotateAnimation,
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
