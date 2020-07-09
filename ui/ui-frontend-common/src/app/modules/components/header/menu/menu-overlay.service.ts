import { Overlay, OverlayConfig, OverlayRef } from '@angular/cdk/overlay';
import { ComponentPortal, PortalInjector } from '@angular/cdk/portal';
import { Injectable, Injector } from '@angular/core';
import { MenuOverlayRef } from './menu-overlay-ref';
import { MenuComponent } from './menu.component';

@Injectable()
export class MenuOverlayService {

  private overlayRef: OverlayRef;

  private dialogRef: MenuOverlayRef;

  private portalInjector: PortalInjector;

  constructor(private overlay: Overlay, private injector: Injector) { }

  public open(): void {
    const positionStrategy = this.overlay
    .position()
    .global()
    .top('0')
    .right('0');

    const config = new OverlayConfig({
      hasBackdrop: true,
      positionStrategy,
      scrollStrategy: this.overlay.scrollStrategies.block()
    });

    this.overlayRef = this.overlay.create(config);
    const injectionTokens = new WeakMap();
    this.dialogRef = new MenuOverlayRef(this.overlayRef);
    injectionTokens.set(MenuOverlayRef, this.dialogRef);
    this.portalInjector = new PortalInjector(this.injector, injectionTokens);
    this.overlayRef.attach(new ComponentPortal(MenuComponent, null, this.portalInjector));
  }
}
