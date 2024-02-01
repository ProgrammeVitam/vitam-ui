import { DOCUMENT } from '@angular/common';
import { AfterViewChecked, Component, OnInit } from '@angular/core';

@Component({
  selector: 'vitamui-common-scroll-top',
  templateUrl: './scroll-top.component.html',
  styleUrls: ['./scroll-top.component.scss'],
})
export class ScrollTopComponent implements OnInit, AfterViewChecked {
  windowScrolled: boolean;
  contentRendered: boolean;

  constructor() {}

  ngOnInit() {}

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

  scrollToTop() {
    (function smoothScroll() {
      const sideNavElement = document.getElementsByClassName('mat-sidenav-content');
      const windowElement = document.getElementsByTagName('div');
      const scrollElement = sideNavElement?.length > 0 ? sideNavElement[0] : windowElement[0];
      const currentScroll = scrollElement.scrollTop;
      if (currentScroll > 0) {
        window.requestAnimationFrame(smoothScroll);
        scrollElement.scrollTo(0, currentScroll - currentScroll / 8);
      }
    })();
  }
}
