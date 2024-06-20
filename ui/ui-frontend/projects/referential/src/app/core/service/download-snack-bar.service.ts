import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { DownloadSnackBarComponent, VitamUISnackBarService } from 'vitamui-library';

@Injectable({
  providedIn: 'root',
})
export class DownloadSnackBarService {
  private overlayRef: OverlayRef;
  private downloadBarRef: any;

  public cancelDownload = new Subject<void>();

  constructor(
    private overlay: Overlay,
    private snackBarService: VitamUISnackBarService,
  ) {
    const positionStrategy = this.overlay.position().global();
    positionStrategy.left('0');
    positionStrategy.right('0');
    positionStrategy.bottom('0');
    this.overlayRef = this.overlay.create({
      width: '100%',
      height: '80px',
      positionStrategy,
    });
  }

  public openDownloadBar(): void {
    this.initBar();
  }

  public close(): void {
    this.overlayRef.detach();
  }

  private initBar(): void {
    if (this.overlayRef.hasAttached()) {
      this.overlayRef.detach();
    }

    const downloadBarPortal = new ComponentPortal(DownloadSnackBarComponent);
    this.downloadBarRef = this.overlayRef.attach(downloadBarPortal);
    this.downloadBarRef.instance.cancel.subscribe(() => {
      this.overlayRef.detach();
      this.snackBarService.open({ message: 'DOWNLOAD.CANCELLED', icon: 'vitamui-icon-info' });
      this.cancelDownload.next();
    });
  }
}
