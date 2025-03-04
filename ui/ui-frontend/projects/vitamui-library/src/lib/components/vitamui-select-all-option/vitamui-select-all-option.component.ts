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
/* eslint-disable @angular-eslint/component-selector */
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  HostBinding,
  HostListener,
  Inject,
  Input,
  OnDestroy,
  OnInit,
  Optional,
  Output,
} from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { MatPseudoCheckboxState } from '@angular/material/core';
import { MatOptgroup, MatOption, MatOptionParentComponent, MAT_OPTION_PARENT_COMPONENT } from '@angular/material/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'vitamui-select-all-option',
  templateUrl: './vitamui-select-all-option.component.html',
  styleUrls: ['./vitamui-select-all-option.component.scss'],
  standalone: false,
})
export class VitamUISelectAllOptionComponent extends MatOption implements OnInit, OnDestroy {
  // You need to provide either a control or a model
  // If you provide a model, you need to subscribe to the toggleSelectionEvent to update the selection
  @Input() control: AbstractControl;
  @Input() value: any[];

  @Input() values: any[] = [];
  @Input() title: string;

  protected unsubscribe: Subject<void>;
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

  constructor(
    elementRef: ElementRef<HTMLElement>,
    changeDetectorRef: ChangeDetectorRef,
    @Optional() @Inject(MAT_OPTION_PARENT_COMPONENT) parent: MatOptionParentComponent,
    @Optional() group: MatOptgroup,
  ) {
    super(elementRef, changeDetectorRef, parent, group);
  }

  ngOnInit(): void {
    this.refresh();

    if (this.control) {
      this.unsubscribe = new Subject<any>();

      this.control.valueChanges.pipe(takeUntil(this.unsubscribe)).subscribe(() => {
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
      return Array.isArray(this.control.value) ? this.control.value.filter((el) => el !== null).length : 0;
    } else {
      return this.value ? this.value.filter((el) => el !== null).length : 0;
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
