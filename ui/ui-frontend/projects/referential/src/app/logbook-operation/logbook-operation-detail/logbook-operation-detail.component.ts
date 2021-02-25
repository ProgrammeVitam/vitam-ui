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
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService, Event, fadeInOutAnimation, LogbookService } from 'ui-frontend-common';
import {LogbookDownloadService} from '../logbook-download.service';


@Component({
  selector: 'app-logbook-operation-detail',
  templateUrl: './logbook-operation-detail.component.html',
  styleUrls: ['./logbook-operation-detail.component.scss'],
  animations: [
    fadeInOutAnimation
  ]
})
export class LogbookOperationDetailComponent implements OnInit, OnChanges {

  @Input() eventId: string;

  @Input() tenantIdentifier: number;
  @Input() isPopup: boolean;

  @Output() closePanel = new EventEmitter();

  event: Event;
  loading: boolean;
  reportFileName: string;
  hideDownload: boolean;
  canDownload: boolean;
  couldDownload: boolean;

  // tslint:disable-next-line: max-line-length
  constructor(private logbookService: LogbookService, private authService: AuthService, private route: ActivatedRoute, private logbookDownloadService: LogbookDownloadService) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.eventId || changes.tenantIdentifier) {
      this.refreshEvents();
    }
  }

  openPopup() {
    window.open('/logbook-operation/tenant/' + this.tenantIdentifier + '/event/' + this.event.id,
      'detailPopup', 'width=584, height=713, resizable=no, location=no');
    this.emitClose();
  }

  closePopup() {
    window.close();
  }

  emitClose() {
    this.closePanel.emit();
  }


  downloadReports() {
    if (!this.tenantIdentifier || !this.eventId) {
      return;
    }

    const tenant = this.authService.getTenantByAppAndIdentifier(this.route.snapshot.data.appId, this.tenantIdentifier);

    if (!tenant) {
      return;
    }
    const accessContractLogbookIdentifier = tenant.accessContractLogbookIdentifier || '';
    this.logbookDownloadService.downloadReport(this.event, this.tenantIdentifier, accessContractLogbookIdentifier);
  }

  updateCanDownload(event: Event) {
    const canDownloadReports = this.logbookDownloadService.canDownloadReports(event);
    this.hideDownload = canDownloadReports.length < 1;
    if (!this.hideDownload) {
      this.canDownload = canDownloadReports.includes('download');
      this.couldDownload = canDownloadReports.includes('in-progress');
    } else {
      this.canDownload = false;
      this.couldDownload = false;
    }
  }

  private refreshEvents() {
    if (!this.tenantIdentifier || !this.eventId) {
      return;
    }

    const tenant = this.authService.getTenantByAppAndIdentifier(this.route.snapshot.data.appId, this.tenantIdentifier);

    if (!tenant) {
      return;
    }

    const accessContractLogbookIdentifier = tenant.accessContractLogbookIdentifier || '';

    this.loading = true;
    this.logbookService.getOperationById(this.eventId, this.tenantIdentifier, accessContractLogbookIdentifier)
      .subscribe((event) => {
        this.event = event;
        this.loading = false;
        this.updateCanDownload(event);
        if (event.events.length > 0 && event.events[0].data != null) {
          const data = JSON.parse(this.event.events[0].data);
          if (data != null && data.FileName != null) {
            this.reportFileName = data.FileName;
          } else {
            this.reportFileName = null;
          }
        } else {
          this.reportFileName = null;
        }
      });
  }

}
