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
import { Overlay, OverlayConfig, OverlayRef, PositionStrategy, ScrollStrategy } from '@angular/cdk/overlay';
import { ComponentPortal, ComponentType, PortalInjector } from '@angular/cdk/portal';
import { ComponentRef, ElementRef, Injectable, Injector } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { VITAMUI_TOOLTIP_MESSAGE } from './../../injection-tokens';
import { TooltipRef } from './tooltip-ref';

interface TooltipOverlayConfig {
  hasBackdrop?: boolean;
  backdropClass?: string;
  scrollStrategy?: ScrollStrategy;
  positionStrategy?: PositionStrategy;
  message?: string;
  tooltipClass?: Observable<string[]>;
}

const DEFAULT_CONFIG: TooltipOverlayConfig = {
  hasBackdrop: true,
  backdropClass: 'transparent-backdrop'
};

@Injectable({
  providedIn: 'root'
})
export class TooltipService {

  private closeTooltip = new Subject<any>();
  public Information: string;

  constructor(private injector: Injector, private overlay: Overlay) { }

  open(origin: ElementRef, component: ComponentType<{}>, config: TooltipOverlayConfig = {}) {
    const overlayConfig = { ...DEFAULT_CONFIG, ...config };

    const overlayRef = this.createOverlay(origin, overlayConfig);

    const dialogRef = new TooltipRef(overlayRef);
    this.attachDialogContainer(overlayRef, config, component, dialogRef);

    overlayRef.backdropClick().subscribe((_) => dialogRef.close());

    return dialogRef;
  }

  getOverlayConfig(origin: ElementRef, config: TooltipOverlayConfig): OverlayConfig {
    const positionStrategy = this.overlay.position()
      .flexibleConnectedTo(origin)
      .withPositions([{ originX: 'end', originY: 'bottom', overlayX: 'center', overlayY: 'top' }]);

    const overlayConfig = new OverlayConfig({
      hasBackdrop: config.hasBackdrop,
      backdropClass: config.backdropClass,
      scrollStrategy: this.overlay.scrollStrategies.reposition(),
      positionStrategy: config.positionStrategy || positionStrategy
    });

    return overlayConfig;
  }

  private createOverlay(origin: ElementRef, config: TooltipOverlayConfig) {
    const overlayConfig = this.getOverlayConfig(origin, config);

    return this.overlay.create(overlayConfig);
  }

  private attachDialogContainer(
    overlayRef: OverlayRef,
    config: TooltipOverlayConfig,
    component: ComponentType<{}>,
    dialogRef: TooltipRef
  ) {
    const injector = this.createInjector(config, dialogRef);
    const containerPortal = new ComponentPortal(component, null, injector);
    const containerRef: ComponentRef<{}> = overlayRef.attach(containerPortal);

    return containerRef.instance;
  }

  private createInjector(config: TooltipOverlayConfig, tooltipRef: TooltipRef): PortalInjector {
    const injectionTokens = new WeakMap();
    injectionTokens.set(TooltipRef, tooltipRef);
    injectionTokens.set(VITAMUI_TOOLTIP_MESSAGE, { message: config.message, tooltipClass: config.tooltipClass });

    return new PortalInjector(this.injector, injectionTokens);
  }

  close() {
    this.closeTooltip.next();
  }

  closingTooltip(): Observable<any> {
    return this.closeTooltip.asObservable();
  }
}
