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
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { LogbookService } from 'ui-frontend-common';
import { IngestService } from '../ingest.service';

@Component({
  selector: 'app-ingest-preview',
  templateUrl: './ingest-preview.component.html',
  styleUrls: ['./ingest-preview.component.scss']
})
export class IngestPreviewComponent implements OnInit {

  @Input() ingest: any; // Make a type ?
  @Output() previewClose = new EventEmitter();
 

  constructor(private logbookService: LogbookService, private ingestService : IngestService) { }

  ngOnInit() {

  }

  emitClose() {
    this.previewClose.emit();
  }

  filterEvents(event: any): boolean {
    return event.outDetail && (
      event.outDetail.includes('EXT_VITAMUI_UPDATE_INGEST') ||
      event.outDetail.includes('EXT_VITAMUI_CREATE_INGEST')
    );
  }

  getOperationStatus(ingest: any): string {
    const eventsLength = ingest.events.length;
    if (eventsLength > 0) {
      if (ingest.evType === ingest.events[eventsLength - 1].evType) {
        return ingest.events[eventsLength - 1].outcome;
      } else {
        return 'En cours';
      }
    }
  }

  ingestStatus(ingest: any): string {
    if (this.getOperationStatus(ingest) === 'En cours') {
      return 'En cours';
    } else {
      return (ingest.events !== undefined && ingest.events.length !== 0) ?
        ingest.events[ingest.events.length - 1].outcome : ingest.outcome;
    }
  }

  downloadManifest() {
    this.logbookService.downloadManifest(this.ingest.id);
  }

  downloadATR() {
    this.logbookService.downloadATR(this.ingest.id);
  }

  generateODTreport() {
    this.ingestService.downloadODTReport(this.ingest.id);
  }
}
