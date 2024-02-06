import { Injectable } from '@angular/core';
import { Logger } from '../../logger/logger';
import { DisplayObject, Layout } from '../models';
import { LayoutSize } from '../types';

@Injectable()
export class LayoutService {
  MAX_COMLUMS = 2;
  DEFAULT_COLUMNS = this.MAX_COMLUMS;
  DEFAULT_SIZE: LayoutSize = 'medium';

  constructor(private logger: Logger) {}

  private keepNodeWithDisplayRuleFilter(displayObject: DisplayObject): boolean {
    const hasDisplayRule = Boolean(displayObject.displayRule);

    if (!hasDisplayRule) {
      this.logger.warn(this, 'No display rule found for the node, the node will be skipped', { displayObject });
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
      .filter(this.keepNodeWithDisplayRuleFilter)
      .filter(this.keepDisplayedNodeFilter)
      .reduce((rows, child: DisplayObject) => {
        if (!this.getLastRow(rows)) {
          rows.push([]);
        }

        const lastRow = this.getLastRow(rows);
        const consumedColumns = lastRow.reduce((acc: number, cell: DisplayObject) => acc + cell.displayRule.ui.layout.columns, 0);
        const canInsertInLastRow = consumedColumns + this.getLayout(child).columns <= this.MAX_COMLUMS;

        canInsertInLastRow ? lastRow.push(child) : rows.push([child]);

        return rows;
      }, []);
  }
}
