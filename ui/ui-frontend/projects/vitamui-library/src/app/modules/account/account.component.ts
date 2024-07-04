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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { BaseUserInfoApiService } from '../api/base-user-info-api.service';
import { AppRootComponent } from '../app-root-component.class';
import { ApplicationId } from '../application-id.enum';
import { Account } from '../models/account/account.interface';
import { BreadCrumbData } from '../models/breadcrumb/breadcrumb.interface';
import { AccountService } from './account.service';
import { TranslateModule } from '@ngx-translate/core';
import { AccountInformationTabComponent } from './account-information-tab/account-information-tab.component';
import { MatLegacyTabsModule } from '@angular/material/legacy-tabs';
import { NgIf } from '@angular/common';
import { UserPhotoComponent } from '../components/header/user-photo/user-photo.component';
import { VitamuiTitleBreadcrumbComponent } from '../components/vitamui-content-breadcrumb/vitamui-title-breadcrumb/vitamui-title-breadcrumb.component';

@Component({
  selector: 'vitamui-common-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.scss'],
  standalone: true,
  imports: [
    VitamuiTitleBreadcrumbComponent,
    UserPhotoComponent,
    NgIf,
    MatLegacyTabsModule,
    AccountInformationTabComponent,
    TranslateModule,
  ],
})
export class AccountComponent extends AppRootComponent implements OnInit, OnDestroy {
  public displayAppTab = false;
  public displayEditionAndAdminContact = false;
  public account: Account;
  public dataBreadcrumb: BreadCrumbData[];

  private sub: Subscription;

  constructor(
    private accountService: AccountService,
    private userInfoApiService: BaseUserInfoApiService,
    public route: ActivatedRoute,
  ) {
    super(route);
  }

  ngOnInit() {
    this.sub = this.accountService.getMyAccount().subscribe((account) => {
      this.userInfoApiService.getMyUserInfo().subscribe((userInfo) => {
        const accountWithUserInfos = account;
        accountWithUserInfos.userInfo = userInfo;
        this.account = accountWithUserInfos;
      });
    });
    this.dataBreadcrumb = [{ identifier: ApplicationId.PORTAL_APP }, { identifier: ApplicationId.ACCOUNTS_APP }];
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }
}
