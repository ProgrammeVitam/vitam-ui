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
  public windowScrolled: boolean;
  private contentRendered: boolean;

  private routerSubscription: Subscription;

  constructor(private router: Router) {
  }

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

        const scrollElement =
          sideNavElement?.length > 0
            ? sideNavElement[0] : windowElement[0];

        if (scrollElement) {
          this.contentRendered = true;
          scrollElement.addEventListener('scroll', () => {
            if (
              scrollElement.scrollTop && scrollElement.scrollTop > 250
            ) {
              this.windowScrolled = true;
            } else if (
              (this.windowScrolled && window.pageYOffset) ||
              scrollElement.scrollTop ||
              scrollElement.scrollTop < 10
            ) {
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
          sideNavElement?.length > 0
            ? sideNavElement[sideNavElement.length - 1]
            : windowElement?.length > 0
              ? windowElement[0]
              : null;
      const currentScroll = scrollElement.scrollTop;
      if (currentScroll > 0) {
        window.requestAnimationFrame(smoothScroll);
        scrollElement.scrollTo(0, currentScroll - currentScroll / 8);
      }
    })();
  }

}
