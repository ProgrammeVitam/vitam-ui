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
import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { DataGeneriquePopupService } from '../data-generique-popup.service';
import { PastisPopupSelectionService } from './pastis-popup-selection.service';
import { CommonModule } from '@angular/common';

@Component({
  imports: [CommonModule],
  selector: 'app-pastis-generic-popup',
  templateUrl: './pastis-generic-popup.component.html',
  styleUrls: ['./pastis-generic-popup.component.scss'],
})
export class PastisGenericPopupComponent implements OnInit {
  private pastisPopupSelectionService = inject(PastisPopupSelectionService);
  private dataGeneriquePopupService = inject(DataGeneriquePopupService);

  donnees: string[];

  @Input()
  firstChoice: string;
  @Input()
  secondChoice: string;
  @Input()
  title: string;

  @Input()
  secondPopup: boolean;

  @Output() changeStatusEvent: EventEmitter<string> = new EventEmitter<string>();

  status: boolean;

  ngOnInit(): void {
    this.dataGeneriquePopupService.currentDonnee.subscribe((donnees) => (this.donnees = donnees));
    if (this.firstChoice == null && this.firstChoice === '') {
      this.firstChoice = this.donnees[0];
    }
    if (typeof this.firstChoice === 'undefined' && this.firstChoice == null) {
      this.firstChoice = this.donnees[0];
    }
    if (typeof this.secondChoice === 'undefined' && this.secondChoice == null) {
      this.secondChoice = this.donnees[1];
    }
    if (typeof this.title === 'undefined' && this.title == null) {
      this.title = this.donnees[2];
    }
    this.status = true;
    this.pastisPopupSelectionService.value = this.firstChoice;
  }

  changeStatus(value: string): void {
    if ((this.status && value !== this.firstChoice) || (!this.status && value !== this.secondChoice)) {
      this.status = !this.status;
      this.pastisPopupSelectionService.value = value;
    }
    this.changeStatusEvent.emit(value);
  }
}
