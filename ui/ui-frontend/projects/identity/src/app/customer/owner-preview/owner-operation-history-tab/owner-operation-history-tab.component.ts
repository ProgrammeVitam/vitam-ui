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
import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import { AuthService, Event, LogbookService } from 'ui-frontend-common';

const EVENT_LIMIT = 100;
@Component({
  selector: 'app-owner-operation-history-tab',
  templateUrl: './owner-operation-history-tab.component.html',
  styleUrls: ['./owner-operation-history-tab.component.scss']
})
export class OwnerOperationHistoryTabComponent implements OnChanges {

  @Input() id: string;
  @Input() externalParamId: string;
  @Input() filter: (event: any) => boolean;

  events: Event[] = [];
  loading = false;

  constructor(private authService: AuthService, private logbookService: LogbookService) { }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.hasOwnProperty('id') || changes.hasOwnProperty('externalParamId')) {
      if (this.id && this.externalParamId ) {
        this.initEvent();
      }
    }
  }

  initEvent() {
    this.loading = true;
    this.events = [];

    const tenantIdentifier = Number(this.authService.user.proofTenantIdentifier);

    this.logbookService.listHistoryForOwner(this.id, this.externalParamId, tenantIdentifier).subscribe(
      (results) => {
        this.loading = false;
        this.events = results.filter((event) => this.filter ? this.filter(event) : true).slice(0, EVENT_LIMIT);
      },
      () => this.loading = false
    );
  }

}
