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
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { BreadCrumbData, Logger } from 'ui-frontend-common';
import { GetorixDeposit } from '../core/model/getorix-deposit.interface';
import { GetorixDepositService } from '../getorix-deposit.service';

@Component({
  selector: 'getorix-deposit-upload-object',
  templateUrl: './getorix-deposit-upload-object.component.html',
  styleUrls: ['./getorix-deposit-upload-object.component.scss'],
})
export class GetorixDepositUploadObjectComponent implements OnInit, OnDestroy {
  operationIdentifierSubscription: Subscription;
  operationId: string;
  dataBreadcrumb: BreadCrumbData[];
  getorixDepositDetails: GetorixDeposit;

  constructor(
    private route: ActivatedRoute,
    private getorixDepositService: GetorixDepositService,
    private router: Router,
    private translateService: TranslateService,
    private loggerService: Logger
  ) {}

  ngOnInit(): void {
    this.operationIdentifierSubscription = this.route.params.subscribe((params) => {
      this.operationId = params.operationIdentifier;
      this.dataBreadcrumb = [
        {
          redirectUrl: this.router.url.replace('/create', '').replace('upload-object', '').replace(this.operationId, ''),
          label: this.translateService.instant('GETORIX_DEPOSIT.BREAD_CRUMB.ARCHIVAL_SPACE'),
        },
        {
          label: this.translateService.instant('GETORIX_DEPOSIT.BREAD_CRUMB.NEW_PROJECT'),
          redirectUrl: this.router.url.replace('/upload-object/', '').replace(this.operationId, ''),
        },
        { label: this.translateService.instant('GETORIX_DEPOSIT.BREAD_CRUMB.UPLOAD_ARCHIVES') },
      ];
      this.getorixDepositService.getGetorixDepositById(params.operationIdentifier).subscribe(
        (data: GetorixDeposit) => {
          this.getorixDepositDetails = data;
        },
        (error) => {
          this.loggerService.error('error while searching for this operation', error);
          this.router.navigate([this.router.url.replace('/create', '').replace('upload-object', '').replace(this.operationId, '')]);
        }
      );
    });
  }

  goToUpdateOperation() {
    this.router.navigate([this.router.url.replace('/upload-object/', '').replace(this.operationId, '')], {
      queryParams: { operationId: this.operationId },
    });
  }
  showComments() {
    console.log('show comments');
  }

  ngOnDestroy() {
    this.operationIdentifierSubscription?.unsubscribe();
  }
}
