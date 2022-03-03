import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DataGeneriquePopupService {

  private test = ['', '', ''];
  private donneeSource = new BehaviorSubject(this.test);

  currentDonnee = this.donneeSource.asObservable();

  constructor() { }

  changeDonnees(donnees: Array<string>) {
    this.donneeSource.next(donnees);
  }
}
