import {Component, EventEmitter, Input, OnInit, Output, TemplateRef, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatTabGroup} from '@angular/material/tabs';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {combineLatest, Observable} from 'rxjs';
import {map, mergeMap} from 'rxjs/operators';
import {
  DEFAULT_PAGE_SIZE,
  Direction,
  LegalStatus,
  PageRequest,
  PaginatedResponse,
  Project,
  Transaction,
  TransactionStatus
} from 'ui-frontend-common';
import {ProjectsApiService} from '../../core/api/project-api.service';
import {ProjectsService} from '../projects.service';

@Component({
  selector: 'app-project-preview',
  templateUrl: './project-preview.component.html',
  styleUrls: ['./project-preview.component.scss']
})
export class ProjectPreviewComponent implements OnInit {

  @Output()
  backToNormalLateralPanel: EventEmitter<any> = new EventEmitter();
  @Output()
  previewClose: EventEmitter<any> = new EventEmitter();
  @Output()
  showExtendedLateralPanel: EventEmitter<any> = new EventEmitter();

  @ViewChild(MatTabGroup) tabGroup: MatTabGroup;

  form: FormGroup;

  project: Project;

  acquisitionInformationsList: string[];
  legalStatusList: LegalStatus[] = [];

  @ViewChild('confirmEditProject', {static: true}) confirmEditProject: TemplateRef<ProjectPreviewComponent>;


  @Input()
  get projectId(): string {
    return this._projectId;
  }

  set projectId(value: string) {
    this.selectedProject$ = this.projectService.getProjectById(value);
    this.selectedTabIndex = 0;
  }

  private _projectId: string;
  private tenantIdentifier: string;

  updateStarted = false;
  isPanelextended = false;
  selectedTabIndex = 0;
  selectedProject$ = new Observable<Project>();
  dialogRefToClose: MatDialogRef<ProjectPreviewComponent>;
  selectedValue = 'NO';

  constructor(private formBuilder: FormBuilder, private projectService: ProjectsService,
              private projectApiService: ProjectsApiService,
              private route: ActivatedRoute, private router: Router,
              public dialog: MatDialog,
              private translationService: TranslateService,
              private snackBar: MatSnackBar,
  ) {
  }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.selectedProject$.subscribe(project => {
      this.project = project;
    })
    this.legalStatusList = this.projectService.getLegalStatusList();
    this.acquisitionInformationsList = this.projectService.getAcquisitionInformationsList();


  }

  searchArchiveUnitsByProject() {

    this.router.navigate(['collect/tenant/' + this.tenantIdentifier + '/units', this.project.id], {
      queryParams: {projectName: this.project.messageIdentifier},
    });

  }

  emitClose() {
    this.isPanelextended = false;
    this.previewClose.emit();
    this.backToNormalLateralPanel.emit();
    this.selectedTabIndex = 0;
  }

  showNormalPanel() {
    this.isPanelextended = false;
    this.backToNormalLateralPanel.emit();
    this.updateStarted = false;
  }

  showExtendedPanel() {
    this.isPanelextended = true;
    this.showExtendedLateralPanel.emit();
  }

  showEditProject() {
    this.updateStarted = true;
    this.showExtendedPanel();
    this.form = this.formBuilder.group({
      messageIdentifier: [null, [Validators.required]],
      id: [null],
      comment: [],
      originatingAgencyIdentifier: [null, [Validators.required]],
      submissionAgencyIdentifier: [],
      archivalAgencyIdentifier: [null, Validators.required],
      transferringAgencyIdentifier: [null, Validators.required],
      archivalAgreement: [null, Validators.required],
      archiveProfile: [null],
      acquisitionInformation: [null],
      legalStatus: [null]
    });
    this.initFormForEdit()

  }

  initFormForEdit() {
    this.form.get('messageIdentifier').setValue(this.project.messageIdentifier);
    this.form.get('comment').setValue(this.project.comment);
    this.form.get('originatingAgencyIdentifier').setValue(this.project.originatingAgencyIdentifier);
    this.form.get('submissionAgencyIdentifier').setValue(this.project.submissionAgencyIdentifier);


    this.form.get('archivalAgencyIdentifier').setValue(this.project.archivalAgencyIdentifier);
    this.form.get('transferringAgencyIdentifier').setValue(this.project.transferringAgencyIdentifier);
    this.form.get('archivalAgreement').setValue(this.project.archivalAgreement);
    this.form.get('archiveProfile').setValue(this.project.archiveProfile);
    this.form.get('acquisitionInformation').setValue(this.project.acquisitionInformation);
    this.form.get('legalStatus').setValue(this.project.legalStatus);
  }

  launchUpdate() {

    const dialogToOpen = this.confirmEditProject;
    this.dialogRefToClose = this.dialog.open(dialogToOpen, {panelClass: 'vitamui-dialog'});

  }

  mapProjectInternalFields(projectToUpdate: Project) {
    projectToUpdate.id = this.project.id;
    projectToUpdate.createdOn = this.project.createdOn;
    projectToUpdate.unitUp = this.project.unitUp;
    projectToUpdate.status = this.project.status;
  }


  fillTransactionFromProject(transaction: Transaction) {
    transaction.archivalAgreement = this.project.archivalAgreement;
    transaction.messageIdentifier = this.project.messageIdentifier;
    transaction.archivalAgencyIdentifier = this.project.archivalAgencyIdentifier;
    transaction.transferringAgencyIdentifier = this.project.transferringAgencyIdentifier;
    transaction.originatingAgencyIdentifier = this.project.originatingAgencyIdentifier;
    transaction.submissionAgencyIdentifier = this.project.submissionAgencyIdentifier;
    transaction.archiveProfile = this.project.archiveProfile;
  }

  onConfirm() {
    const projectToUpdate = {
      ...this.form.value,
      name: this.form.value.messageIdentifier,
    };
    this.mapProjectInternalFields(projectToUpdate);

    const updateProjectOperation$ = this.projectService.updateProject(projectToUpdate);
    const previousProject = this.project;
    this.project = null;
    if (this.selectedValue !== 'NO') {
      updateProjectOperation$.pipe(mergeMap((project): Observable<PaginatedResponse<Transaction>> => {
          const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, 'id', Direction.ASCENDANT);
          this.dialogRefToClose.close(true);
          this.project = project;
          this.updateStarted = false;

          return this.projectApiService.getTransactionsByProjectId(pageRequest, projectToUpdate.id);
        }),
        map(paginated => paginated.values),
        mergeMap((transactions: Transaction[]) => {
          const updateTransactionOperation$: Observable<Transaction>[] = [];
          const transactionsKO: Transaction[] = [];
          transactions.forEach(transaction => {
            if (transaction.status === TransactionStatus.OPEN) {
              this.fillTransactionFromProject(transaction);
              updateTransactionOperation$.push(this.projectApiService.updateTransaction(transaction));
            } else if (transaction.status === TransactionStatus.KO) {
              transactionsKO.push(transaction)
            }
          });
          return combineLatest(updateTransactionOperation$).pipe(map(() => transactionsKO));
        })).subscribe((transactionsKO: Transaction[]) => {

        let transactionMessage = this.translationService.instant('COLLECT.UPDATE_PROJECT.TERMINATED');
        if (transactionsKO.length > 0) {
          transactionMessage += ' ' + this.translationService.instant('COLLECT.UPDATE_PROJECT.TRANSACTIONS_KO')
        }
        this.snackBar.open(transactionMessage, null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
        });

      }, () => {
        this.project = previousProject;
      });
    } else {
      updateProjectOperation$.subscribe(
        () => {
          this.snackBar.open(this.translationService.instant('COLLECT.UPDATE_PROJECT.TERMINATED'), null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
          });
          this.dialogRefToClose.close(true);
          this.updateStarted = false;
          this.selectedProject$ = updateProjectOperation$;
        }
        , () => {
          this.project = previousProject;
        });
    }


  }

  onClose() {
    this.dialogRefToClose.close(true);
  }


}
