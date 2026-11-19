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
import { Component, effect, inject, OnChanges, OnInit, SimpleChanges, input, signal } from '@angular/core';
import type { DisplayObject } from '../../models/display-object.model';
import { internationalizedKeys } from '../../services/display-object-helper.service';
import { FavoriteEntryService } from '../../services/favorite-entry.service';
import { LayoutService } from '../../services/layout.service';
import { DisplayObjectType } from '../../types';
import { DataComponent } from '../../../components/data/data.component';
import { NgTemplateOutlet } from '@angular/common';
import { AccordionComponent } from '../../../components/accordion/accordion.component';
import { ListComponent } from '../list/list.component';
import { PrimitiveComponent } from '../primitive/primitive.component';
import { EmptyPipe } from '../../../pipes/empty.pipe';
import { TranslatePipe } from '@ngx-translate/core';
import { ObjectViewerExpansionService } from '../../services/object-viewer-expansion.service';

@Component({
  selector: 'vitamui-common-group',
  templateUrl: './group.component.html',
  styleUrls: ['./group.component.scss'],
  imports: [DataComponent, NgTemplateOutlet, AccordionComponent, ListComponent, PrimitiveComponent, EmptyPipe, TranslatePipe],
})
export class GroupComponent implements OnInit, OnChanges {
  private readonly layoutService = inject(LayoutService);
  private readonly favoriteEntryService = inject(FavoriteEntryService);
  // Only provided by the object viewer, groups displayed by other components keep their own state.
  private readonly expansionService = inject(ObjectViewerExpansionService, { optional: true });

  readonly displayObject = input<DisplayObject>(undefined);
  readonly opened = signal(false);

  favoriteEntry: [key: string, value: any];
  favoritePath: string;
  rows: DisplayObject[][] = [[]];
  display: 'ACCORDION' | 'KEY-VALUE-OBJECTS' | 'ROWS';

  readonly DisplayObjectType = DisplayObjectType;
  readonly entries = Object.entries as (value: object) => [string, string | string[]][];

  constructor() {
    effect(() => {
      const expansion = this.expansionService?.expansion();
      if (expansion) this.opened.set(expansion.opened);
    });
  }

  ngOnInit(): void {
    const displayObject = this.displayObject();
    if (displayObject.key !== 'Generalities') {
      displayObject.open = false;
    }
    // Groups created after an "expand all" request, such as nested groups, follow it.
    this.opened.set(this.expansionService?.expansion()?.opened ?? displayObject.open);
  }

  ngOnChanges(changes: SimpleChanges): void {
    const { displayObject } = changes;

    if (displayObject) {
      const displayObjectValue = this.displayObject();
      if (!displayObjectValue.key) {
        this.display = 'ROWS';
      } else {
        this.display = internationalizedKeys.includes(displayObjectValue.key) ? 'KEY-VALUE-OBJECTS' : 'ACCORDION';
      }

      this.favoriteEntry = this.favoriteEntryService.favoriteEntry(this.displayObject());
      this.favoritePath = this.favoriteEntryService.favoritePath(this.displayObject());
      this.rows = this.layoutService.compute(this.displayObject());
    }
  }

  toggle(): void {
    this.displayObject().open = !this.displayObject().open;
  }
}
