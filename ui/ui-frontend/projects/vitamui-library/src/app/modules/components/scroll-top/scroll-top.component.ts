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
import { AfterViewChecked, Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'vitamui-common-scroll-top',
  templateUrl: './scroll-top.component.html',
  styleUrls: ['./scroll-top.component.scss'],
  standalone: false,
})
export class ScrollTopComponent implements OnInit, AfterViewChecked, OnDestroy {
  public windowScrolled: boolean;
  private contentRendered: boolean;

  private routerSubscription: Subscription;

  constructor(private router: Router) {}

  ngOnInit() {
    this.routerSubscription = this.router.events.subscribe((evt) => {
      if (!(evt instanceof NavigationEnd)) {
        return;
      }
      this.windowScrolled = false;
      this.contentRendered = false;
    });
  }

  ngAfterViewChecked() {
    if (!this.contentRendered) {
      const bodyElement = document.getElementsByClassName('vitamui-content');
      if (bodyElement?.length > 0) {
        const sideNavElement = document.getElementsByClassName('mat-sidenav-content');
        const windowElement = document.getElementsByTagName('div');

        const scrollElement = sideNavElement?.length > 0 ? sideNavElement[0] : windowElement[0];

        if (scrollElement) {
          this.contentRendered = true;
          scrollElement.addEventListener('scroll', () => {
            if (scrollElement.scrollTop && scrollElement.scrollTop > 250) {
              this.windowScrolled = true;
            } else if ((this.windowScrolled && window.pageYOffset) || scrollElement.scrollTop || scrollElement.scrollTop < 10) {
              this.windowScrolled = false;
            }
          });
        }
      }
    }
  }

  ngOnDestroy() {
    this.routerSubscription.unsubscribe();
  }

  public scrollToTop() {
    (function smoothScroll() {
      const sideNavElement = document.getElementsByClassName('mat-sidenav-content');
      const windowElement = document.getElementsByTagName('div');
      const scrollElement =
        sideNavElement?.length > 0 ? sideNavElement[sideNavElement.length - 1] : windowElement?.length > 0 ? windowElement[0] : null;
      const currentScroll = scrollElement.scrollTop;
      if (currentScroll > 0) {
        window.requestAnimationFrame(smoothScroll);
        scrollElement.scrollTo(0, currentScroll - currentScroll / 8);
      }
    })();
  }
}
