import { AfterViewChecked, ChangeDetectorRef, Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { tap } from 'rxjs/operators';
import { OjectUtils } from 'ui-frontend-common';
import { AccessionRegistersService } from '../accession-register.service';

@Component({
  selector: 'app-accession-register-advanced-search',
  templateUrl: './accession-register-advanced-search.component.html',
  styleUrls: ['./accession-register-advanced-search.component.scss'],
})
export class AccessionRegisterAdvancedSearchComponent implements OnInit, OnDestroy, AfterViewChecked {
  @Output() showAdvancedSearchPanel = new EventEmitter<boolean>();

  advancedSearchForm: FormGroup;
  acquisitionInformationsControl: FormControl;
  acquisitionInformations: string[] = [];
  isAdvancedFormChanged$: Observable<boolean>;
  globalSearchButtonEvent$: Observable<boolean>;
  globalResetEvent$: Observable<boolean>;
  valuesChangedSub: Subscription;
  resetSub: Subscription;

  constructor(
    private formBuilder: FormBuilder,
    private accessionRegistersService: AccessionRegistersService,
    private cdr: ChangeDetectorRef,
  ) {}

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
      values.transferReply !== 'all';

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
    });
  }

  ngOnDestroy(): void {
    this.valuesChangedSub.unsubscribe();
    this.resetSub?.unsubscribe();
  }
}
