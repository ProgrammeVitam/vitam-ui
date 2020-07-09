import { OverlayRef } from '@angular/cdk/overlay';

export class MenuOverlayRef {

    constructor(private overlayRef: OverlayRef) { }

    get overlay() { return this.overlayRef; }

    public close(): void {
        this.overlayRef.dispose();
    }
}
