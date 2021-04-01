import { Component, OnInit, Input, SimpleChanges, OnChanges } from '@angular/core';
import { IngestService } from '../../ingest.service';
import { EventDisplayHelperService } from '../event-display-helper.service';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { Event } from '../event';


@Component({
  selector: 'app-ingest-errors-details-tab',
  templateUrl: './ingest-errors-details-tab.component.html',
  styleUrls: ['./ingest-errors-details-tab.component.css']
})
export class IngestErrorsDetailsTabComponent implements OnInit, OnChanges {

  @Input()
  ingest: any;

  ingestErrorsTreeControl: NestedTreeControl<Event>;
  ingestErrorsTreeDataSource: MatTreeNestedDataSource<Event>;


  constructor(private ingestService: IngestService, private eventDisplayHelper: EventDisplayHelperService) {

    this.ingestErrorsTreeControl = new NestedTreeControl<Event>(node => node.subEvents);
    this.ingestErrorsTreeDataSource = new MatTreeNestedDataSource<Event>();
 
  }

  hasChild = (_: number, node: Event) => !!node.subEvents && node.subEvents.length > 0;

  getAllEvents(ingest: any) {
    if(ingest) {
      this.ingestService.getIngestOperation(ingest.id).subscribe(data => {
      this.ingestErrorsTreeDataSource.data = this.eventDisplayHelper.getAllEvents(data.events);
      });
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

  getEventStatus(event : Event) {
      return event.eventData.outcome;
  }
  isStepOK(event : Event) {
    if(event.eventData.outcome === "OK") {
      return true;
    }
    else {
      return false;
    }
  }


}
