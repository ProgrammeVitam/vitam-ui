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
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import '@angular/localize/init';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AppRootComponent, Option } from 'ui-frontend-common';
import { DslQueryType } from '../../../../vitamui-library/src/lib/models/dsl-query-type.enum';
import { AccessContractService } from '../access-contract/access-contract.service';
import { VitamUISnackBar } from '../shared/vitamui-snack-bar';
import { AdminDslService } from './admin-dsl.service';

@Component({
  selector: 'app-admin-dsl',
  templateUrl: './admin-dsl.component.html',
  styleUrls: ['./admin-dsl.component.scss']
})
export class AdminDslComponent extends AppRootComponent implements OnInit {
  tenantId: number;

  form: FormGroup;

  accessContracts: Option[] = [];
  dslQueryTypeEnum = DslQueryType;
  dslQueryFormatErrorMessage = $localize`:dsl query format error message@@dslQueryFormatErrorMessage:Format de la requête invalide`;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private snackBar: VitamUISnackBar,
              private adminDslService: AdminDslService,
              private accessContractService: AccessContractService,
              private formBuilder: FormBuilder) {

    super(route);

    this.route.params.subscribe(params => {
      if (params.tenantIdentifier) {
        this.tenantId = params.tenantIdentifier;
        this.accessContractService.getAllForTenant('' + this.tenantId).subscribe(
          accessContracts => {
            this.accessContracts = accessContracts.map(accessContract => ({
              key: accessContract.identifier,
              label: accessContract.name
            }));
          }
        );
      }
    });

    this.form = this.formBuilder.group({
      dslQueryType: [null, Validators.required],
      id: null,
      accessContract: [null, Validators.required],
      dsl: [null, Validators.required],
      response: null
    });

  }

  search() {
    try {
      const searchObservable: Observable<any> = this.form.get('dslQueryType').value === this.dslQueryTypeEnum.ARCHIVE_UNIT ?
        this.adminDslService.getByDsl(this.form.value.id, JSON.parse(this.form.value.dsl), this.form.value.accessContract) :
        this.adminDslService.getUnitObjectsByDsl(this.form.value.id, JSON.parse(this.form.value.dsl), this.form.value.accessContract);

      searchObservable.subscribe(
        (response: any) => {
          if (response.httpCode === 400) {
            this.form.controls.response.setValue(response.description);
          } else {
            this.form.controls.response.setValue(JSON.stringify(response, null, 2));
          }
        }, (error: any) => {
          console.log(error);
        });
    } catch (syntaxError) {
      this.snackBar.open(this.dslQueryFormatErrorMessage, null, {
        panelClass: 'vitamui-snack-bar',
        duration: 1000
      });
    }
  }

  checkDsl() {
    const dsl = this.form.value.dsl;
    try {
      return dsl.length > 1 && !!JSON.parse(dsl);
    } catch (syntaxError) {
      this.snackBar.open(this.dslQueryFormatErrorMessage, null, {
        panelClass: 'vitamui-snack-bar',
        duration: 1000
      });
      return false;
    }
  }

  copyToClipbord(element: HTMLTextAreaElement) {
    element.select();
    document.execCommand('copy');
    element.setSelectionRange(0, 0);
  }

  clear() {
    this.form.controls.response.reset();
  }

  ngOnInit() {
  }

  changeTenant(tenantIdentifier: number) {
    this.tenantId = tenantIdentifier;
    this.router.navigate(['..', tenantIdentifier], { relativeTo: this.route });
  }

  /**
   * Check if the unit id is required according to the selected dsl query type
   */
  isUnitIdRequired(): boolean {
    return this.form.get('dslQueryType').value === this.dslQueryTypeEnum.TECHNICAL_OBJECT_GROUP;
  }

}
