import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import { AccessContract } from 'projects/vitamui-library/src/public-api';

import { AccessContractService } from '../../access-contract/access-contract.service';
import { ActivatedRoute } from "@angular/router";
import { ProbativeValueService } from "../probative-value.service";

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

  constructor(private probativeValueService: ProbativeValueService, private accessContractService: AccessContractService, private route: ActivatedRoute) { }

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
    this.probativeValueService.export(this.probativeValue.id, this.accessContractId);
  }

}
