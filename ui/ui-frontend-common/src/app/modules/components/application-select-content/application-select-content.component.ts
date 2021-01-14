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
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { AuthService } from '../../auth.service';
import { Application } from '../../models/application/application.interface';
import { Category } from '../../models/application/category.interface';

@Component({
  selector: 'vitamui-common-application-select-content',
  templateUrl: './application-select-content.component.html',
  styleUrls: ['./application-select-content.component.scss']
})
export class ApplicationSelectContentComponent {

  @Input() isModalMenu: boolean;

  @Input()
  set applications(applications: Application[]) {
    this._applications = applications;
    this.checkTenantNumberByApp(this.applications);
    if (this._categories) {
      this.computeAppCategories();
    }
  }
  get applications(): Application[] { return this._applications; }
  // tslint:disable-next-line:variable-name
  private _applications: Application[];

  @Input()
  set categories(categories: { [categoryId: string]: Category }) {
    this._categories = categories;
    if (this._applications) {
      this.computeAppCategories();
    }
  }
  get categories(): { [categoryId: string]: Category } { return this._categories; }
  // tslint:disable-next-line:variable-name
  private _categories: { [categoryId: string]: Category };

  @Output() applicationSelected = new EventEmitter<string>();

  get target(): string { return '_blank'; }

  categoryList: any[];

  constructor(private authService: AuthService) { }

  checkTenantNumberByApp(apps: Application[]) {
    apps.forEach((application) => {
      const app = this.authService.user.tenantsByApp.find((appToTest) => appToTest.name === application.id);
      if (app) {
          application.hasTenantList = app.tenants && app.tenants.length > 1 && application.hasTenantList;
      }
    });
  }

  computeAppCategories() {
    const sortedApps = this._applications.sort((app1, app2) => app1.position - app2.position);
    this.categoryList = [];
    const ids = [];

    const defaultCategory = {
      id: 'default',
      title: 'Autres',
      displayTitle: true,
      order: 99,
      applications: []
    };
    this.categoryList.push(defaultCategory);

    for (const id in this._categories) {
      if (!this._categories.hasOwnProperty(id) || id === 'default') {
        continue;
      }

      const category: any = this._categories[id];
      category.id = id;
      category.applications = [];
      this.categoryList.push(category);
      ids.push(id);
    }

    this.categoryList.forEach(category => {
      if (category.id === 'default') {
        category.applications = sortedApps.filter((app) => !ids.includes(app.category));
      } else {
        category.applications = sortedApps.filter((app) => app.category === category.id);
      }
    });

    this.categoryList = this.categoryList.sort((c1, c2) => c1.order - c2.order);
  }

  selectApp(value: string) {
    this.applicationSelected.emit(value);
  }
}
