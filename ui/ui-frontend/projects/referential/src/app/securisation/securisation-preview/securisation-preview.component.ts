import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {AccessContract, Event} from 'projects/vitamui-library/src/public-api';

import {AccessContractService} from '../../access-contract/access-contract.service';
import {SecurisationService} from '../securisation.service';

@Component({
  selector: 'app-securisation-preview',
  templateUrl: './securisation-preview.component.html',
  styleUrls: ['./securisation-preview.component.scss']
})
export class SecurisationPreviewComponent implements OnInit {

  @Input() securisation: Event;
  @Output() previewClose: EventEmitter<any> = new EventEmitter();

  accessContracts: AccessContract[];
  accessContractId: string;

  constructor(
    private securisationService: SecurisationService,
    private accessContractService: AccessContractService,
    private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params.tenantIdentifier) {
        this.accessContractService.getAllForTenant(params.tenantIdentifier).subscribe((value) => {
          this.accessContracts = value;
        });
      }
    });
  }

  emitClose() {
    this.previewClose.emit();
  }

  updateAccessContractId(event: any) {
    this.accessContractId = event.value;
  }

  downloadReport() {
    this.securisationService.download(this.securisation.id, this.accessContractId);
  }
}
