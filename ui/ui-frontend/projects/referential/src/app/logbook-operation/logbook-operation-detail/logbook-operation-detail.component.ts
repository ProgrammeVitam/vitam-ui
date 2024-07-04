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
import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  AuthService,
  IEvent,
  ExternalParameters,
  ExternalParametersService,
  fadeInOutAnimation,
  LogbookOperationReportState,
  LogbookOperationTypeProc,
  LogbookService,
  VitamUISnackBarService,
  VitamuiSidenavHeaderComponent,
  EventTypeLabelComponent,
  HistoryModule,
  PipesModule,
} from 'vitamui-library';
import { IngestStatus } from '../../../../../ingest/src/app/models/logbook-event.interface';
import { LogbookDownloadService } from '../logbook-download.service';
import { TruncatePipe } from '../../../../../vitamui-library/src/app/modules/pipes/truncate.pipe';
import { EventTypeBadgeClassPipe } from '../../shared/pipes/event-type-badge-class.pipe';
import { LastEventPipe } from '../../shared/pipes/last-event.pipe';
import { TranslateModule } from '@ngx-translate/core';
import { MatLegacyTabsModule } from '@angular/material/legacy-tabs';
import { NgIf, SlicePipe } from '@angular/common';

const msgForDownload: { [key: string]: string } = {
  EXPORT_DIP: 'LOGBOOK_OPERATION_DETAIL.DOWNLOAD_DIP',
  ARCHIVE_TRANSFER: 'LOGBOOK_OPERATION_DETAIL.DOWNLOAD_DIP_TRANSFER',
};

const defaultDownloadButtonLabel = 'LOGBOOK_OPERATION_DETAIL.DOWNLOAD_REPORT';

@Component({
  selector: 'app-logbook-operation-detail',
  templateUrl: './logbook-operation-detail.component.html',
  styleUrls: ['./logbook-operation-detail.component.scss'],
  animations: [fadeInOutAnimation],
  standalone: true,
  imports: [
    VitamuiSidenavHeaderComponent,
    NgIf,
    MatLegacyTabsModule,
    EventTypeLabelComponent,
    HistoryModule,
    SlicePipe,
    PipesModule,
    TranslateModule,
    LastEventPipe,
    EventTypeBadgeClassPipe,
    TruncatePipe,
  ],
})
export class LogbookOperationDetailComponent implements OnInit, OnChanges, OnDestroy {
  @Input() eventId: string;
  @Input() tenantIdentifier: number;
  @Input() isPopup: boolean;

  @Output() closePanel = new EventEmitter();

  public event: IEvent;
  private accessContractId: string;
  private hasAccessContractId = false;
  private accessContractLogbookIdentifier: string;

  public reportFileName: string;
  public downloadButtonTitle: string;
  public showDownloadButton = false;
  public disableDownloadButton = true;

  private subscriptions = new Subscription();

  constructor(
    private logbookService: LogbookService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private logbookDownloadService: LogbookDownloadService,
    private externalParameterService: ExternalParametersService,
    private vitamUISnackBarService: VitamUISnackBarService,
  ) {}

  ngOnInit() {
    this.externalParameterService.getUserExternalParameters().subscribe((parameters) => this.setAccessContractId(parameters));
    this.subscriptions.add(
      this.logbookDownloadService.logbookOperationsReloaded.subscribe((logbookOperations) =>
        this.setLogbookOperationIfIfHasBeenReloaded(logbookOperations),
      ),
    );
    this.refreshLogbookOperation();
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  ngOnChanges() {
    this.refreshLogbookOperation();
  }

  private setLogbookOperationIfIfHasBeenReloaded(logbookOperations: IEvent[]) {
    const logbookOperationUpdated = logbookOperations.find((e) => e.id === this.eventId);
    if (logbookOperationUpdated) {
      this.event = logbookOperationUpdated;
      this.updateDownloadButton();
      this.updateReportFilename();
    }
  }

  private setAccessContractId(userExternalParameters: Map<string, string>) {
    const accessContratId: string = userExternalParameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
    if (accessContratId && accessContratId.length > 0) {
      this.accessContractId = accessContratId;
      this.hasAccessContractId = true;
    } else {
      this.vitamUISnackBarService.open({
        message: 'SNACKBAR.NO_ACCESS_CONTRACT_LINKED',
      });
    }
  }

  public emitClose() {
    this.closePanel.emit();
  }

  private doesNotHaveTenant(): boolean {
    return this.tenantIdentifier === null || this.tenantIdentifier === undefined || !this.eventId;
  }

  public downloadReports() {
    if (this.doesNotHaveTenant()) {
      return;
    }
    this.logbookDownloadService.launchDownloadReport(this.event, this.accessContractId);
  }

  private updateDownloadButton() {
    this.downloadButtonTitle = msgForDownload[this.event.typeProc] ?? defaultDownloadButtonLabel;
    const logbookOperationReportState = this.logbookDownloadService.logbookOperationReportState(this.event);
    this.showDownloadButton =
      logbookOperationReportState === LogbookOperationReportState.IN_PROGRESS ||
      logbookOperationReportState === LogbookOperationReportState.DOWNLOADABLE;
    this.disableDownloadButton = !(logbookOperationReportState === LogbookOperationReportState.DOWNLOADABLE && this.hasAccessContractId);
  }

  private updateReportFilename() {
    if (this.event.events.length > 0 && this.event.events[0].data != null) {
      const data = JSON.parse(this.event.events[0].data);
      if (data != null && data.FileName != null) {
        this.reportFileName = data.FileName;
      } else {
        this.reportFileName = null;
      }
    } else {
      this.reportFileName = null;
    }
  }

  private setAccessContractLogbookIdentifier() {
    if (this.accessContractLogbookIdentifier || this.doesNotHaveTenant()) {
      return;
    }
    const tenant = this.authService.getTenantByAppAndIdentifier(this.route.snapshot.data.appId, this.tenantIdentifier);
    if (!tenant) {
      return;
    }
    this.accessContractLogbookIdentifier = tenant.accessContractLogbookIdentifier || '';
  }

  private refreshLogbookOperation() {
    if (this.doesNotHaveTenant()) {
      return;
    }
    this.setAccessContractLogbookIdentifier();
    this.logbookService.getOperationById(this.eventId, this.tenantIdentifier, this.accessContractLogbookIdentifier).subscribe((event) => {
      this.logbookDownloadService.logbookOperationsReloaded.next([event]);
    });
  }

  public hasATRDownloadable(): boolean {
    if (!this.event) {
      return false;
    }
    return this.event.typeProc === LogbookOperationTypeProc.INGEST_TEST && this.ingestIsFinish();
  }

  private ingestIsFinish(): boolean {
    const eventStatus = this.eventStatus(this.event);
    return eventStatus !== IngestStatus.STARTED && eventStatus !== IngestStatus.IN_PROGRESS;
  }

  // refacto: mettre en commun avec logbook-event.interface.ts
  private eventStatus(ingest: IEvent): IngestStatus {
    const lastEvent: IEvent = this.eventLastEvent(ingest);
    if (ingest.type === lastEvent.type) {
      return lastEvent.outcome as IngestStatus;
    }
    return IngestStatus.IN_PROGRESS;
  }

  // refacto: mettre en commun avec logbook-event.interface.ts
  private eventLastEvent(ingest: IEvent): IEvent {
    if (!this.eventHasEvents(ingest)) {
      return ingest;
    }
    return ingest.events[ingest.events.length - 1];
  }

  // refacto: mettre en commun avec logbook-event.interface.ts
  private eventHasEvents(ingest: IEvent): boolean {
    return ingest.events !== undefined && ingest.events.length > 0;
  }

  public downloadATR() {
    this.logbookService.downloadATR(this.event.objectId);
  }
}
