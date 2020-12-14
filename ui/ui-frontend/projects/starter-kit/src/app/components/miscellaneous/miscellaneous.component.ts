import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { SampleDialogComponent } from './sample-dialog/sample-dialog.component';

@Component({
  selector: 'app-miscellaneous',
  templateUrl: './miscellaneous.component.html',
  styleUrls: ['./miscellaneous.component.scss']
})
export class MiscellaneousComponent implements OnInit {

  constructor(private dialog: MatDialog) { }

  ngOnInit() {}

  openDialog() {
    this.dialog.open(SampleDialogComponent, { panelClass: 'vitamui-modal', disableClose: true }).afterClosed().subscribe(() => {
      console.log('Dialog closed !');
    });
  }

}
