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
import { Component, Input, OnChanges } from '@angular/core';
import {
  AgIdExtDeflateJson,
  EvDetDataDeflateJson,
  ingestHasEvents,
  ingestLastEvent,
  ingestStatus,
  LogbookOperation,
} from '../../../models/logbook-event.interface';
import { TranslateModule } from '@ngx-translate/core';
import { PipesModule } from 'vitamui-library';
import { IngestEventDetailComponent } from './ingest-event-detail/ingest-event-detail.component';
import { NgIf, NgSwitch, NgSwitchCase } from '@angular/common';

@Component({
  selector: 'app-ingest-information-tab',
  templateUrl: './ingest-information-tab.component.html',
  styleUrls: ['./ingest-information-tab.component.scss'],
  standalone: true,
  imports: [NgIf, NgSwitch, NgSwitchCase, IngestEventDetailComponent, PipesModule, TranslateModule],
})
export class IngestInformationTabComponent implements OnChanges {
  @Input() ingest: LogbookOperation;
  evDetDataDeflated: EvDetDataDeflateJson;
  agIdExtDeflated: AgIdExtDeflateJson;

  constructor() {}

  ngOnChanges() {
    this.evDetDataDeflated = this.deflateJsonEvDetData(this.ingest);
    this.agIdExtDeflated = this.deflateJsonAgIdExt(this.ingest);
  }

  hasEvent(): boolean {
    return ingestHasEvents(this.ingest);
  }

  ingestMessage(ingest: LogbookOperation): string {
    return ingestHasEvents(ingest) ? ingestLastEvent(ingest).outMessg : ingest.outMessg;
  }

  ingestEndDate(ingest: LogbookOperation): string {
    return ingestHasEvents(ingest) ? ingestLastEvent(ingest).evDateTime : ingest.evDateTime;
  }

  getIngestStatus(ingest: LogbookOperation): string {
    return ingestStatus(ingest);
  }

  private deflateJsonEvDetData(element: LogbookOperation): EvDetDataDeflateJson {
    if (!element || !element.evDetData || typeof element.evDetData !== 'string' || element.evDetData.length <= 2) {
      return {};
    }
    return JSON.parse(element.evDetData);
  }

  private deflateJsonAgIdExt(element: LogbookOperation): AgIdExtDeflateJson {
    if (!element || !element.agIdExt || typeof element.agIdExt !== 'string' || element.agIdExt.length <= 2) {
      return {};
    }
    return JSON.parse(element.agIdExt);
  }

  getAgIdExt(element: LogbookOperation): any {
    if (!element) {
      return element;
    }
    if (element.agIdExt && typeof element.agIdExt === 'string' && element.agIdExt.length >= 2) {
      element.agIdExt = JSON.parse(element.agIdExt);
    }
    return element.agIdExt;
  }
}
