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
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {AccessContract} from 'projects/vitamui-library/src/public-api';
import {Observable, of} from 'rxjs';
import {catchError, filter, map, switchMap} from 'rxjs/operators';
import {diff, Option} from 'ui-frontend-common';
import {extend, isEmpty} from 'underscore';
import {AgencyService} from '../../../agency/agency.service';
import {AccessContractService} from '../../access-contract.service';

@Component({
  selector: 'app-access-contract-usage-and-services-tab',
  templateUrl: './access-contract-usage-and-services-tab.component.html',
  styleUrls: ['./access-contract-usage-and-services-tab.component.scss']
})
export class AccessContractUsageAndServicesTabComponent implements OnInit {

  @Input()
  set accessContract(accessContract: AccessContract) {
    this._accessContract = accessContract;

    if (!accessContract.originatingAgencies) {
      accessContract.originatingAgencies = [];
    }

    if (!accessContract.dataObjectVersion) {
      accessContract.dataObjectVersion = [];
    }

    this.originatingAgencySelect.setValue(accessContract.originatingAgencies);
    this.usageSelect.setValue(accessContract.dataObjectVersion);
    this.resetForm(this.accessContract);
  }

  get accessContract(): AccessContract {
    return this._accessContract;
  }

  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({emitEvent: false});
    } else if (this.form.disabled) {
      this.form.enable({emitEvent: false});
      this.form.get('identifier').disable({emitEvent: false});
    }
  }

  constructor(
    private formBuilder: FormBuilder,
    private accessContractService: AccessContractService,
    private agencyService: AgencyService) {
    this.form = this.formBuilder.group({
      everyOriginatingAgency: [true, Validators.required],
      originatingAgencies: [[]],
      everyDataObjectVersion: [true, Validators.required],
      dataObjectVersion: [[]]
    });

    this.agencyService.getAll().subscribe(agencies =>
      this.originatingAgencies = agencies.map(x => ({label: x.name, key: x.identifier}))
    );

    this.originatingAgencySelect.valueChanges.subscribe(
      x => {
        this.form.controls.originatingAgencies.setValue(x);
      }
    );

    this.form.controls.everyOriginatingAgency.valueChanges.subscribe(
      allAgencies => {
        if (allAgencies) {
          // remove required validator on originatingAgencySelect
          this.originatingAgencySelect.setValidators([]);
          this.originatingAgencySelect.updateValueAndValidity();
        } else {
          // add required validator on originatingAgencySelect
          this.originatingAgencySelect.setValidators(Validators.required);
          this.originatingAgencySelect.markAllAsTouched();
          this.originatingAgencySelect.updateValueAndValidity();
        }
      }
    );
    
    this.form.controls.everyDataObjectVersion.valueChanges.subscribe(
      allUsage => {
        if (allUsage) {
          // remove required validator on usageSelect
          this.usageSelect.setValidators([]);
          this.usageSelect.updateValueAndValidity();
        } else {
          // add required validator on usageSelect
          this.usageSelect.setValidators(Validators.required);
          this.usageSelect.markAllAsTouched();
          this.usageSelect.updateValueAndValidity();
        }
      }
    );

    this.usageSelect.valueChanges.subscribe(
      x => {
        this.form.controls.dataObjectVersion.setValue(x);
      }
    );
  }

  form: FormGroup;
  submited = false;

  originatingAgencySelect = new FormControl();
  usageSelect = new FormControl();

  originatingAgencies: Option[];

  // FIXME: Get list from common var ?
  usages: Option[] = [
    {key: 'BinaryMaster', label: 'Archives numériques originales', info: ''},
    {key: 'Dissemination', label: 'Copies de diffusion', info: ''},
    {key: 'Thumbnail', label: 'Vignettes', info: ''},
    {key: 'TextContent', label: 'Contenu textuel', info: ''},
    {key: 'PhysicalMaster', label: 'Archives physiques', info: ''}
  ];

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  // tslint:disable-next-line:variable-name
  private _accessContract: AccessContract;

  previousValue = (): AccessContract => {
    return this._accessContract;
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';

    this.updated.emit(!unchanged);

    return unchanged;
  }

  prepareSubmit(): Observable<AccessContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({id: this.previousValue().id, identifier: this.previousValue().identifier}, formData)),
      switchMap(
        (formData: { id: string, [key: string]: any }) => this.accessContractService.patch(formData).pipe(catchError(() => of(null)))));
  }

  onSubmit() {
    this.submited = true;
    // if (this.isInvalid()) { return; }
    this.prepareSubmit().subscribe(() => {
      this.accessContractService.get(this._accessContract.identifier).subscribe(
        response => {
          this.submited = false;
          this.accessContract = response;
        }
      );
    }, () => {
      this.submited = false;
    });
  }

  ngOnInit() {

  }

  resetForm(accessContract: AccessContract) {
    if (accessContract.everyOriginatingAgency) {
      // remove required validator on originatingAgencySelect
      this.originatingAgencySelect.setValidators([]);
    } else {
      // add required validator on originatingAgencySelect
      this.originatingAgencySelect.setValidators(Validators.required);
    }

    if (accessContract.everyDataObjectVersion) {
      // remove required validator on usageSelect
      this.usageSelect.setValidators([]);
    } else {
      // add required validator on usageSelect
      this.usageSelect.setValidators(Validators.required);
    }

    this.form.reset(accessContract, {emitEvent: false});
  }

  isInvalid(): boolean {
    return this.form.invalid || this.usageSelect.invalid || this.originatingAgencySelect.invalid;
  }

}
