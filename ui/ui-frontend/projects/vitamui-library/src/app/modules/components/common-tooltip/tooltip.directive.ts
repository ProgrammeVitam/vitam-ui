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
import { coerceBooleanProperty } from '@angular/cdk/coercion';
import { ConnectedPosition, Overlay, OverlayPositionBuilder, OverlayRef } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { ComponentRef, Directive, ElementRef, HostListener, Input, OnDestroy, OnInit, SimpleChanges, OnChanges } from '@angular/core';
import { TooltipPosition } from './TooltipPosition.enum';
import { CommonTooltipComponent } from './common-tooltip.component';

const VITAMUI_TOOL_TIP_POSITIONS: { [key: string]: ConnectedPosition } = {
  TOP: {
    originX: 'start',
    originY: 'top',
    overlayX: 'start',
    overlayY: 'bottom',
  },
  BOTTOM: {
    originX: 'start',
    originY: 'bottom',
    overlayX: 'start',
    overlayY: 'top',
  },
  LEFT: {
    originX: 'start',
    originY: 'center',
    overlayX: 'end',
    overlayY: 'center',
  },
  RIGHT: {
    originX: 'end',
    originY: 'center',
    overlayX: 'start',
    overlayY: 'center',
  },
};

const TOOLTIP_TRIGGER_CLASS = 'tooltip-trigger';

@Directive({
  selector: '[vitamuiTooltip]',
  standalone: false,
})
export class TooltipDirective implements OnInit, OnDestroy, OnChanges {
  @Input('vitamuiTooltip') text?: string;
  @Input() outline = false;
  @Input() vitamuiTooltipPosition: TooltipPosition = TooltipPosition.BOTTOM;
  @Input() vitamuiTooltipClass?: string;
  @Input() vitamuiTooltipShowDelay = 0;
  /** Disables the display of the tooltip. */
  @Input({ alias: 'vitamuiTooltipDisabled', transform: coerceBooleanProperty }) disabled: boolean = false;

  #tooltipRef?: ComponentRef<CommonTooltipComponent>;
  #showTimeoutId: ReturnType<typeof setTimeout>;
  #hideTimeoutId: ReturnType<typeof setTimeout>;
  #overlayRef: OverlayRef;

  constructor(
    private overlay: Overlay,
    private overlayPositionBuilder: OverlayPositionBuilder,
    private elementRef: ElementRef,
  ) {}

  ngOnInit(): void {
    if (!this.disabled && this.text) (this.elementRef.nativeElement as HTMLElement).classList.add(TOOLTIP_TRIGGER_CLASS);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (this.#tooltipRef?.instance) {
      this.updateTooltip();
    }
    if (changes.disabled) {
      if (this.disabled) (this.elementRef.nativeElement as HTMLElement).classList.remove(TOOLTIP_TRIGGER_CLASS);
      else (this.elementRef.nativeElement as HTMLElement).classList.add(TOOLTIP_TRIGGER_CLASS);

      // If tooltip is disabled, hide immediately.
      if (this.disabled && this.#overlayRef) {
        this.hide();
      }
    }
  }

  ngOnDestroy() {
    this.closeToolTip();
    this.#overlayRef?.dispose();
  }

  @HostListener('mouseenter')
  @HostListener('focus')
  show() {
    clearTimeout(this.#hideTimeoutId);
    clearTimeout(this.#showTimeoutId);

    if (!this.disabled && this.text) {
      this.openToolTip();
    }
  }

  @HostListener('mouseleave')
  @HostListener('blur')
  @HostListener('click') // If the tooltip host is destroyed (removed from DOM) on click (e.g.: an option in a select input), we'll never have mouseleave/blur event, so we also hide the tooltip on click
  hide() {
    clearTimeout(this.#showTimeoutId);
    this.#hideTimeoutId = setTimeout(() => this.closeToolTip(), this.vitamuiTooltipShowDelay);
  }

  private openToolTip() {
    if (!this.#overlayRef) this.createOverlayRef();
    if (!this.#overlayRef.hasAttached()) {
      this.#showTimeoutId = setTimeout(() => {
        const tooltipPortal = new ComponentPortal(CommonTooltipComponent);
        this.#tooltipRef = this.#overlayRef.attach(tooltipPortal);
        this.updateTooltip();
      }, this.vitamuiTooltipShowDelay);
    }
  }

  private updateTooltip() {
    this.#tooltipRef.instance.text = this.text;
    this.#tooltipRef.instance.position = this.vitamuiTooltipPosition;
    this.#tooltipRef.instance.outline = this.outline;
    this.#tooltipRef.instance.className = this.vitamuiTooltipClass;
  }

  private createOverlayRef() {
    const position = VITAMUI_TOOL_TIP_POSITIONS[this.vitamuiTooltipPosition];
    const positionStrategy = this.overlayPositionBuilder.flexibleConnectedTo(this.elementRef).withPositions([position]).withPush(false);
    this.#overlayRef = this.overlay.create({
      positionStrategy,
      scrollStrategy: this.overlay.scrollStrategies.reposition(),
    });
  }

  private closeToolTip() {
    if (this.#overlayRef?.hasAttached()) {
      this.#overlayRef.detach();
    }
  }
}
