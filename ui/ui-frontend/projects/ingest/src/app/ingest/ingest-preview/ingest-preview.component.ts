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
import { Component, EventEmitter, inject, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { first } from 'rxjs/operators';
import { LogbookService, PipesModule, TooltipDirective, VitamuiMenuButtonComponent, VitamuiSidenavHeaderComponent } from 'vitamui-library';
import type { LogbookOperation } from '../../models/logbook-event.interface';
import { IngestStatus, ingestStatus, ingestStatusVisualColor } from '../../models/logbook-event.interface';
import { IngestService } from '../ingest.service';
import { MatMenuItem } from '@angular/material/menu';
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import { IngestInformationTabComponent } from './ingest-information-tab/ingest-information-tab.component';
import { IngestErrorsDetailsTabComponent } from './ingest-errors-details-tab/ingest-errors-details-tab.component';
import { TranslatePipe } from '@ngx-translate/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ingest-preview',
  templateUrl: './ingest-preview.component.html',
  styleUrls: ['./ingest-preview.component.scss'],
  imports: [
    VitamuiMenuButtonComponent,
    MatMenuItem,
    TooltipDirective,
    MatTabGroup,
    MatTab,
    IngestInformationTabComponent,
    IngestErrorsDetailsTabComponent,
    PipesModule,
    TranslatePipe,
    CommonModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule,
    VitamuiSidenavHeaderComponent,
  ],
})
export class IngestPreviewComponent implements OnInit, OnChanges {
  private logbookService = inject(LogbookService);
  private ingestService = inject(IngestService);

  IngestStatus = IngestStatus;

  ingest: LogbookOperation;

  @Input() ingestFromParent: LogbookOperation;
  @Output() previewClose = new EventEmitter();
  @Output() ingestHasChanged = new EventEmitter<LogbookOperation>();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['ingestFromParent']) {
      this.reloadLogbookOperation();
    }
  }

  ngOnInit() {
    this.ingestService.logbookOperationsReloaded.subscribe((logbookOperations) =>
      this.setLogbookOperationIfIfHasBeenReloaded(logbookOperations),
    );
  }

  setLogbookOperationIfIfHasBeenReloaded(logbookOperations: LogbookOperation[]) {
    const logbookOperationUpdated = logbookOperations.find((e) => e.id === this.ingestFromParent.id);
    if (logbookOperationUpdated) {
      this.reloadLogbookOperation();
    }
  }

  reloadLogbookOperation() {
    this.ingestService
      .getIngestOperation(this.ingestFromParent.id)
      .pipe(first())
      .subscribe((receivedLogbookOperation) => {
        if (this.ingestFromParent.id === receivedLogbookOperation.id) {
          this.updateIngest(receivedLogbookOperation);
        }
      });
  }

  private updateIngest(logbookOperation: LogbookOperation) {
    this.ingest = logbookOperation;
    this.ingestHasChanged.emit(this.ingest);
  }

  emitClose() {
    this.previewClose.emit();
  }

  getIngestStatus(ingest: any): IngestStatus {
    return ingestStatus(ingest);
  }

  getIngestStatusColor(): 'green' | 'grey' | 'orange' | 'red' | 'black' {
    const status: IngestStatus = ingestStatus(this.ingest);
    return ingestStatusVisualColor(status);
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
