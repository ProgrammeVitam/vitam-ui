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
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { AccessContract, Option, diff } from 'ui-frontend-common';
import { isEmpty } from 'underscore';
import { AgencyService } from '../../../agency/agency.service';
import { AccessContractService } from '../../access-contract.service';

@Component({
  selector: 'app-access-contract-usage-and-services-tab',
  templateUrl: './access-contract-usage-and-services-tab.component.html',
  styleUrls: ['./access-contract-usage-and-services-tab.component.scss'],
})
export class AccessContractUsageAndServicesTabComponent implements OnInit {
  private _accessContract: AccessContract;
  private _initialAccessContract: AccessContract;

  @Input()
  public set accessContract(accessContract: AccessContract) {
    this.initAccessContract(accessContract);
    this._initialAccessContract = this.deepClone(accessContract);
    this._accessContract = accessContract;

    this.resetForm(this._initialAccessContract);
  }

  public get accessContract(): AccessContract {
    return this._accessContract;
  }

  @Input() readOnly: boolean;
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;
  submitting = false;
  changed = false;
  originatingAgencies: Option[];

  // FIXME: Get list from common var ?
  usages: Option[] = [
    { key: 'BinaryMaster', label: 'Archives numériques originales', info: '' },
    { key: 'Dissemination', label: 'Copies de diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignettes', info: '' },
    { key: 'TextContent', label: 'Contenu textuel', info: '' },
    { key: 'PhysicalMaster', label: 'Archives physiques', info: '' },
  ];

  constructor(
    private formBuilder: FormBuilder,
    private accessContractService: AccessContractService,
    private agencyService: AgencyService
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadAgencies();
    this.initTogglingLogic(this.form.controls.everyOriginatingAgency);
    this.initTogglingLogic(this.form.controls.everyDataObjectVersion);
    this.form.valueChanges
      .pipe(
        map((value) => JSON.stringify(diff(value, this._initialAccessContract)) !== '{}'),
        tap({
          next: (value) => {
            this.changed = value;
          },
        })
      )
      .subscribe((value) => this.updated.emit(value));
  }

  public onSubmit() {
    this.submitting = true;
    this.prepareSubmit().subscribe(
      () => {
        this.accessContractService.get(this.accessContract.identifier).subscribe((response) => {
          this._initialAccessContract = this.deepClone(response);

          /**
           * Have to assign without overwriting "this.accessContract" reference because
           * don't have mecanism to update access contract list properly.
           *
           * TODO: Improve access contracts management by extracting life cycle into a specialized service.
           */
          Object.assign(this.accessContract, response);
        });
        this.submitting = false;
      },
      () => {
        this.submitting = false;
      },
      () => {
        this.submitting = false;
      }
    );
  }

  public prepareSubmit(): Observable<AccessContract> {
    const { id, identifier } = this._initialAccessContract;
    const data = this.form.getRawValue();
    const delta = diff(data, this._initialAccessContract); // If items in arrays are same but not in same ordre then the diff will detect a change.
    const dataToPatch = { id, identifier, ...delta };

    if (isEmpty(delta)) {return of(null);}

    return of(dataToPatch).pipe(
      switchMap((formData: { id: string; [key: string]: any }) =>
        this.accessContractService.patch(formData).pipe(catchError(() => of(null)))
      )
    );
  }

  public resetForm(accessContract: AccessContract) {
    this.form.reset(accessContract);
    this.form.controls.originatingAgencies.setValidators(accessContract.everyOriginatingAgency ? [] : Validators.required);
    this.form.controls.originatingAgencies.setValue(accessContract.originatingAgencies || [], { emitEvent: false });
    this.form.controls.dataObjectVersion.setValidators(accessContract.everyDataObjectVersion ? [] : Validators.required);
    this.form.controls.dataObjectVersion.setValue(accessContract.dataObjectVersion || [], { emitEvent: false });
  }

  private initForm(): void {
    this.form = this.formBuilder.group({
      everyOriginatingAgency: [true, [Validators.required]],
      originatingAgencies: [null, [Validators.required]],
      everyDataObjectVersion: [true, [Validators.required]],
      dataObjectVersion: [null, [Validators.required]],
    });
    if (this.readOnly && this.form.enabled) {
      this.form.disable({ emitEvent: false });
    } else if (this.form.disabled) {
      this.form.enable({ emitEvent: false });
      this.form.get('identifier').disable({ emitEvent: false });
    }
  }

  private initAccessContract(accessContract: AccessContract): AccessContract {
    accessContract.originatingAgencies = accessContract.originatingAgencies || [];
    accessContract.dataObjectVersion = accessContract.dataObjectVersion || [];

    return accessContract;
  }

  private loadAgencies = () => {
    this.agencyService
      .getAll()
      .subscribe((agencies) => (this.originatingAgencies = agencies.map((x) => ({ label: x.name, key: x.identifier }))));
  };

  private initTogglingLogic = (abstractControl: AbstractControl): void => {
    abstractControl.valueChanges.subscribe((value: boolean) => {
      abstractControl.setValidators(value ? [] : [Validators.required]);
      if (!value) {abstractControl.markAllAsTouched();}
      abstractControl.updateValueAndValidity({ emitEvent: false });
    });
  };

  private deepClone(object: any): any {
    return JSON.parse(JSON.stringify(object));
  }
}
