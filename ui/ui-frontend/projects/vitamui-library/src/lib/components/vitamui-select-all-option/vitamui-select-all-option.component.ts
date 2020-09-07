/* tslint:disable:component-selector */
import {ChangeDetectorRef, Component, ElementRef, EventEmitter, HostBinding, HostListener, Inject,
  Input, OnDestroy, OnInit, Optional, Output} from '@angular/core';
import {AbstractControl} from '@angular/forms';
import {MAT_OPTION_PARENT_COMPONENT, MatOptgroup, MatOption,
  MatOptionParentComponent, MatPseudoCheckboxState} from '@angular/material/core';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';

@Component({
  selector: 'vitamui-select-all-option',
  templateUrl: './vitamui-select-all-option.component.html',
  styleUrls: ['./vitamui-select-all-option.component.scss']
})
export class VitamUISelectAllOptionComponent extends MatOption implements OnInit, OnDestroy {
  // You need to provide either a control or a model
  // If you provide a model, you need to subscribe to the toggleSelectionEvent to update the selection
  @Input() control: AbstractControl;
  @Input() value: any[];

  @Input() values: any[] = [];
  @Input() title: string;

  protected unsubscribe: Subject<any>;
  @Output() toggleSelection: EventEmitter<any[]> = new EventEmitter();

  @HostBinding('class') cssClass = 'mat-option';
  @HostListener('click') click(): void {
    this._selectViaInteraction();

    if (this.control) {
      this.control.setValue(this.selected ? this.values : []);
    } else {
      this.toggleSelection.emit(!this.selectedAll ? this.values : []);
    }
  }

  constructor(elementRef: ElementRef<HTMLElement>,
              changeDetectorRef: ChangeDetectorRef,
              @Optional() @Inject(MAT_OPTION_PARENT_COMPONENT) parent: MatOptionParentComponent,
              @Optional() group: MatOptgroup) {
    super(elementRef, changeDetectorRef, parent, group);
  }

  ngOnInit(): void {
    this.refresh();

    if (this.control) {
      this.unsubscribe = new Subject<any>();

      this.control.valueChanges
        .pipe(takeUntil(this.unsubscribe))
        .subscribe(() => {
          this.refresh();
        });
      }
  }

  ngOnDestroy(): void {
    super.ngOnDestroy();

    if (this.control) {
      this.unsubscribe.next();
      this.unsubscribe.complete();
    }
  }

  get selectedItemsCount(): number {
    if (this.control) {
      return Array.isArray(this.control.value) ? this.control.value.filter(el => el !== null).length : 0;
    } else {
      return this.value ? this.value.filter(el => el !== null).length : 0;
    }

  }

  get selectedAll(): boolean {
    return this.selectedItemsCount === this.values.length;
  }

  get selectedPartially(): boolean {
    const selectedItemsCount = this.selectedItemsCount;
    return selectedItemsCount > 0 && selectedItemsCount < this.values.length;
  }

  get checkboxState(): MatPseudoCheckboxState {
    let state: MatPseudoCheckboxState = 'unchecked';

    if (this.selectedAll) {
      state = 'checked';
    } else if (this.selectedPartially) {
      state = 'indeterminate';
    }

    return state;
  }

  refresh(): void {
    if (this.selectedItemsCount > 0) {
      this.select();
    } else {
      this.deselect();
    }
  }
}

