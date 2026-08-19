/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { AfterViewChecked, ChangeDetectorRef, Component, EventEmitter, OnDestroy, OnInit, Output, inject } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { tap } from 'rxjs/operators';
import { OjectUtils, VitamuiMultiInputsModule, SelectComponent } from 'vitamui-library';
import { AccessionRegistersService } from '../accession-register.service';
import { MatRadioGroup, MatRadioButton } from '@angular/material/radio';
import { AsyncPipe } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-accession-register-advanced-search',
  templateUrl: './accession-register-advanced-search.component.html',
  styleUrls: ['./accession-register-advanced-search.component.scss'],
  imports: [ReactiveFormsModule, VitamuiMultiInputsModule, SelectComponent, MatRadioGroup, MatRadioButton, AsyncPipe, TranslatePipe],
})
export class AccessionRegisterAdvancedSearchComponent implements OnInit, OnDestroy, AfterViewChecked {
  private formBuilder = inject(FormBuilder);
  private accessionRegistersService = inject(AccessionRegistersService);
  private cdr = inject(ChangeDetectorRef);

  @Output() showAdvancedSearchPanel = new EventEmitter<boolean>();

  advancedSearchForm: FormGroup;
  acquisitionInformationsControl: FormControl;
  acquisitionInformations: string[] = [];
  isAdvancedFormChanged$: Observable<boolean>;
  globalSearchButtonEvent$: Observable<boolean>;
  globalResetEvent$: Observable<boolean>;
  valuesChangedSub: Subscription;
  resetSub: Subscription;

  ngAfterViewChecked(): void {
    this.cdr.detectChanges();
  }

  ngOnInit(): void {
    this.acquisitionInformations = this.accessionRegistersService.getAcquisitionInformations();
    this.acquisitionInformationsControl = new FormControl(this.acquisitionInformations);
    this.initForm();
    this.isAdvancedFormChanged$ = this.accessionRegistersService.isAdvancedFormChanged();
    this.globalSearchButtonEvent$ = this.accessionRegistersService.getGlobalSearchButtonEvent();
    this.valuesChangedSub = this.advancedSearchForm.valueChanges.subscribe((values) => {
      this.dataChanged(values);
      this.accessionRegistersService.setAdvancedSearchData(values);
    });
    this.resetSub = this.accessionRegistersService
      .isGlobalResetEvent()
      .pipe(
        tap((isReset) => {
          if (isReset) {
            this.advancedSearchForm.reset({
              acquisitionInformations: this.acquisitionInformations,
              elimination: 'all',
              transferReply: 'all',
              originatingAgencyReassignment: 'all',
            });
          }
        }),
      )
      .subscribe();
  }

  private dataChanged(values: any) {
    const haveChanged =
      OjectUtils.arrayNotUndefined(values.originatingAgencies) ||
      OjectUtils.arrayNotUndefined(values.archivalAgreements) ||
      OjectUtils.arrayNotUndefined(values.archivalProfiles) ||
      values.acquisitionInformations.length !== this.acquisitionInformations.length ||
      values.elimination !== 'all' ||
      values.transferReply !== 'all' ||
      values.originatingAgencyReassignment !== 'all';

    this.accessionRegistersService.setAdvancedFormHaveChanged(haveChanged);
    this.accessionRegistersService.setGlobalSearchButtonEvent(false);
  }

  private initForm() {
    this.advancedSearchForm = this.formBuilder.group({
      originatingAgencies: [[], []],
      archivalAgreements: [[], []],
      archivalProfiles: [[], []],
      acquisitionInformations: this.acquisitionInformationsControl,
      elimination: ['all', []],
      transferReply: ['all', []],
      originatingAgencyReassignment: ['all', []],
    });
  }

  ngOnDestroy(): void {
    this.valuesChangedSub.unsubscribe();
    this.resetSub?.unsubscribe();
  }
}
