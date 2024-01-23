/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { Logger, StartupService } from 'ui-frontend-common';
import { DepositStatus, GetorixDeposit } from './core/model/getorix-deposit.interface';
import { GetorixDepositService } from './getorix-deposit.service';

@Component({
  selector: 'getorix-deposit',
  templateUrl: './getorix-deposit.component.html',
  styleUrls: ['./getorix-deposit.component.scss'],
})
export class GetorixDepositComponent implements OnInit, OnDestroy {
  getorixDepositList: GetorixDeposit[] = [];
  pending = false;

  tenantIdentifier: string;
  subscriptions: Subscription = new Subscription();

  constructor(
    private router: Router,
    private getorixDepositService: GetorixDepositService,
    private logger: Logger,
    private startupService: StartupService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit() {
    this.subscriptions.add(
      this.route.params.subscribe((params) => {
        this.tenantIdentifier = params.tenantIdentifier;
      }),
    );

    this.getLastThreeOperations();
  }

  startDepositCreation() {
    this.router.navigate([this.router.url, 'create']);
  }

  showOperationList() {
    console.log('show operation List');
  }

  getLastThreeOperations() {
    this.getorixDepositList = [];
    this.pending = true;
    this.subscriptions.add(
      this.getorixDepositService.getLastThreeOperations().subscribe(
        (response: GetorixDeposit[]) => {
          this.getorixDepositList = response;
          this.pending = false;
        },
        (error) => {
          this.logger.error('error while searching the first deposits operations', error);
          this.pending = false;
        },
      ),
    );
  }

  OpenGetorixOperationDetails(getorixOperation: GetorixDeposit) {
    if (getorixOperation) {
      console.log('salam operation details', getorixOperation);
      if (getorixOperation.depositStatus === DepositStatus.DRAFT) {
        this.router.navigate([this.router.url, 'create'], {
          queryParams: { operationId: getorixOperation.id },
        });
      } else {
        window.location.href =
          this.startupService.getCollectUrl() +
          '/getorix-deposit/tenant/' +
          this.tenantIdentifier +
          '/create/upload-object/' +
          getorixOperation.id;
      }
    }
  }

  ngOnDestroy() {
    this.subscriptions?.unsubscribe();
  }
}
