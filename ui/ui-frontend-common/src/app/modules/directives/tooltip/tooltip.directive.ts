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
import { OverlayPositionBuilder } from '@angular/cdk/overlay';
import { Directive, ElementRef, HostListener, Input, OnDestroy } from '@angular/core';
import { Subscription, timer } from 'rxjs';
import { distinctUntilChanged, map } from 'rxjs/operators';

import { TooltipRef } from './tooltip-ref';
import { TooltipComponent } from './tooltip.component';
import { TooltipService } from './tooltip.service';

@Directive({
  selector: '[vitamuiCommonTooltip]'
})
export class TooltipDirective implements OnDestroy {

  @Input() vitamuiCommonTooltip: string;

  // Delay in MS
  @Input() vitamuiCommonTooltipShowDelay = 0;

  tooltipRef: TooltipRef;
  timerSub: Subscription;

  constructor(private tooltipService: TooltipService, private elementRef: ElementRef, private positionBuilder: OverlayPositionBuilder) { }

  @HostListener('mouseenter')
  showTooltip() {
    this.timerSub = timer(this.vitamuiCommonTooltipShowDelay).subscribe(() => {
      const positionStrategy = this.positionBuilder
      .flexibleConnectedTo(this.elementRef)
      .withTransformOriginOn('.vitamui-tooltip')
      .withPositions([
        { originX: 'center', originY: 'bottom', overlayX: 'start', overlayY: 'top' },
        { originX: 'center', originY: 'top', overlayX: 'start', overlayY: 'bottom' },
        { originX: 'center', originY: 'bottom', overlayX: 'end', overlayY: 'top' },
        { originX: 'center', originY: 'top', overlayX: 'end', overlayY: 'bottom' },
      ]);
      const tooltipClass$ = positionStrategy.positionChanges.pipe(
        map((position) => {
          const tooltipClasses = [];

          if (position.connectionPair.overlayY === 'bottom') {
            tooltipClasses.push('vitamui-tooltip-bottom');
          } else if (position.connectionPair.overlayY === 'top') {
            tooltipClasses.push('vitamui-tooltip-top');
          }
          if (position.connectionPair.overlayX === 'start') {
            tooltipClasses.push(' vitamui-tooltip-start');
          } else if (position.connectionPair.overlayX === 'end') {
            tooltipClasses.push(' vitamui-tooltip-end');
          }

          return tooltipClasses;
        }),
        distinctUntilChanged()
      );
      this.tooltipRef = this.tooltipService.open(this.elementRef, TooltipComponent, {
        hasBackdrop: false,
        message: this.vitamuiCommonTooltip,
        positionStrategy,
        tooltipClass: tooltipClass$
      });
    });
  }

  @HostListener('mouseleave')
  hidetooltip() {
    this.closeTooltip();
  }

  ngOnDestroy() {
    this.closeTooltip();
  }

  private closeTooltip() {
    if (this.tooltipRef) {
      this.tooltipRef.close();
    }
    if (this.timerSub) {
      this.timerSub.unsubscribe();
      this.timerSub = null;
    }
  }

}
