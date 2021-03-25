import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Event} from 'projects/vitamui-library/src/public-api';

import {SecurisationService} from '../securisation.service';
import {ExternalParametersService, ExternalParameters} from 'ui-frontend-common';
import {MatSnackBar} from '@angular/material/snack-bar';
import '@angular/localize/init';

@Component({
  selector: 'app-securisation-preview',
  templateUrl: './securisation-preview.component.html',
  styleUrls: ['./securisation-preview.component.scss']
})
export class SecurisationPreviewComponent implements OnInit {

  @Input() securisation: Event;
  @Output() previewClose: EventEmitter<any> = new EventEmitter();

  accessContractId: string;

  constructor(
    private securisationService: SecurisationService,
    private externalParameterService: ExternalParametersService,
    private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.externalParameterService.getUserExternalParameters().subscribe(parameters => {
      const accessContratId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessContratId && accessContratId.length > 0) {
        this.accessContractId = accessContratId;
      } else {
        this.snackBar.open(
          $localize`:access contrat not set message@@accessContratNotSetErrorMessage:Aucun contrat d'accès n'est associé à l'utiisateur`, 
          null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000
        });
      }
    });
  }

  emitClose() {
    this.previewClose.emit();
  }

  downloadReport() {
    this.securisationService.download(this.securisation.id, this.accessContractId);
  }
}
