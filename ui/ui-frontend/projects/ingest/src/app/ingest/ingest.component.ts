import { Component, OnInit } from '@angular/core';
import { IngestService } from './ingest.service';
import { SidenavPage, GlobalEventService } from 'ui-frontend-common';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-ingest',
  templateUrl: './ingest.component.html',
  styleUrls: ['./ingest.component.scss']
})
export class IngestComponent extends SidenavPage<any> implements OnInit {

  constructor(private ingestService: IngestService, route: ActivatedRoute, globalEventService: GlobalEventService) {
    super(route, globalEventService);
  }
  results: string;

  getOperations() {
    this.ingestService.ingest().subscribe((results) => {
      console.log('YOOO = ' + results);
      this.results = results;
    });
  }

  ngOnInit() {
  }

  refresh() {
  }
}
