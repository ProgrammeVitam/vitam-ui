/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
import { animate, AUTO_STYLE, state, style, transition, trigger } from '@angular/animations';
import { Component, Input, OnInit } from '@angular/core';
import { AccessionRegisterDetail, rotate90Animation } from 'ui-frontend-common';

@Component({
  selector: 'app-accession-register-detail',
  templateUrl: './accession-register-detail.component.html',
  styleUrls: ['./accession-register-detail.component.scss'],
  animations: [
    trigger('collapse', [
      state('false', style({ height: AUTO_STYLE, visibility: AUTO_STYLE })),
      state('true', style({ height: '0', visibility: 'hidden' })),
      transition('false => true', animate(300 + 'ms ease-in')),
      transition('true => false', animate(300 + 'ms ease-out')),
    ]),
    trigger('rotateAnimation', [
      state('collapse', style({ transform: 'rotate(-180deg)' })),
      state('expand', style({ transform: 'rotate(0deg)' })),
      transition('expand <=> collapse', animate('200ms ease-out')),
    ]),
    rotate90Animation,
  ],
})
export class AccessionRegisterDetailComponent implements OnInit {
  @Input()
  accessionRegisterDetail: AccessionRegisterDetail;

  longCommentSize: number = 200;
  hasLongComment: boolean = false;
  showFullComment: boolean = false;

  constructor() {}

  ngOnInit(): void {
    const comment = this.accessionRegisterDetail.comment;
    this.hasLongComment = !(comment == undefined || comment.length < 1 || comment.join('').length < this.longCommentSize);
  }

  onClicShowMoreOrLessOfComment() {
    this.showFullComment = !this.showFullComment;
  }

  formatedComment() {
    if (this.accessionRegisterDetail.comment == undefined || this.accessionRegisterDetail.comment.length < 1) {
      return '';
    }
    if (this.showFullComment) {
      return this.accessionRegisterDetail.comment.join('\n\n');
    }
    const premierParagraph = this.accessionRegisterDetail.comment[0];
    if (premierParagraph) {
      return premierParagraph.substring(0, 200);
    }
    return '';
  }
}
