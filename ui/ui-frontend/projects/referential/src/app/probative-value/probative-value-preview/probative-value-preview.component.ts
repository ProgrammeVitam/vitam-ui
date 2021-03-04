import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute} from '@angular/router';
import {AccessContractService} from '../../access-contract/access-contract.service';
import {ProbativeValueService} from '../probative-value.service';
import {ExternalParametersService, ExternalParameters, AccessContract} from 'ui-frontend-common';
import '@angular/localize/init';

@Component({
  selector: 'app-probative-value-preview',
  templateUrl: './probative-value-preview.component.html',
  styleUrls: ['./probative-value-preview.component.scss']
})
export class ProbativeValuePreviewComponent implements OnInit {

  @Input() probativeValue: any;
  @Output() previewClose: EventEmitter<any> = new EventEmitter();

  accessContracts: AccessContract[];
  accessContractId: string;

  constructor(
    private probativeValueService: ProbativeValueService,
    private accessContractService: AccessContractService,
    private externalParameterService: ExternalParametersService,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params.tenantIdentifier) {
        this.accessContractService.getAllForTenant(params.tenantIdentifier).subscribe((value) => {
          this.accessContracts = value;

          this.externalParameterService.getUserExternalParameters().subscribe(parameters => {
            const accessContratId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
            if (this.accessContracts && this.accessContracts.findIndex(contract => contract.identifier === accessContratId)) {
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


        });
      }
    });
  }

  emitClose() {
    this.previewClose.emit();
  }

  downloadReport() {
    this.probativeValueService.export(this.probativeValue.id, this.accessContractId);
  }

}
