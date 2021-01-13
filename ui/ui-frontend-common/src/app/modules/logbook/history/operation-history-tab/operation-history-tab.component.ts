/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { filter, map, switchMap, takeUntil } from 'rxjs/operators';

import { AuthService } from '../../../auth.service';
import { Event } from '../../../models';
import { LogbookService } from '../../logbook.service';

const EVENT_LIMIT = 100;

@Component({
  selector: 'vitamui-common-operation-history-tab',
  templateUrl: './operation-history-tab.component.html',
  styleUrls: ['./operation-history-tab.component.scss']
})

export class OperationHistoryTabComponent implements OnChanges, OnDestroy {

  @Input() id: string;
  @Input() identifier: string;
  @Input() collectionName: string;
  @Input() filter: (event: any) => boolean;
  @Input() filteringByIdentifier = true;

  events: Event[] = [];
  loading = false;

  private isDestroyed$ = new Subject();

  constructor(private authService: AuthService, private logbookService: LogbookService, private route: ActivatedRoute) { }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.hasOwnProperty('identifier') || changes.hasOwnProperty('collectionName')) {
      if (this.id && this.collectionName ) {
        this.initEvent();
      }
    }
  }

  ngOnDestroy(): void {
    this.isDestroyed$.next();
  }

  initEvent() {
    this.loading = true;
    this.events = [];
    this.route.paramMap
      .pipe(
        map((paramMap) =>
          paramMap.get('tenantIdentifier') ? +paramMap.get('tenantIdentifier') : Number(this.authService.user.proofTenantIdentifier)
        ),
        switchMap((tenantIdentifier) => {
          return this.logbookService.listOperationByIdAndCollectionName(this.id, this.collectionName, tenantIdentifier);
        }),
        takeUntil(this.isDestroyed$)
      )
      .subscribe(
        (results) => {
          this.events = results.filter((event) => {
            return this.filter ? this.filter(event) && (!this.filteringByIdentifier || this.filterByIdentifier(event)) : true;
          })
          .slice(0, EVENT_LIMIT);
          this.loading = false;
        },
        () => (this.loading = false)
      );

  }

  filterByIdentifier(event: any): boolean {

    return event.objectId && event.objectId === this.identifier;
  }
}
