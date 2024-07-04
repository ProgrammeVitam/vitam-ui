import { Component, ElementRef, EventEmitter, forwardRef, Input, Output } from '@angular/core';
import { NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { EditableFieldComponent } from 'vitamui-library';
import { TranslateModule } from '@ngx-translate/core';
import { MatLegacyProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { CustomParamsComponent } from '../../custom-params/custom-params.component';
import { CdkOverlayOrigin, CdkConnectedOverlay } from '@angular/cdk/overlay';
import { NgIf, NgFor, KeyValuePipe } from '@angular/common';

export const EDITABLE_DOMAIN_INPUT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  // eslint-disable-next-line no-use-before-define
  useExisting: forwardRef(() => EditableCustomParamsComponent),
  multi: true,
};

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'editable-custom-params',
  templateUrl: './editable-custom-params.component.html',
  providers: [EDITABLE_DOMAIN_INPUT_VALUE_ACCESSOR],
  standalone: true,
  imports: [
    NgIf,
    CdkOverlayOrigin,
    NgFor,
    CustomParamsComponent,
    ReactiveFormsModule,
    MatLegacyProgressSpinnerModule,
    CdkConnectedOverlay,
    KeyValuePipe,
    TranslateModule,
  ],
})
export class EditableCustomParamsComponent extends EditableFieldComponent {
  array: any[] = [];

  selected: string;
  private domainInputClicked = false;
  get canConfirm(): boolean {
    return this.editMode && !this.control.pending && this.control.valid && this.control.dirty;
  }
  @Input()
  set defaultDomain(defaultDomain: string) {
    this._defaultDomain = defaultDomain;
    this.selected = defaultDomain;
  }
  get defaultDomain(): string {
    return this._defaultDomain;
  }
  private _defaultDomain: string;

  @Output() defaultDomainChange = new EventEmitter<string>();
  @Input() placeholder: string;
  constructor(elementRef: ElementRef) {
    super(elementRef);
  }

  getList(obj: any) {
    const map = new Map<string, string>();
    if (!!obj) {
      Object.entries(obj).forEach((array: [string, string]) => {
        map.set(array[0], array[1]);
      });
    }
    return map;
  }

  cancel() {
    super.cancel();
    this.selected = this.defaultDomain;
  }

  onClick(target: HTMLElement) {
    if (!this.editMode) {
      return;
    }
    if (this.domainInputClicked) {
      this.domainInputClicked = false;

      return;
    }
    const overlayRef = this.cdkConnectedOverlay.overlayRef;
    if (this.isInside(target, this.elementRef.nativeElement) || this.isInside(target, overlayRef.hostElement)) {
      return;
    }
    this.cancel();
  }

  onDomainInputClick() {
    this.domainInputClicked = true;
  }

  enterEditMode() {
    super.enterEditMode();
  }
}
