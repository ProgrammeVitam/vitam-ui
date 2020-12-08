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
import { AfterViewInit, Directive, OnDestroy, ViewChild } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { ActivatedRoute } from '@angular/router';
import { merge, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { AppRootComponent } from './app-root-component.class';
import { GlobalEventService } from './global-event.service';

@Directive()
// tslint:disable-next-line:directive-class-suffix
export class SidenavPage<T> extends AppRootComponent implements AfterViewInit, OnDestroy {

  openedItem: T;

  @ViewChild('panel') panel: MatSidenav;

  private destroy = new Subject<void>();

  constructor(route: ActivatedRoute, public globalEventService: GlobalEventService) {
    super(route);
    merge(
      this.globalEventService.pageEvent,
      this.globalEventService.customerEvent,
      this.globalEventService.tenantEvent
    ).pipe(takeUntil(this.destroy)).subscribe(() => {
      this.closePanel();
    });
  }

  ngAfterViewInit() {
    if (!this.panel) {
      this.logger.error(this, 'Missing <mat-sidenav> element in component\'s template. Please a <mat-sidenav> with a #panel attribute');
    }
  }

  ngOnDestroy() {
    this.destroy.next();
  }

  openPanel(item: T) {
    this.openedItem = item;
    if (this.panel && !this.panel.opened) {
      this.panel.open();
    }
  }

  closePanel() {
    if (this.panel && this.panel.opened) {
      this.panel.close();
    }
  }

}
