import { Component, OnInit, Input } from '@angular/core';
import { Event } from 'vitamui-library';
import {SecurisationService} from "../../securisation.service";


@Component({
  selector: 'app-securisation-information-tab',
  templateUrl: './securisation-information-tab.component.html',
  styleUrls: ['./securisation-information-tab.component.scss']
})
export class SecurisationInformationTabComponent implements OnInit {

  @Input()
  securisation: Event;
  timestamp: { signerCertIssuer: string, genTime: Date };

  constructor(private securisationService: SecurisationService) { }

  ngOnInit() {
    this.securisationService.getInfoFromTimestamp(this.securisation.parsedData.TimeStampToken).subscribe( response => {
      this.timestamp = response;
    });
  }
}
