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
import { DOCUMENT } from '@angular/common';
import { Directive, ElementRef, HostListener, Inject, Input, OnInit } from '@angular/core';

@Directive({
  // eslint-disable-next-line @angular-eslint/directive-selector
  selector: '[vitamuiVerticalResizeSidebar]',
})
export class ResizeVerticalDirective implements OnInit {
  @Input('vitamuiVerticalResizeSidebar') orientation: 'top' | 'bottom' = 'top';
  @Input() paddingSize = 75;
  @Input() minSize = 150;

  isDragging = false;
  siblingElement: Element;
  @HostListener('window:mousedown', ['$event'])
  onMouseDown(event: MouseEvent) {
    if (event && this.elementRef && event.target === this.elementRef.nativeElement) {
      this.isDragging = true;
    }
  }

  @HostListener('window:mouseup')
  onMouseUp() {
    this.isDragging = false;
  }

  @HostListener('window:mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    if (!this.isDragging) {
      return false;
    }

    let height;
    if (this.orientation === 'top') {
      height = `height:${Math.max(event.clientY - this.paddingSize, this.minSize)}px`;
    } else {
      const { top } = this.elementRef.nativeElement.getBoundingClientRect();
      const clientHeight = this.siblingElement.clientHeight;
      height = `height:${Math.max(top - event.clientY + clientHeight, this.minSize)}px`;
    }
    this.siblingElement.setAttribute('style', `flex:0 0 auto;max-height:75%;${height}`);
  }


  constructor(private elementRef: ElementRef, @Inject(DOCUMENT) private document: any) {}

  ngOnInit(): void {
    const id = 'vitamui-vertical-resize-bar' + Math.floor(Math.random() * 10);
    const nativeElt = this.elementRef.nativeElement;
    nativeElt.id = id;
    nativeElt.className = 'vitamui-sidepanel-resize-sidebar';
    if (this.orientation === 'top') {
      this.siblingElement = this.document.querySelector('#' + id).previousElementSibling;
    } else {
      this.siblingElement = this.document.querySelector('#' + id).nextElementSibling;
    }
  }
}
