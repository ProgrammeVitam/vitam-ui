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
// tslint:disable:max-classes-per-file
import {
  Component, Directive, EventEmitter, forwardRef, Input, NgModule, Output, Pipe, PipeTransform
} from '@angular/core';
import {
  AsyncValidatorFn, ControlValueAccessor, NG_VALUE_ACCESSOR, ValidatorFn
} from '@angular/forms';

@Component({ selector: 'vitamui-common-navbar', template: '<ng-content></ng-content>'})
export class NavbarStubComponent {
  @Input() appId: string;
  @Input() hideTenantMenu = false;
  @Input() hideCustomerMenu = false;
  @Input() customers: any[];
}

@Component({ selector: 'vitamui-common-application-select-content', template: '<ng-content></ng-content>'})
export class ApplicationSelectContentStubComponent {
  @Input() applications: any[];
  @Input() categories: any;
  @Input() isModalMenu: boolean;
}

@Component({ selector: 'vitamui-common-customer-select', template: '<ng-content></ng-content>'})
export class VitamUICustomerSelectStubComponent {
}

@Component({
  selector: 'vitamui-common-vitamui-duration-input',
  template: '<ng-content></ng-content>',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VitamUIDurationInputStubComponent),
    multi: true,
  }]
})
export class VitamUIDurationInputStubComponent implements ControlValueAccessor {
  @Input() placeholder: any;

  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}

@Component({ selector: 'vitamui-common-tenant-select', template: '<ng-content></ng-content>'})
export class VitamUITenantSelectStubComponent {
}

@Component({selector: 'vitamui-common-stepper', template: '<ng-content></ng-content>' })
export class StepperStubComponent {
  @Input() selectedIndex: number;
}

@Component({ selector: 'vitamui-common-input-error', template: '<ng-content></ng-content>' })
export class VitamUIInputErrorStubComponent {}

@Component({
  selector: 'vitamui-common-input',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VitamUIInputStubComponent),
    multi: true,
  }]
})
export class VitamUIInputStubComponent implements ControlValueAccessor {
  @Input() placeholder: any;

  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}

@Component({
  selector: 'vitamui-common-input-positive-number',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VitamUIInputPositiveNumberStubComponent),
    multi: true,
  }]
})
export class VitamUIInputPositiveNumberStubComponent implements ControlValueAccessor {
  @Input() placeholder: any;

  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}

@Component({
  selector: 'vitamui-common-list-input',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VitamUIListInputStubComponent),
    multi: true,
  }]
})
export class VitamUIListInputStubComponent implements ControlValueAccessor {
  @Input () placeholder: any;

  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}

@Component({
  selector: 'vitamui-common-textarea',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VitamUITextareaStubComponent),
    multi: true,
  }]
})
export class VitamUITextareaStubComponent implements ControlValueAccessor {
  @Input() placeholder: any;

  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}

@Component({
  selector: 'vitamui-common-slide-toggle',
  template: '<ng-content></ng-content>',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VitamUISlideToggleStubComponent),
    multi: true,
  }]
})
export class VitamUISlideToggleStubComponent implements ControlValueAccessor {
  @Input() disabled: boolean;
  @Input() checked: any;
  value: boolean;
  writeValue(value: boolean) { this.value = value; }
  registerOnChange() {}
  registerOnTouched() {}
}

// Editable fields

@Directive()
// tslint:disable-next-line:directive-class-suffix
export class EditableFieldStubComponent implements ControlValueAccessor {
  @Input() validator: ValidatorFn;
  @Input() asyncValidator: AsyncValidatorFn;
  value: string;
  writeValue(value: string) { this.value = value; }
  registerOnChange() {}
  registerOnTouched() {}
}

@Component({
  selector: 'vitamui-common-editable-email-input',
  template: '{{value}}',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VitamUIEditableEmailInputStubComponent),
    multi: true,
  }]
})
export class VitamUIEditableEmailInputStubComponent extends EditableFieldStubComponent {
  @Input() domains: string[];
}

@Component({ selector: 'vitamui-common-field-error', template: '<ng-content></ng-content>' })
export class VitamUIFieldErrorStubComponent {}

@Component({
  selector: 'vitamui-common-editable-file',
  template: '{{value}}',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VitamUIEditableFileStubComponent),
    multi: true,
  }]
})
export class VitamUIEditableFileStubComponent extends EditableFieldStubComponent {}

@Component({
  selector: 'vitamui-common-editable-input',
  template: '{{value}}',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VitamUIEditableInputStubComponent),
    multi: true,
  }]
})
export class VitamUIEditableInputStubComponent extends EditableFieldStubComponent {}

@Component({
  selector: 'vitamui-common-editable-textarea',
  template: '{{value}}',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VitamUIEditableTextareaStubComponent),
    multi: true,
  }]
})
export class VitamUIEditableTextareaStubComponent extends EditableFieldStubComponent {}

@Component({
  selector: 'vitamui-common-editable-select',
  template: '{{value}}',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VitamUIEditableSelectStubComponent),
    multi: true,
  }]
})
export class VitamUIEditableSelectStubComponent extends EditableFieldStubComponent {}

@Component({ selector: 'vitamui-common-editable-option', template: '' })
export class VitamUIEditableOptionStubComponent {
  @Input() value: any;
  @Input() content: any;
  @Input() disabled: any;
}

@Component({
  selector: 'vitamui-common-editable-toggle-group',
  template: '{{value}}',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VitamUIEditableToggleGroupStubComponent),
    multi: true,
  }]
})
export class VitamUIEditableToggleGroupStubComponent extends EditableFieldStubComponent {}

@Component({ selector: 'vitamui-common-editable-button-toggle', template: '' })
export class VitamUIEditableButtonToggleStubComponent {
  @Input() value: any;
}

@Component({
  selector: 'vitamui-common-editable-level-input',
  template: '{{prefix}}',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => EditableLevelInputStubComponent),
    multi: true
  }]
})
export class EditableLevelInputStubComponent implements ControlValueAccessor {
  @Input() validator: any;
  @Input() asyncValidator: any;
  @Input() prefix: any;
  @Input() disabled: boolean;
  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}

@Pipe({ name: 'strongify' })
export class StrongifyStubPipe implements PipeTransform {
  transform(value: any): any { return value; }
}

@Pipe({ name: 'truncate' })
export class TruncateStubPipe implements PipeTransform {
  transform(value: string = ''): any { return value; }
}

@Pipe({ name: 'translate' })
export class TranslateStubPipe implements PipeTransform {
  transform(value: string = ''): any { return value; }
}

@Directive({ selector: '[vitamuiCommonInfiniteScroll]' })
export class InfiniteScrollStubDirective {
  @Input() vitamuiCommonInfiniteScrollThreshold: any;
  @Input() vitamuiCommonInfiniteScrollDisable: any;
  @Output() vitamuiScroll = new EventEmitter<void>();
}

@Directive({
  selector: '[vitamuiCommonRowCollapse]',
  exportAs: 'vitamuiRowCollapse'
})
export class RowCollapseStubDirective {
  @Input() vitamuiCommonCollapse: any;
}


@Directive({
  selector: '[vitamuiCommonRowCollapseTriggerFor]'
})
export class RowCollapseTriggerForStubDirective {
  @Input() vitamuiCommonRowCollapseTriggerFor: any;
}

@NgModule({
  declarations: [
    ApplicationSelectContentStubComponent,
    VitamUICustomerSelectStubComponent,
    VitamUIDurationInputStubComponent,
    VitamUIEditableButtonToggleStubComponent,
    VitamUIEditableEmailInputStubComponent,
    VitamUIEditableFileStubComponent,
    VitamUIEditableInputStubComponent,
    VitamUIEditableOptionStubComponent,
    VitamUIEditableSelectStubComponent,
    VitamUIEditableTextareaStubComponent,
    VitamUIEditableToggleGroupStubComponent,
    VitamUIFieldErrorStubComponent,
    VitamUIInputErrorStubComponent,
    VitamUIInputPositiveNumberStubComponent,
    VitamUIInputStubComponent,
    VitamUIListInputStubComponent,
    VitamUISlideToggleStubComponent,
    VitamUITenantSelectStubComponent,
    VitamUITextareaStubComponent,
    EditableLevelInputStubComponent,
    InfiniteScrollStubDirective,
    NavbarStubComponent,
    RowCollapseStubDirective,
    RowCollapseTriggerForStubDirective,
    StepperStubComponent,
    StrongifyStubPipe,
    TruncateStubPipe,
    TranslateStubPipe
  ],
  exports: [
    ApplicationSelectContentStubComponent,
    VitamUICustomerSelectStubComponent,
    VitamUIDurationInputStubComponent,
    VitamUIEditableButtonToggleStubComponent,
    VitamUIEditableEmailInputStubComponent,
    VitamUIEditableFileStubComponent,
    VitamUIEditableInputStubComponent,
    VitamUIEditableOptionStubComponent,
    VitamUIEditableSelectStubComponent,
    VitamUIEditableTextareaStubComponent,
    VitamUIEditableToggleGroupStubComponent,
    VitamUIFieldErrorStubComponent,
    VitamUIInputErrorStubComponent,
    VitamUIInputPositiveNumberStubComponent,
    VitamUIInputStubComponent,
    VitamUIListInputStubComponent,
    VitamUISlideToggleStubComponent,
    VitamUITenantSelectStubComponent,
    VitamUITextareaStubComponent,
    EditableLevelInputStubComponent,
    InfiniteScrollStubDirective,
    NavbarStubComponent,
    RowCollapseStubDirective,
    RowCollapseTriggerForStubDirective,
    StepperStubComponent,
    StrongifyStubPipe,
    TruncateStubPipe,
    TranslateStubPipe
  ]
})
export class VitamUICommonTestModule {}
