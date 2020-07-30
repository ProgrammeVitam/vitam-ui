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
import { Component, OnInit } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { StartupService } from 'ui-frontend-common';
import { Application, ApplicationService, Category } from 'ui-frontend-common';

@Component({
  selector: 'app-portal',
  templateUrl: './portal.component.html',
  styleUrls: ['./portal.component.scss']
})
export class PortalComponent implements OnInit {

  public applications: Map<Category, Application[]>;

  public welcomeTitle: string;

  public welcomeMessage: string;

  public customerLogoUrl: string;

  constructor(
    private applicationService: ApplicationService,
    private startupService: StartupService,
    private domSanitizer: DomSanitizer,
    private router: Router) { }

  ngOnInit() {
    this.applications = this.applicationService.getAppsGroupByCategories();
    this.welcomeTitle = this.startupService.getWelcomeTitle();
    this.welcomeMessage = this.startupService.getWelcomeMessage();

    if (this.startupService.getCustomerLogoURL()) {
      this.customerLogoUrl = this.domSanitizer.bypassSecurityTrustUrl(
        'data:image/*;base64,' +
        this.startupService.getCustomerLogoURL()
      ) as string;
    } else {
      this.customerLogoUrl = this.domSanitizer.bypassSecurityTrustUrl(
        'data:image/*;base64,' +
        this.startupService.getAppLogoURL()
      ) as string;
    }
  }

  public openApplication(app: Application): void {
    this.applicationService.openApplication(app, this.router, this.startupService.getConfigStringValue('UI_URL'));
  }
}
