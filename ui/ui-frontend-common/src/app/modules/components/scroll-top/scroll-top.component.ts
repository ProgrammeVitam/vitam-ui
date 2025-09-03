import { DOCUMENT } from '@angular/common';
import { AfterViewChecked, Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'vitamui-common-scroll-top',
  templateUrl: './scroll-top.component.html',
  styleUrls: ['./scroll-top.component.scss'],
})
export class ScrollTopComponent implements OnInit, AfterViewChecked, OnDestroy {
  public windowScrolled = false;
  private contentRendered = false;
  private routerSubscription: Subscription;
  private scrollElement: Element;
  private scrollListener: () => void;

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
          this.scrollElement = scrollElement;

          // Définir le listener une fois
          this.scrollListener = () => {
            if (this.scrollElement.scrollTop > 250) {
              this.windowScrolled = true;
            } else if ((this.windowScrolled && window.pageYOffset) || this.scrollElement.scrollTop < 10) {
              this.windowScrolled = false;
            }
          };

          // Ajouter le listener
          this.scrollElement.addEventListener('scroll', this.scrollListener);
        }
      }
    }
  }

  ngOnDestroy() {
    if (this.scrollElement && this.scrollListener) {
      this.scrollElement.removeEventListener('scroll', this.scrollListener);
    }
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  public scrollToTop() {
    const element = document.getElementsByClassName('mat-sidenav-content')[0] || document.getElementsByTagName('div')[0];

    if (!element) return;

    element.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }
}
