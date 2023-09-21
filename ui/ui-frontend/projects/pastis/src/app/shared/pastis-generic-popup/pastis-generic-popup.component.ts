import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Subscription } from 'rxjs';
import { DataGeneriquePopupService } from '../data-generique-popup.service';
import { PastisPopupSelectionService } from './pastis-popup-selection.service';

@Component({
  selector: 'pastis-generic-popup',
  templateUrl: './pastis-generic-popup.component.html',
  styleUrls: ['./pastis-generic-popup.component.scss'],
})
export class PastisGenericPopupComponent implements OnInit, OnDestroy {
  @Input() firstChoice: string;
  @Input() secondChoice: string;
  @Input() title: string;
  @Input() secondPopup: boolean;

  @Output() changeStatusEvent: EventEmitter<string> = new EventEmitter<string>();

  donnees: string[];
  status: boolean;

  private subscriptions = new Subscription();

  constructor(
    private pastisPopupSelectionService: PastisPopupSelectionService,
    private dataGeneriquePopupService: DataGeneriquePopupService
  ) {}

  ngOnInit(): void {
    this.subscriptions.add(this.dataGeneriquePopupService.currentDonnee.subscribe((donnees) => (this.donnees = donnees)));
    if (this.firstChoice === null && this.firstChoice === '') {
      this.firstChoice = this.donnees[0];
    }
    if (typeof this.firstChoice === 'undefined' && this.firstChoice === null) {
      this.firstChoice = this.donnees[0];
    }
    if (typeof this.secondChoice === 'undefined' && this.secondChoice === null) {
      this.secondChoice = this.donnees[1];
    }
    if (typeof this.title === 'undefined' && this.title === null) {
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

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
