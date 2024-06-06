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
import { Component, ElementRef, forwardRef, Inject, Input, ViewChild } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';

import { EditableFieldComponent } from 'vitamui-library';
import { PatternComponent } from '../../pattern/pattern.component';
import { DOCUMENT } from '@angular/common';
/*eslint no-use-before-define: "error"*/
export const EDITABLE_PATTERNS_INPUT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => EditablePatternsComponent),
  multi: true,
};

@Component({
  selector: 'app-editable-patterns',
  templateUrl: './editable-patterns.component.html',
  providers: [EDITABLE_PATTERNS_INPUT_VALUE_ACCESSOR],
})
export class EditablePatternsComponent extends EditableFieldComponent {
  @Input() options: Array<{ value: string; disabled: boolean }>;

  @ViewChild(PatternComponent, { static: false }) pattern: PatternComponent;

  private patternClicked = false;

  constructor(
    elementRef: ElementRef,
    @Inject(DOCUMENT) private document: Document,
  ) {
    super(elementRef);
  }

  onClick(target: HTMLElement) {
    if (!this.editMode) {
      return;
    }
    if (this.patternClicked) {
      this.patternClicked = false;

      return;
    }
    const overlayRef = this.cdkConnectedOverlay.overlayRef;
    // Overlay has same id as the "select" element, suffixed by "-panel"
    const selectOverlay = this.document.querySelector(`#${this.pattern.select.id}-panel`) as HTMLElement;
    if (
      this.isInside(target, this.elementRef.nativeElement) ||
      this.isInside(target, overlayRef.hostElement) ||
      this.isInside(target, selectOverlay ? selectOverlay : null)
    ) {
      return;
    }
    this.cancel();
  }

  onPatternClick() {
    this.patternClicked = true;
  }

  protected isInside(target: HTMLElement, element: HTMLElement): boolean {
    return element && (target === element || element.contains(target));
  }
}
