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
import { NestedTreeControl } from '@angular/cdk/tree';
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { LogbookOperation } from '../../../models/logbook-event.interface';
import { Event } from '../event';
import { EventDisplayHelperService } from '../event-display-helper.service';

@Component({
  selector: 'app-ingest-errors-details-tab',
  templateUrl: './ingest-errors-details-tab.component.html',
  styleUrls: ['./ingest-errors-details-tab.component.css'],
})
export class IngestErrorsDetailsTabComponent implements OnInit, OnChanges {
  @Input() ingest: LogbookOperation;

  ingestErrorsTreeControl: NestedTreeControl<Event>;
  ingestErrorsTreeDataSource: MatTreeNestedDataSource<Event>;

  constructor(private eventDisplayHelper: EventDisplayHelperService) {
    this.ingestErrorsTreeControl = new NestedTreeControl<Event>((node) => node.subEvents);
    this.ingestErrorsTreeDataSource = new MatTreeNestedDataSource<Event>();
  }

  hasChild = (_: number, node: Event) => !!node.subEvents && node.subEvents.length > 0;

  getAllEvents(ingest: LogbookOperation) {
    if (ingest) {
      this.ingestErrorsTreeDataSource.data = this.eventDisplayHelper.getAllEvents(ingest.events);
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.ingest) {
      this.getAllEvents(changes.ingest.currentValue);
    }
  }

  ngOnInit(): void {
    this.getAllEvents(this.ingest);
  }

  getEventStatus(event: Event) {
    return event.eventData.outcome;
  }

  isStepOK(event: Event) {
    return event.eventData.outcome === 'OK';
  }

  getEvDetData(event: Event): string {
    return event.eventData?.evDetData && event.eventData?.evDetData !== '{}'
      ? JSON.stringify({ evDetData: JSON.parse(event.eventData.evDetData) })
      : '';
  }
}
