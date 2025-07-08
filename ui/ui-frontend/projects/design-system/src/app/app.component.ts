/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { Component } from '@angular/core';
import { Route, Router, Routes } from '@angular/router';
import { VitamuiSelectOptions } from '../../../vitamui-library/src/lib/components/select/select.component';
import { FormBuilder, FormGroup } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { RouteData } from './app-routing.module';

@Component({
  selector: 'design-system-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: false,
})
export class AppComponent {
  title = 'Design system App';

  routes: Routes;
  searchOptions: VitamuiSelectOptions;
  form: FormGroup;
  url: string;

  constructor(
    private router: Router,
    fb: FormBuilder,
    translateService: TranslateService,
  ) {
    this.routes = router.config;

    function extractSearchData(context: string, route: Route, acc: VitamuiSelectOptions) {
      const pathWithContext = [context, route.path].join('/');
      if (route.children) {
        route.children.forEach((child) => extractSearchData(pathWithContext, child, acc));
      } else if (route.path && route.redirectTo === undefined) {
        const data: RouteData = route.data;
        const altSearch = data?.altSearch
          ? ([...(data.altSearch._ || []), ...(data.altSearch[translateService.currentLang] || [])] as string[])
          : [];
        const routeTitle = translateService.instant(`ROUTE${pathWithContext.replace(/\//g, '.')}.TITLE`);
        const options = [routeTitle, ...altSearch].map((s) => ({ key: pathWithContext, label: s }));
        acc.options.push(...options);
      }
      return acc;
    }

    // Make sure the translations are loaded, and reload on translation change
    translateService.onLangChange.subscribe(() => {
      this.searchOptions = this.routes.reduce(
        (acc, route) => {
          extractSearchData('', route, acc);
          return acc;
        },
        { options: [], customSorting: (a, b) => a.label.localeCompare(b.label) } as VitamuiSelectOptions,
      );
    });

    const searchControl = fb.control('');
    this.form = fb.group({
      search: searchControl,
    });
    searchControl.valueChanges.subscribe((value) => {
      if (value) {
        router.navigateByUrl(value);
        setTimeout(() => searchControl.reset());
      }
    });
  }

  isActive(url: string): boolean {
    return this.router.isActive(url, {
      paths: 'subset',
      queryParams: 'subset',
      fragment: 'ignored',
      matrixParams: 'ignored',
    });
  }
}
