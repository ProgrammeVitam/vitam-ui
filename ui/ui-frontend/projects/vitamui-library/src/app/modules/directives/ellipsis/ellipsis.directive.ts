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
import {
  AfterViewInit,
  ComponentRef,
  Directive,
  ElementRef,
  HostBinding,
  HostListener,
  inject,
  input,
  OnDestroy,
  OnInit,
  Renderer2,
} from '@angular/core';
import { coerceBooleanProperty } from '@angular/cdk/coercion';
import { ConnectedPosition, Overlay, OverlayPositionBuilder, OverlayRef } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { CommonTooltipComponent } from '../../components/common-tooltip/common-tooltip.component';

const ELLIPSIS_TOOLTIP_POSITION: ConnectedPosition = {
  originX: 'start',
  originY: 'bottom',
  overlayX: 'start',
  overlayY: 'top',
};

@Directive({
  selector: '[vitamuiCommonEllipsis]',
  standalone: false,
})
export class EllipsisDirective implements OnInit, AfterViewInit, OnDestroy {
  private renderer = inject(Renderer2);
  private elementRef = inject(ElementRef);
  private overlay = inject(Overlay);
  private overlayPositionBuilder = inject(OverlayPositionBuilder);

  isToolTipOnMouseEnter = input(false, { transform: coerceBooleanProperty });
  vitamuiCommonEllipsisLines = input(1);
  breakAll = input(false, { transform: coerceBooleanProperty });

  domElement: HTMLElement;

  private isTruncated = false;
  private overlayRef: OverlayRef;
  private tooltipRef: ComponentRef<CommonTooltipComponent>;

  ngOnInit(): void {
    this.domElement = this.elementRef.nativeElement;
    this.renderer.addClass(this.domElement, 'text-ellipsis');
    if (this.breakAll()) this.renderer.addClass(this.domElement, 'break-all');
    this.checkTruncation();
  }

  ngAfterViewInit(): void {
    this.renderer.setProperty(this.domElement, 'scrollTop', 1);
    this.checkTruncation();
  }

  ngOnDestroy(): void {
    this.closeTooltip();
    this.overlayRef?.dispose();
  }

  @HostListener('window:resize')
  checkTruncation() {
    this.isTruncated = this.domElement.offsetHeight < this.domElement.scrollHeight;
    if (!this.isTruncated) this.closeTooltip();
  }

  @HostListener('mouseenter')
  onMouseEnter() {
    if (this.isToolTipOnMouseEnter()) this.checkTruncation();
    if (this.isTruncated) this.openTooltip();
  }

  @HostListener('mouseleave')
  onMouseLeave() {
    this.closeTooltip();
  }

  @HostBinding('style.-webkit-line-clamp')
  get lineClamp() {
    return this.vitamuiCommonEllipsisLines() > 1 ? this.vitamuiCommonEllipsisLines() : null;
  }

  private openTooltip() {
    if (!this.overlayRef) this.createOverlayRef();
    if (!this.overlayRef.hasAttached()) {
      this.tooltipRef = this.overlayRef.attach(new ComponentPortal(CommonTooltipComponent));
      this.tooltipRef.instance.text = this.domElement.textContent;
      this.tooltipRef.instance.position = 'BOTTOM';
    }
  }

  private closeTooltip() {
    if (this.overlayRef?.hasAttached()) this.overlayRef.detach();
  }

  private createOverlayRef() {
    const positionStrategy = this.overlayPositionBuilder
      .flexibleConnectedTo(this.elementRef)
      .withPositions([ELLIPSIS_TOOLTIP_POSITION])
      .withPush(false);
    this.overlayRef = this.overlay.create({ positionStrategy, scrollStrategy: this.overlay.scrollStrategies.reposition() });
  }
}
