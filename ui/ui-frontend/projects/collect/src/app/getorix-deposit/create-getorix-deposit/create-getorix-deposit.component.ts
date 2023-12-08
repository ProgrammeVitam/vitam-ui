/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { Component, HostListener, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDatepicker } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Moment } from 'moment';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService, BreadCrumbData, GlobalEventService, Logger, SidenavPage, StartupService } from 'ui-frontend-common';
import { DepositFormError, DepositOperationCategory } from '../core/model/deposit-operation-category.interface';
import { DepositStatus, GetorixDeposit } from '../core/model/getorix-deposit.interface';
import { GetorixDepositService } from '../getorix-deposit.service';

const OTHER = 'OTHER';
const SEARCH = 'SEARCH';
const DIAGNOSIS = 'DIAGNOSIS';
const ORIGINATING_AGENCY = 'originatingAgency';
const VERSATILE_SERVICE = 'versatileService';
@Component({
  selector: 'create-getorix-deposit',
  templateUrl: './create-getorix-deposit.component.html',
  styleUrls: ['./create-getorix-deposit.component.scss'],
})
export class CreateGetorixDepositComponent extends SidenavPage<any> implements OnInit, OnDestroy {
  ALPHA_NUMERIC_REGEX = /^[a-zA-ZÀ-ÖØ-öø-ÿ\d\-_\s]+$/i;
  NUMERIC_REGEX = /^[0-9]*$/;

  showConfirmLeaveGetorixDepisitCreationSuscription: Subscription = new Subscription();

  @ViewChild('confirmLeaveGetorixDepisitCreationDialog', { static: true })
  confirmLeaveGetorixDepisitCreationDialog: TemplateRef<CreateGetorixDepositComponent>;

  operationCategoryList: DepositOperationCategory[] = [
    {
      operationCategory: 'AGENCY_DETAILS',
      isError: false,
      target: 'agencyDetails',
      isSelected: false,
    },
    {
      operationCategory: 'SCIENTIFIC_OFFICER',
      isError: false,
      target: 'scientificOfficer',
      isSelected: false,
    },
    {
      operationCategory: 'OPERATION_TYPE_NAME',
      isError: false,
      target: 'operaionNameAndType',
      isSelected: false,
    },
    {
      operationCategory: 'OPERATION_NUMBER',
      isError: false,
      target: 'operationNumber',
      isSelected: false,
    },
    {
      operationCategory: 'PERSCRIPTION_NUMBER',
      isError: false,
      target: 'prescriptionOrderNumber',
      isSelected: false,
    },
    {
      operationCategory: 'LOCALISATION',
      isError: false,
      target: 'operationLocalisation',
      isSelected: false,
    },
    {
      operationCategory: 'PARTICULARITIES',
      isError: false,
      target: 'operationParticularities',
      isSelected: false,
    },
    {
      operationCategory: 'DATES',
      isError: false,
      target: 'operationDates',
      isSelected: false,
    },
    {
      operationCategory: 'DOCUMENT_DATES',
      isError: false,
      target: 'documentDates',
      isSelected: false,
    },
    {
      operationCategory: 'ARCHIVE_STATUS',
      isError: false,
      target: 'archiveStatus',
      isSelected: false,
    },
    {
      operationCategory: 'VOLUME_DETAILS',
      isError: false,
      target: 'volumeDetails',
      isSelected: false,
    },
    {
      operationCategory: 'FURNITURE',
      isError: false,
      target: 'furniture',
      isSelected: false,
    },
  ];
  depositFormError: DepositFormError[] = [
    {
      operationCategory: 'AGENCY_DETAILS',
      target: 'agencyDetails',
      message: 'ORGANISATION_NAME',
      inputName: 'originatingAgency',
      isValid: false,
    },
    {
      operationCategory: 'AGENCY_DETAILS',
      target: 'agencyDetails',
      message: 'VERSATILE_SERVICE',
      inputName: 'versatileService',
      isValid: false,
    },
    {
      operationCategory: 'SCIENTIFIC_OFFICER',
      target: 'scientificOfficer',
      message: 'OFFICER_FIRST_NAME',
      inputName: 'firstScientificOfficerFirstName',
      isValid: false,
    },
    {
      operationCategory: 'SCIENTIFIC_OFFICER',
      target: 'scientificOfficer',
      message: 'OFFICER_LAST_NAME',
      inputName: 'firstScientificOfficerLastName',
      isValid: false,
    },
    {
      operationCategory: 'OPERATION_TYPE_NAME',
      target: 'operaionNameAndType',
      message: 'OPRATION_NAME',
      inputName: 'operationName',
      isValid: false,
    },
    {
      operationCategory: 'OPERATION_TYPE_NAME',
      target: 'operaionNameAndType',
      message: 'OPERATION_TYPE',
      inputName: 'operationType',
      isValid: false,
    },
    {
      operationCategory: 'OPERATION_NUMBER',
      target: 'operationNumber',
      message: 'INTERN_ADMINISTRATOR_NUMBER',
      inputName: 'internalAdministratorNumber',
      isValid: false,
    },
    {
      operationCategory: 'OPERATION_NUMBER',
      target: 'operationNumber',
      message: 'NATIONAL_NUMBER',
      inputName: 'nationalNumber',
      isValid: false,
    },
    {
      operationCategory: 'PERSCRIPTION_NUMBER',
      target: 'prescriptionOrderNumber',
      message: 'PERSCRIPTION_NUMBER',
      inputName: 'prescriptionOrderNumber',
      isValid: false,
    },
    {
      operationCategory: 'LOCALISATION',
      target: 'operationLocalisation',
      message: 'DEPARTEMENT',
      inputName: 'archaeologistGetorixAddress.department',
      isValid: false,
    },
    {
      operationCategory: 'LOCALISATION',
      target: 'operationLocalisation',
      message: 'INSEE_NUMBER',
      inputName: 'archaeologistGetorixAddress.inseeNumber',
      isValid: false,
    },
    {
      operationCategory: 'LOCALISATION',
      target: 'operationLocalisation',
      message: 'COMMUNE',
      inputName: 'archaeologistGetorixAddress.commune',
      isValid: false,
    },
    {
      operationCategory: 'DATES',
      target: 'operationDates',
      message: 'OPERATION_START_DATE',
      inputName: 'operationStartDate',
      isValid: false,
    },
    {
      operationCategory: 'DATES',
      target: 'operationDates',
      message: 'OPERATION_END_DATE',
      inputName: 'operationEndDate',
      isValid: false,
    },
    {
      operationCategory: 'DOCUMENT_DATES',
      target: 'documentDates',
      message: 'DOCUMENT_START_DATE',
      inputName: 'documentStartDate',
      isValid: false,
    },
    {
      operationCategory: 'DOCUMENT_DATES',
      target: 'documentDates',
      message: 'DOCUMENT_END_DATE',
      inputName: 'documentEndDate',
      isValid: false,
    },
    {
      operationCategory: 'VOLUME_DETAILS',
      target: 'volumeDetails',
      message: 'ARCHIVE_VOLUME',
      inputName: 'archiveVolume',
      isValid: false,
    },
  ];

  operationType: string;
  tenantIdentifier: string;
  tenantIdentifierSubscription: Subscription;
  createDraftedDepositSubscription: Subscription;
  createInProgressDepositSubscription: Subscription;
  showForm = false;
  showInitialForm = true;
  versatileServiceDisabled = true;
  showSecondScientifOfficer = false;
  showOfficerAction = true;
  operationTypeDisabled = true;
  showInputErrorFrame = false;
  getorixDepositform: FormGroup;
  formChanged = false;
  isSuccessCreation = false;
  dataBreadcrumb: BreadCrumbData[];
  getorixDepositCreated: GetorixDeposit;
  pending = false;
  operationId: string;

  previousDepositForm = {
    originatingAgency: '',
    versatileService: '',
    firstScientificOfficerFirstName: '',
    firstScientificOfficerLastName: '',
    secondScientificOfficerFirstName: '',
    secondScientificOfficerLastName: '',
    operationName: '',
    operationType: '',
    internalAdministratorNumber: '',
    nationalNumber: '',
    prescriptionOrderNumber: '',
    operationParticularities: '',
    operationStartDate: '',
    operationEndDate: '',
    documentStartDate: '',
    documentEndDate: '',
    saveLastCondition: '',
    materialStatus: '',
    archiveVolume: '',
    furniture: '',
    furnitureComment: '',
    archaeologistGetorixAddress: {
      placeName: '',
      department: '',
      inseeNumber: '',
      commune: '',
    },
  };

  constructor(
    private getorixDepositService: GetorixDepositService,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private router: Router,
    public dialog: MatDialog,
    private authService: AuthService,
    globalEventService: GlobalEventService,
    private translateService: TranslateService,
    private loggerService: Logger,
    private startupService: StartupService
  ) {
    super(route, globalEventService);
    this.initExportForm();

    this.getorixDepositform.get('operationType').valueChanges.subscribe((data) => {
      this.operationTypeDisabled = data !== OTHER;
    });
  }

  ngOnInit() {
    this.pending = false;
    this.isSuccessCreation = false;
    this.showForm = false;
    this.showInitialForm = true;

    this.scrollToTop();
    this.tenantIdentifierSubscription = this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
      if (this.tenantIdentifier) {
        this.dataBreadcrumb = [
          {
            redirectUrl: this.router.url.replace('/create', ''),
            label: this.translateService.instant('GETORIX_DEPOSIT.BREAD_CRUMB.ARCHIVAL_SPACE'),
          },
          {
            label: this.translateService.instant('GETORIX_DEPOSIT.BREAD_CRUMB.NEW_PROJECT'),
            redirectUrl: this.router.url,
            isGetorix: true,
          },
          { label: this.translateService.instant('GETORIX_DEPOSIT.BREAD_CRUMB.UPLOAD_ARCHIVES') },
        ];
      }
    });

    this.detectChanges();

    this.operationId = this.route.snapshot.queryParamMap.get('operationId');

    if (this.operationId) {
      this.showInitialForm = false;
      this.showForm = true;
      this.getorixDepositService.getGetorixDepositById(this.operationId).subscribe(
        (data) => {
          this.showInitialForm = false;
          this.showForm = true;
          this.isSuccessCreation = false;
          this.getorixDepositform.patchValue(data);
          if (data.operationType !== SEARCH && data.operationType !== DIAGNOSIS) {
            this.getorixDepositform.patchValue({ operationTypeValue: data.operationType });
            this.getorixDepositform.patchValue({ operationType: OTHER });
            this.operationTypeDisabled = false;
          }
        },
        (error) => {
          this.loggerService.error('error while searching for this operation', error);
          window.location.href = this.router.url.replace('?operationId=', '').replace(this.operationId, '');
        }
      );
    }
  }

  returnToMainPage() {
    this.router.navigate(['/getorix-deposit/tenant/', this.tenantIdentifier]);
  }

  startDepositCreation() {
    this.scrollToTop();
    this.showForm = true;
    this.showInitialForm = false;
  }

  checkInputValidation(formControlAttribut: string) {
    if (this.getorixDepositform.get(formControlAttribut).invalid || this.getorixDepositform.get(formControlAttribut).pending) {
      this.depositFormError.find((deposit) => deposit.inputName == formControlAttribut).isValid = false;
      this.operationCategoryList.find(
        (category) => category.target == this.depositFormError.find((deposit) => deposit.inputName == formControlAttribut).target
      ).isError = true;
    }
    if (this.getorixDepositform.get(formControlAttribut).valid) {
      this.depositFormError.find((deposit) => deposit.inputName == formControlAttribut).isValid = true;
      this.operationCategoryList.find(
        (category) => category.target == this.depositFormError.find((deposit) => deposit.inputName == formControlAttribut).target
      ).isError = false;
    }
  }

  validateCreation() {
    this.pending = true;
    this.depositFormError.map((element) => this.checkInputValidation(element.inputName));

    if (this.versatileServiceDisabled) {
      this.depositFormError.find((deposit) => deposit.inputName == 'versatileService').isValid = true;
      if (this.getorixDepositform.get(ORIGINATING_AGENCY).valid) {
        this.operationCategoryList.find((category) => category.target == 'agencyDetails').isError = false;
      }
    }
    if (this.operationTypeDisabled && this.operationType != null) {
      this.depositFormError.find((deposit) => deposit.inputName == 'operationType').isValid = true;

      if (this.getorixDepositform.get('operationName').valid) {
        this.operationCategoryList.find((category) => category.target == 'operaionNameAndType').isError = false;
      }
    }
    this.showInputErrorFrame = this.depositFormError.filter((element) => element.isValid == false).length > 0;

    if (this.showInputErrorFrame) {
      this.scrollToTop();
      this.showForm = true;
      this.pending = false;
    } else {
      let getorixDeposit: GetorixDeposit = this.getorixDepositform.getRawValue();
      getorixDeposit.tenantIdentifier = +this.tenantIdentifier;
      getorixDeposit.depositStatus = DepositStatus.IN_PROGRESS;
      getorixDeposit.userId = this.authService.user.id;
      if (
        this.getorixDepositform.get(VERSATILE_SERVICE).value === '' ||
        (this.getorixDepositform.get(VERSATILE_SERVICE).value !== '' &&
          this.getorixDepositform.get(VERSATILE_SERVICE).value !== this.getorixDepositform.get(ORIGINATING_AGENCY).value)
      ) {
        getorixDeposit.versatileService = this.getorixDepositform.get(ORIGINATING_AGENCY).value;
        if (!this.versatileServiceDisabled) {
          getorixDeposit.versatileService = this.getorixDepositform.get(VERSATILE_SERVICE).value;
        }
      }

      if (this.getorixDepositform.get('operationType').value === OTHER) {
        getorixDeposit.operationType = this.getorixDepositform.get('operationTypeValue').value;
      }
      if (this.operationId) {
        getorixDeposit.id = this.operationId;
        this.getorixDepositService.updateGetorixDeposit(this.removeEmptyValue(getorixDeposit)).subscribe(() => {
          this.pending = false;
          window.location.href =
            this.startupService.getCollectUrl() +
            '/getorix-deposit/tenant/' +
            this.tenantIdentifier +
            '/create/upload-object/' +
            this.operationId;
        });
      } else {
        this.createInProgressDepositSubscription = this.getorixDepositService
          .createGetorixDeposit(this.removeEmptyValue(getorixDeposit))
          .subscribe((data) => {
            this.getorixDepositCreated = data;
            this.getorixDepositform.reset();
            this.depositFormError.map((deposit) => (deposit.isValid = false));
            this.showInputErrorFrame = false;
            this.formChanged = false;
            this.showSecondScientifOfficer = false;
            this.detectChanges();
            this.scrollToTop();
            this.pending = false;
            this.showForm = false;
            this.showInitialForm = false;
            this.isSuccessCreation = true;
          });
      }
    }
  }

  checkVersatileServiceBoxChange() {
    this.versatileServiceDisabled = !this.versatileServiceDisabled;
  }

  addNewScientificOfficer() {
    this.showSecondScientifOfficer = true;
    this.showOfficerAction = false;
  }
  deleteSecondScientificOfficer() {
    this.showSecondScientifOfficer = false;
    this.showOfficerAction = true;
  }

  ngOnDestroy() {
    this.tenantIdentifierSubscription?.unsubscribe();
    this.createDraftedDepositSubscription?.unsubscribe();
    this.createInProgressDepositSubscription?.unsubscribe();
  }

  setOperationStartDateMonthAndYear(normalizedMonthAndYear: Date, datepicker: MatDatepicker<Moment>) {
    this.getorixDepositform.patchValue({ operationStartDate: normalizedMonthAndYear });
    datepicker.close();
  }

  setOperationEndDateMonthAndYear(normalizedMonthAndYear: Date, datepicker: MatDatepicker<Moment>) {
    this.getorixDepositform.patchValue({ operationEndDate: normalizedMonthAndYear });

    datepicker.close();
  }

  setDocumentStartDateYear(normalizedYear: Date, datepicker: MatDatepicker<Moment>) {
    this.getorixDepositform.patchValue({ documentStartDate: normalizedYear });

    datepicker.close();
  }

  setDocumentEndDateYear(normalizedYear: Date, datepicker: MatDatepicker<Moment>) {
    this.getorixDepositform.patchValue({ documentEndDate: normalizedYear });

    datepicker.close();
  }

  scrollToElement(htmlElementId: string) {
    this.operationCategoryList.map((element) => (element.isSelected = false));
    if (htmlElementId === 'furniture') {
      const element = document.getElementById('volumeDetails');
      element.scrollIntoView({ behavior: 'auto', block: 'center' });
    } else {
      const element = document.getElementById(htmlElementId);
      element.scrollIntoView({ behavior: 'auto', block: 'center' });
    }

    if (this.operationCategoryList.find((category) => category.target == htmlElementId)) {
      this.operationCategoryList.find((category) => category.target == htmlElementId).isSelected = true;
    }
  }

  exitWithoutSave() {
    this.dialog.closeAll();
    this.initExportForm();
    this.operationCategoryList.map((element) => {
      element.isSelected = false;
      element.isError = false;
    });
    this.depositFormError.map((deposit) => (deposit.isValid = false));
    this.showInputErrorFrame = false;
    this.showForm = false;
    this.showInitialForm = true;
    this.formChanged = false;
    this.detectChanges();
  }

  returnToGetorixInitialPage() {
    if (this.formChanged) {
      const dialogToOpen = this.confirmLeaveGetorixDepisitCreationDialog;
      const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });

      this.showConfirmLeaveGetorixDepisitCreationSuscription = dialogRef
        .afterClosed()
        .pipe(filter((result) => !!result))
        .subscribe(() => {
          let getorixDeposit: GetorixDeposit = this.getorixDepositform.getRawValue();
          getorixDeposit.tenantIdentifier = +this.tenantIdentifier;
          getorixDeposit.depositStatus = DepositStatus.DRAFT;
          getorixDeposit.userId = this.authService.user.id;
          if (
            this.getorixDepositform.get(VERSATILE_SERVICE).value === '' ||
            (this.getorixDepositform.get(VERSATILE_SERVICE).value !== '' &&
              this.getorixDepositform.get(VERSATILE_SERVICE).value !== this.getorixDepositform.get(ORIGINATING_AGENCY).value)
          ) {
            getorixDeposit.versatileService = this.getorixDepositform.get(ORIGINATING_AGENCY).value;
          }

          if (this.getorixDepositform.get('operationType').value === OTHER) {
            getorixDeposit.operationType = this.getorixDepositform.get('operationTypeValue').value;
          }

          this.createDraftedDepositSubscription = this.getorixDepositService
            .createGetorixDeposit(this.removeEmptyValue(getorixDeposit))
            .subscribe(() => {
              this.getorixDepositform.reset();
              this.depositFormError.map((deposit) => (deposit.isValid = false));
              this.showInputErrorFrame = false;
              this.showForm = false;
              this.showInitialForm = true;
              this.formChanged = false;
              this.showSecondScientifOfficer = false;
              this.detectChanges();
            });
        });
    } else {
      this.exitWithoutSave();
    }
  }

  removeEmptyValue(obj: any): any {
    Object.keys(obj).forEach((key) => {
      if (obj[key] && typeof obj[key] === 'object') {
        this.removeEmptyValue(obj[key]);
      } else if (obj[key] === '') {
        delete obj[key];
      }
    });
    return obj;
  }

  @HostListener('window:scroll', ['$event'])
  onMouseMove(divIdentifier: string) {
    if (this.operationCategoryList.find((element) => element.target == divIdentifier)) {
      this.operationCategoryList.map((element) => (element.isSelected = false));
      this.operationCategoryList.find((element) => element.target == divIdentifier).isSelected = true;
    }
  }

  detectChanges() {
    this.getorixDepositform.valueChanges.subscribe((response) => {
      this.formChanged = JSON.stringify(response) !== JSON.stringify(this.previousDepositForm);
    });
  }

  private scrollToTop() {
    const c = document.documentElement.scrollTop || document.body.scrollTop;
    if (c > 0) {
      window.requestAnimationFrame(this.scrollToTop);
      window.scrollTo(0, c - c / 8);
    }
  }

  private initExportForm() {
    this.getorixDepositform = this.formBuilder.group({
      originatingAgency: ['', [Validators.required, Validators.minLength(2), Validators.pattern(this.ALPHA_NUMERIC_REGEX)]],
      versatileService: ['', [Validators.required, Validators.minLength(2), Validators.pattern(this.ALPHA_NUMERIC_REGEX)]],
      firstScientificOfficerFirstName: ['', [Validators.required, Validators.minLength(1), Validators.pattern(this.ALPHA_NUMERIC_REGEX)]],
      firstScientificOfficerLastName: ['', [Validators.required, Validators.minLength(1), Validators.pattern(this.ALPHA_NUMERIC_REGEX)]],
      secondScientificOfficerFirstName: [''],
      secondScientificOfficerLastName: [''],
      operationName: ['', [Validators.required, Validators.minLength(3), Validators.pattern(this.ALPHA_NUMERIC_REGEX)]],
      operationType: ['', [Validators.required, Validators.minLength(3), Validators.pattern(this.ALPHA_NUMERIC_REGEX)]],
      operationTypeValue: ['', [Validators.required, Validators.minLength(3), Validators.pattern(this.ALPHA_NUMERIC_REGEX)]],
      internalAdministratorNumber: ['', [Validators.required, Validators.pattern(this.ALPHA_NUMERIC_REGEX), Validators.minLength(1)]],
      nationalNumber: ['', [Validators.required, Validators.pattern(this.ALPHA_NUMERIC_REGEX), Validators.minLength(1)]],
      prescriptionOrderNumber: ['', [Validators.required, Validators.pattern(this.ALPHA_NUMERIC_REGEX), Validators.minLength(1)]],
      operationParticularities: [''],
      operationStartDate: ['', Validators.required],
      operationEndDate: ['', Validators.required],
      documentStartDate: ['', Validators.required],
      documentEndDate: ['', Validators.required],
      saveLastCondition: [''],
      materialStatus: [''],
      archiveVolume: ['', [Validators.required, Validators.pattern(this.NUMERIC_REGEX), Validators.minLength(1)]],
      furniture: [false],
      furnitureComment: [''],
      archaeologistGetorixAddress: this.formBuilder.group({
        placeName: [''],
        department: ['', [Validators.required, Validators.minLength(2), Validators.pattern(this.ALPHA_NUMERIC_REGEX)]],
        inseeNumber: ['', [Validators.required, Validators.minLength(1), Validators.pattern(this.ALPHA_NUMERIC_REGEX)]],
        commune: ['', [Validators.required, Validators.minLength(1), Validators.pattern(this.ALPHA_NUMERIC_REGEX)]],
      }),
    });
  }

  // success page

  goToUploadOperationObjects() {
    if (this.getorixDepositCreated) {
      window.location.href =
        this.startupService.getCollectUrl() +
        '/getorix-deposit/tenant/' +
        this.tenantIdentifier +
        '/create/upload-object/' +
        this.getorixDepositCreated.id;
    }
  }

  showDetails(item: string) {
    this.openPanel(item);
  }
}
