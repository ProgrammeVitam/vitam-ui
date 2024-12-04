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
import { Injectable } from '@angular/core';
import { Logger } from '../../logger/logger';
import { DisplayObject, Layout } from '../models';
import { LayoutSize } from '../types';

@Injectable({
  providedIn: 'root',
})
export class LayoutService {
  MAX_COLUMNS = 2;
  DEFAULT_COLUMNS = this.MAX_COLUMNS;
  DEFAULT_SIZE: LayoutSize = 'medium';
  COMPLETE_ROWS = true;

  constructor(private logger: Logger) {}

  private keepNodeWithDisplayRuleFilter(displayObject: DisplayObject): boolean {
    const hasDisplayRule = Boolean(displayObject.displayRule);

    if (!hasDisplayRule) {
      this.logger.warn(this, `No display rule found for the node ${displayObject.path}, the node will be skipped`, { displayObject });
    }

    return hasDisplayRule;
  }

  private keepDisplayedNodeFilter(displayObject: DisplayObject): boolean {
    return Boolean(displayObject?.displayRule?.ui?.display);
  }

  private getLastRowIndex = (rows: any[]) => rows.length - 1;

  private getLastRow = (rows: any[]) => rows[this.getLastRowIndex(rows)];

  private getLayout(displayObject: DisplayObject): Layout {
    const { columns, size } = displayObject.displayRule?.ui?.layout;

    if (columns === undefined) {
      this.logger.info(this, `No columns found in display rule, default one (${this.DEFAULT_COLUMNS}) will be applied`);
    }
    if (size === undefined) {
      this.logger.info(this, `No size found in display rule, default one (${this.DEFAULT_SIZE}) will be applied`);
    }

    return {
      columns: columns || this.DEFAULT_COLUMNS,
      size: size || this.DEFAULT_SIZE,
    };
  }

  public compute(displayObject: DisplayObject): DisplayObject[][] {
    return displayObject.children
      .filter((displayObject) => this.keepNodeWithDisplayRuleFilter(displayObject))
      .filter((displayObject) => this.keepDisplayedNodeFilter(displayObject))
      .reduce((rows, child: DisplayObject) => {
        if (!this.getLastRow(rows)) {
          rows.push([]);
        }

        const lastRow = this.getLastRow(rows);
        const consumedColumns = lastRow.reduce((acc: number, cell: DisplayObject) => acc + cell.displayRule.ui.layout.columns, 0);
        const canInsertInLastRow = consumedColumns + this.getLayout(child).columns <= this.MAX_COLUMNS;

        canInsertInLastRow ? lastRow.push(child) : rows.push([child]);

        return rows;
      }, [] as DisplayObject[][])
      .map((row) => {
        const consumedColumns = row.reduce((acc: number, cell: DisplayObject) => acc + cell.displayRule.ui.layout.columns, 0);
        const canInsertColumn = consumedColumns < this.MAX_COLUMNS;

        if (this.COMPLETE_ROWS && canInsertColumn) row.push(null);

        return row;
      });
  }
}
