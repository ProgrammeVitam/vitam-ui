import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { EventDisplayHelperService } from '../event-display-helper.service';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { Event } from '../event';
import { LogbookOperation } from '../../../models/logbook-event.interface';

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
}
