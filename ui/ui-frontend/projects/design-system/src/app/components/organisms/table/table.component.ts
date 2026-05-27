/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { Component, inject } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { collapseAnimation, rotateAnimation } from '../../../../../../vitamui-library/src/app/modules/animations/vitamui-common-animations';
import { Direction } from '../../../../../../vitamui-library/src/app/modules/vitamui-table/direction.enum';
import { Group } from '../../../../../../vitamui-library/src/app/modules/models/group/group.interface';
import { VitamUICommonModule } from '../../../../../../vitamui-library/src/app/modules/vitamui-common.module';
import { VitamUILibraryModule } from '../../../../../../vitamui-library/src/lib/vitamui-library.module';
import { SampleDialogComponent } from '../dialog/sample-dialog/sample-dialog.component';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { TranslatePipe } from '@ngx-translate/core';
import { MatTableModule } from '@angular/material/table';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss'],
  animations: [collapseAnimation, rotateAnimation],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    VitamUICommonModule,
    VitamUILibraryModule,
    MatButtonToggleModule,
    TranslatePipe,
    MatDialogModule,
    MatTableModule,
    MatMenuModule,
  ],
})
export class TableComponent {
  private dialog = inject(MatDialog);

  tableDataSource = [
    { zipName: 'Cabinet Douillet_Martin', size: '30 Go', compression: 100, loading: 20 },
    { zipName: 'Cabinet Douillet_Martin 2', size: '12 Go', compression: 80, loading: 0 },
  ];
  displayedColumns: string[] = ['zipName', 'size', 'compression', 'loading'];
  selectedRow = this.tableDataSource[0];

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

  public onFilterChange(key: string, values: any[]): void {
    this.filterMap[key] = values;
  }

  public handleClick(event: any): void {
    console.log('[onClick] : ' + event);
  }

  openDialog(event: any) {
    console.log('[Dialog] : ' + event);
    this.dialog
      .open(SampleDialogComponent, { disableClose: true })
      .afterClosed()
      .subscribe(() => {
        console.log('Dialog closed !');
      });
  }
}
