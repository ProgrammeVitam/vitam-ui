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
import { AfterViewInit, Component, ViewChildren } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter, merge } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { MatExpansionPanel } from '@angular/material/expansion';

@Component({
  selector: 'design-system-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements AfterViewInit {
  title = 'Design system App';

  @ViewChildren(MatExpansionPanel) expansionPanels: MatExpansionPanel[];

  constructor(
    private router: Router,
    translateService: TranslateService,
  ) {
    // Create a function that lists headings but not the ones inside our vitamui lib components
    (document as any).findHeadings =
      (document as any).findHeadings ||
      (() =>
        Array.from(document.querySelectorAll('h2, h3, h4, h5, h6')).filter((element) => {
          while (element.parentElement) {
            element = element.parentElement;
            if (element.tagName.startsWith('VITAMUI-')) {
              return false;
            }
          }
          return true;
        }));

    merge(
      this.router.events.pipe(filter((event) => event instanceof NavigationEnd)),
      translateService.onDefaultLangChange,
      translateService.onLangChange,
    ).subscribe(() => setTimeout(() => this.refreshAnchorMenu()));
  }

  ngAfterViewInit() {
    setTimeout(() => this.refreshAnchorMenu(), 100);
  }

  isActive(url: string): boolean {
    return this.router.isActive(url, {
      paths: 'subset',
      fragment: 'ignored',
      queryParams: 'ignored',
      matrixParams: 'ignored',
    });
  }

  private refreshAnchorMenu() {
    document.querySelectorAll(`mat-expansion-panel:not(.active) .mat-expansion-panel-body`).forEach((el) => (el.innerHTML = ''));
    const panelBody = document.querySelector(`mat-expansion-panel.active .mat-expansion-panel-body`);
    if (panelBody) {
      const headings: HTMLElement[] = (document as any).findHeadings();

      const list = headings.map((heading, index) => {
        const currentDepth = this.computeDepth(heading);
        const previousDepth = index ? this.computeDepth(headings[index - 1]) : 0;

        return `
          ${currentDepth > previousDepth ? [...Array(currentDepth - previousDepth).keys()].map(() => `<ul>`).join('') : ''}
          ${currentDepth < previousDepth ? [...Array(previousDepth - currentDepth).keys()].map(() => `</ul>`).join('') : ''}
          <li>
            <a onClick="document.findHeadings()[${index}].scrollIntoView({
                        behavior: 'smooth',
                        block: 'start',
                        inline: 'nearest',
                      });"
                title="${heading.textContent}"
            >
              ${heading.textContent}
            </a>
          </li>`;
      });

      panelBody.innerHTML = `<ul>${list.join('')}</ul>`;
      this.expansionPanels.forEach((expansionPanel) => {
        expansionPanel.hideToggle = true;
        expansionPanel.open();
      });
    }
  }

  private computeDepth(heading: HTMLElement) {
    return Number.parseInt(heading.tagName.replace(/\D/, '')) - 2;
  }
}
