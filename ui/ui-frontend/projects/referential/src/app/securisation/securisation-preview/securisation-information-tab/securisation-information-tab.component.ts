import {Component, Input, OnInit} from '@angular/core';
import {Event} from 'projects/vitamui-library/src/public-api';
import {SecurisationService} from '../../securisation.service';


@Component({
  selector: 'app-securisation-information-tab',
  templateUrl: './securisation-information-tab.component.html',
  styleUrls: ['./securisation-information-tab.component.scss']
})
export class SecurisationInformationTabComponent implements OnInit {

  @Input()
  securisation: Event;
  timestamp: { signerCertIssuer: string, genTime: Date };

  constructor(private securisationService: SecurisationService) {
  }

  ngOnInit() {
    if (this.securisation.parsedData) {
      this.securisationService.getInfoFromTimestamp(this.securisation.parsedData.TimeStampToken).subscribe(response => {
        this.timestamp = response;
      });
    }
  }
}
