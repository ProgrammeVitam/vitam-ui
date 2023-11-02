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
import { Directive, ElementRef, HostListener, Inject, Input, OnInit, Renderer2 } from '@angular/core';

@Directive({
  selector: '[vitamuiCommonResizeSidebar]',
})
export class ResizeSidebarDirective implements OnInit {
  /**
   * Orientation du block à redimensionner, permet de positionner à droite ou à gauche la barre permettant de
   * redimensionner
   * valeur possible: left ou right (par défaut)
   */
  @Input('vitamuiCommonResizeSidebar') orientation: 'right' | 'left' = 'right';

  /**
   * largeur de la barre permettant de redimensionner
   * valeur par défaut : 4px
   */
  @Input() barSize = '4px';

  /**
   * Couleur de la barre permettant de redimensionner
   * valeur par défaut #702382
   */
  @Input() barColor = 'rgb(112, 35, 130)';

  /**
   * Largeur du block à redimensionner
   * valeur par défaut 300 (px)
   */
  @Input() width = 300;

  private status = 0;

  @HostListener('window:mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    const mouse = { x: event.clientX, y: event.clientY };
    if (this.status === 1) {
      const space = Number(mouse.x) ? mouse.x : 0;
      if (this.orientation === 'left') {
        this.width = space;
        this.elementRef.nativeElement.style.width = this.width + 'px';
      } else {
        const {left} = this.elementRef.nativeElement.getBoundingClientRect();
        this.width = left - space + this.width;
        this.elementRef.nativeElement.style.width = this.width + 'px';
      }
    }
  }

  @HostListener('window:mouseup')
  onMouseUp() {
    this.status = 0;
  }

  constructor(private elementRef: ElementRef, private renderer: Renderer2, @Inject(DOCUMENT) private document) {}

  ngOnInit(): void {
    const nativeElt = this.elementRef.nativeElement;
    nativeElt.style.width = this.width + 'px';
    const child = this.createResizeBar();
    this.renderer.appendChild(nativeElt, child);
  }

  private createResizeBar() {
    const div = this.document.createElement('div');
    div.style.display = 'block';
    div.style.height = '100%';
    div.style.position = 'absolute';
    div.className = 'vitamuiResizeSidebar';
    div.style.zIndex = '99';
    div.style.top = '0';
    if (this.orientation === 'left') {
      div.style.left = `calc(100% - ${this.barSize})`;
      div.style.borderRight = `${this.barSize} solid ${this.barColor}`;
    } else {
      div.style.right = `calc(100% - ${this.barSize})`;
      div.style.borderLeft = `${this.barSize} solid ${this.barColor}`;
    }

    div.style.cursor = 'ew-resize';

    div.addEventListener('mousedown', (event) => this.setStatus(event, 1));

    return div;
  }

  private setStatus(event: MouseEvent, status: number) {
    if (status === 1) {
      event.stopPropagation();
    }
    this.status = status;
  }
}
