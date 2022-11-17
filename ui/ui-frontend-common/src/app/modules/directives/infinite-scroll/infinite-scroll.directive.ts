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
import {
  Directive,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';

const DEFAULT_SCROLL_THRESHOLD = 0.8;

@Directive({
  selector: '[vitamuiCommonInfiniteScroll]',
})
export class InfiniteScrollDirective implements OnInit, OnDestroy {
  @Input() vitamuiCommonInfiniteScrollThreshold = DEFAULT_SCROLL_THRESHOLD;
  @Input() vitamuiCommonInfiniteScrollDisable = false;

  @Output() vitamuiScroll = new EventEmitter<void>();

  private scrollElement: Element;

  constructor() {
  }

  ngOnInit() {
    const sideNavElement = document.getElementsByClassName('mat-sidenav-content');
    const windowElement = document.getElementsByTagName('div');
    const scrollElement = sideNavElement?.length > 0 ? sideNavElement[0] : windowElement[0];

    this.scrollElement = this.getScrollElement();
    if (this.scrollElement) {
        scrollElement.addEventListener('scroll', () => {
            if (!this.vitamuiCommonInfiniteScrollDisable) {
                const height = scrollElement.scrollHeight - scrollElement.clientHeight;
                const scrollRatio = scrollElement.scrollTop / height;

                if (scrollRatio >= this.vitamuiCommonInfiniteScrollThreshold) {
                    this.vitamuiScroll.emit();
                }
            }
        });
    }
  }

  ngOnDestroy() {
    if (this.scrollElement) {
      this.scrollElement.removeEventListener('scroll', this.scroll, true);
    }
  }

  private getScrollElement(): Element {
    const sideNavElement = document.getElementsByClassName(
      'mat-sidenav-content'
    );
    const windowElement = document.getElementsByTagName('div');
    return sideNavElement?.length > 0
      ? sideNavElement[sideNavElement.length - 1]
      : windowElement?.length > 0
        ? windowElement[0]
        : null;
  }

  private scroll = (): void => {
    if (!this.vitamuiCommonInfiniteScrollDisable) {
      const height =
        this.scrollElement.scrollHeight - this.scrollElement.clientHeight;
      const scrollRatio = this.scrollElement.scrollTop / height;
      if (scrollRatio >= this.vitamuiCommonInfiniteScrollThreshold) {
        this.vitamuiScroll.emit();
      }
    }
  }

}
