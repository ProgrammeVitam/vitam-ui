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
import { SafeResourceUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ApplicationId, AuthService, GlobalEventService, StartupService, ThemeDataType, ThemeService } from 'ui-frontend-common';
import { Application, ApplicationService, Category } from 'ui-frontend-common';

@Component({
  selector: 'app-portal',
  templateUrl: './portal.component.html',
  styleUrls: ['./portal.component.scss']
})
export class PortalComponent implements OnInit, OnDestroy {

  public applications: Map<Category, Application[]>;
  public welcomeTitle: string;
  public welcomeMessage: string;
  public customerLogoUrl: SafeResourceUrl;
  public loading = true;

  private sub: Subscription;

  constructor(
    private applicationService: ApplicationService,
    private startupService: StartupService,
    private authService: AuthService,
    private themeService: ThemeService,
    private router: Router,
    private globalEventService: GlobalEventService) { }

  ngOnInit() {
    this.sub = this.applicationService.getActiveTenantAppsMap().subscribe((appMap) => {
        this.applications = appMap;
        this.loading = false;
    });
    this.welcomeTitle = this.themeService.getData(this.authService.user, ThemeDataType.PORTAL_TITLE) as string;
    this.welcomeMessage = this.themeService.getData(this.authService.user, ThemeDataType.PORTAL_MESSAGE) as string;
    this.customerLogoUrl = this.themeService.getData(this.authService.user, ThemeDataType.PORTAL_LOGO);
    this.globalEventService.pageEvent.next(ApplicationId.PORTAL_APP);
  }

  ngOnDestroy() {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }

  public openApplication(app: Application): void {
    this.applicationService.openApplication(app, this.router, this.startupService.getConfigStringValue('UI_URL'));
  }
}
