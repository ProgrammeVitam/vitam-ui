/* tslint:disable:component-selector */
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
import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Application, AuthService} from 'ui-frontend-common';

@Component({
  selector: 'vitamui-common-application-select-content-updated',
  templateUrl: './application-select-content.component.html',
  styleUrls: ['./application-select-content.component.scss']
})
export class UpdatedApplicationSelectContentComponent {

  @Input() isModalMenu: boolean;

  @Input()
  set applications(applications: Application[]) {
    this._applications = applications;
    this.checkTenantNumberByApp(this.applications);
    const sortedApps = this._applications.sort((app1, app2) => app1.position - app2.position);
    this.userApplications = sortedApps.filter((app) => app.category === 'users');
    this.adminApplications = sortedApps.filter((app) => app.category === 'administrators');
    this.settingsApplications = sortedApps.filter((app) => app.category === 'settings');
    this.referentialApplications = sortedApps.filter((app) => app.category === 'referential');
    this.opauditApplications = sortedApps.filter((app) => app.category === 'opaudit');
    this.techAdminApplications = sortedApps.filter((app) => app.category === 'techadmin');
  }

  get applications(): Application[] {
    return this._applications;
  }

  // tslint:disable-next-line:variable-name
  private _applications: Application[];

  @Output() applicationSelected = new EventEmitter<string>();

  get target(): string {
    return '_blank';
  }

  userApplications: Application[];
  settingsApplications: Application[];
  adminApplications: Application[];
  referentialApplications: Application[];
  opauditApplications: Application[];
  techAdminApplications: Application[];

  constructor(private authService: AuthService) {
  }

  checkTenantNumberByApp(apps: Application[]) {
    apps.forEach((application) => {
      const app = this.authService.user.tenantsByApp.find((appToTest) => appToTest.name === application.id);
      if (app) {
        if (app.tenants && app.tenants.length > 1 && application.hasTenantList) {
          application.hasTenantList = true;
        } else {
          application.hasTenantList = false;
        }
      }
    });
  }

  selectApp(value: string) {
    this.applicationSelected.emit(value);
  }
}
