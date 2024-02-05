/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import '@angular/localize/init';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import { AccessContract, ExternalParameters, ExternalParametersService } from 'ui-frontend-common';
import { ProbativeValueService } from '../probative-value.service';

@Component({
  selector: 'app-probative-value-preview',
  templateUrl: './probative-value-preview.component.html',
  styleUrls: ['./probative-value-preview.component.scss'],
})
export class ProbativeValuePreviewComponent implements OnInit, OnDestroy {
  @Input() probativeValue: any;
  @Output() previewClose: EventEmitter<any> = new EventEmitter();

  accessContracts: AccessContract[];
  hasAccessContract: boolean;

  accessContractSub: Subscription;
  errorMessageSub: Subscription;
  accessContract: string;

  constructor(
    private probativeValueService: ProbativeValueService,
    private externalParameterService: ExternalParametersService,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private translateService: TranslateService,
  ) {}

  findUserAccessContract() {
    this.accessContractSub = this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
      const accessConctractId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessConctractId) {
        this.accessContract = accessConctractId;
        this.hasAccessContract = true;
      } else {
        this.errorMessageSub = this.translateService
          .get('ARCHIVE_SEARCH.ACCESS_CONTRACT_NOT_FOUND')
          .pipe(
            map((message) => {
              this.snackBar.open(message, null, {
                panelClass: 'vitamui-snack-bar',
                duration: 10000,
              });
            }),
          )
          .subscribe();
      }
    });
  }

  ngOnDestroy() {
    this.accessContractSub.unsubscribe();
    if (this.errorMessageSub) {
      this.errorMessageSub.unsubscribe();
    }
  }

  ngOnInit() {
    this.route.params.subscribe((params) => {
      if (params.tenantIdentifier) {
        this.findUserAccessContract();
      }
    });
  }

  emitClose() {
    this.previewClose.emit();
  }

  downloadReport() {
    this.probativeValueService.export(this.probativeValue.id, this.accessContract);
  }
}
