import { Component } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { collapseAnimation, Direction, Group, rotateAnimation } from 'vitamui-library';
import { SampleDialogComponent } from '../miscellaneous/sample-dialog/sample-dialog.component';

@Component({
  selector: 'design-system-arrays',
  templateUrl: './arrays.component.html',
  styleUrls: ['./arrays.component.scss'],
  animations: [collapseAnimation, rotateAnimation],
})
export class ArraysComponent {
  tableDataSource = [
    { zipName: 'Cabinet Douillet_Martin', size: '30 Go', compression: 100, loading: 20 },
    { zipName: 'Cabinet Douillet_Martin 2', size: '12 Go', compression: 80, loading: 0 },
  ];
  displayedColumns: string[] = ['zipName', 'size', 'compression', 'loading'];

  public orderBy = 'name';
  public direction = Direction.ASCENDANT;
  public levelFilterOptions: Array<{ value: string; label: string }> = [];
  public filterMap: { [key: string]: any[] } = { status: ['ENABLED'], level: null };

  public dataSource = [
    {
      name: 'Sample name',
      identifier: '0001',
      description: 'Sample description with a very long text that will trigger ellipsis with tooltip',
      level: 'Hero',
    },
    { name: 'Sample name', identifier: '0002', description: 'Sample description', level: 'Hero' },
    { name: 'Sample name', identifier: '0003', description: 'Sample description', level: 'Hero' },
    { name: 'Sample name', identifier: '0004', description: 'Sample description', level: 'Hero' },
    { name: 'Sample name', identifier: '0005', description: 'Sample description', level: 'Hero' },
    { name: 'Sample name', identifier: '0006', description: 'Sample description', level: 'Hero' },
  ] as Group[];

  constructor(private dialog: MatDialog) {}

  public onFilterChange(key: string, values: any[]): void {
    this.filterMap[key] = values;
  }

  public handleClick(event: any): void {
    console.log('[onClick] : ' + event);
  }

  openDialog(event: any) {
    console.log('[Dialog] : ' + event);
    this.dialog
      .open(SampleDialogComponent, { panelClass: 'vitamui-modal', disableClose: true })
      .afterClosed()
      .subscribe(() => {
        console.log('Dialog closed !');
      });
  }
}
