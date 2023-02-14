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

import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of, Subscription } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { diff, Option, Unit } from 'ui-frontend-common';
import { extend, isEmpty } from 'underscore';
import { ArchiveCollectService } from '../../archive-collect.service';

@Component({
  selector: 'app-archive-unit-information-tab',
  templateUrl: './archive-unit-information-tab.component.html',
  styleUrls: [ './archive-unit-information-tab.component.css' ],
})
export class ArchiveUnitInformationTabComponent implements OnInit, OnChanges, OnDestroy {
  @Input()
  archiveUnit: Unit;
  @Input()
  accessContract: string;
  uaPath$: Observable<{ fullPath: string; resumePath: string }>;

  @Input()
  updateStarted = false;
  form: FormGroup;
  unsetAction: string[] = [];
  hasTitle = false;
  hasFrTitle = false;
  hasEnTitle = false;

  previousValue: {
    title: string;
    description: string;
    descriptionLevel: string;
    startDate: Date;
    endDate: Date;
  };
  hasDescription = false;
  hasFrDescription = false;
  hasEnDescription = false;
  hasNoDescription = false;
  updateFormSub: Subscription;
  @Output()
  showNormalPanel = new EventEmitter<any>();
  @ViewChild('updateArchiveUnitDescMetadataAlerteMessageDialog', { static: true })
  updateArchiveUnitDescMetadataAlerteMessageDialog: TemplateRef<ArchiveUnitInformationTabComponent>;

  @ViewChild('updateArchiveUnitDescMetadataAlerteFormCancelDialog', { static: true })
  updateArchiveUnitDescMetadataAlerteFormCancelDialog: TemplateRef<ArchiveUnitInformationTabComponent>;
  updateArchiveUnitDescMetadataAlerteFormCancelDialogSubscription: Subscription;

  fullPath = false;

  constructor(
    private archiveService: ArchiveCollectService,
    private formBuilder: FormBuilder,
    private dialog: MatDialog,
    private translateService: TranslateService
  ) {
  }

  descriptionLevels: Option[] = [
    { key: 'Item', label: this.translateService.instant('UNIT_UPDATE.ITEM') },
    { key: 'File', label: this.translateService.instant('UNIT_UPDATE.FILE') },
    { key: 'SubGrp', label: this.translateService.instant('UNIT_UPDATE.SUBGRP') },
    { key: 'RecordGrp', label: this.translateService.instant('UNIT_UPDATE.RECORDGRP') },
    { key: 'Subseries', label: this.translateService.instant('UNIT_UPDATE.SUBSERIES') },
    { key: 'Series', label: this.translateService.instant('UNIT_UPDATE.SERIES') },
    { key: 'Collection', label: this.translateService.instant('UNIT_UPDATE.COLLECTION') },
    { key: 'Class', label: this.translateService.instant('UNIT_UPDATE.CLASS') },
    { key: 'Subfonds', label: this.translateService.instant('UNIT_UPDATE.SUBFONDS') },
    { key: 'Fonds', label: this.translateService.instant('UNIT_UPDATE.FONDS') },
    { key: 'OtherLevel', label: this.translateService.instant('UNIT_UPDATE.OTHERLEVEL') },
  ];

  ngOnInit() {
    this.initTitleAndDescriptionsFlagValues(this.archiveUnit);
    // TODO : Créer Web service de création du chemin d'archive
    // this.uaPath$ = this.archiveService.buildArchiveUnitPath(this.archiveUnit, this.accessContract);

    this.form = this.formBuilder.group({
      title: [ null, [ Validators.required ] ],
      description: [ null ],
      descriptionLevel: [ null, [ Validators.required ] ],
      startDate: [ this.archiveUnit.StartDate ],
      endDate: [ this.archiveUnit.EndDate ],
    });

    this.previousValue = {
      title: this.getAuTitle(this.archiveUnit),
      description: this.getAuDescription(this.archiveUnit),
      descriptionLevel: this.archiveUnit.DescriptionLevel,
      startDate: this.archiveUnit.StartDate,
      endDate: this.archiveUnit.EndDate,
    };

    this.form.get('startDate').valueChanges.subscribe(() => {
      if (this.unsetAction.length > 0) {
        this.unsetAction = this.unsetAction.filter((el) => el !== 'StartDate');
      }
    });

    this.form.get('endDate').valueChanges.subscribe(() => {
      if (this.unsetAction.length > 0) {
        this.unsetAction = this.unsetAction.filter((el) => el !== 'EndDate');
      }
    });

    this.form.get('description').valueChanges.subscribe((desc) => {
      this.cleanUnsetDescription('Description');
      this.cleanUnsetDescription('Description_.fr');
      this.cleanUnsetDescription('Description_.en');
      this.cleanUnsetDescription('Description_');

      if (desc !== undefined && desc !== null && desc.length === 0 && this.hasDescription && !this.unsetAction.includes('Description')) {
        this.unsetAction.push('Description');
      }

      if (
        desc !== undefined &&
        desc !== null &&
        desc.length === 0 &&
        this.hasFrDescription &&
        !this.unsetAction.includes('Description_.fr')
      ) {
        this.unsetAction.push('Description_.fr');
      }
      if (
        desc !== undefined &&
        desc !== null &&
        desc.length === 0 &&
        this.hasEnDescription &&
        !this.unsetAction.includes('Description_.en')
      ) {
        this.unsetAction.push('Description_.en');
      }
    });
  }

  cleanUnsetDescription(key: string) {
    const index = this.unsetAction.indexOf(key, 0);
    if (index > -1) {
      this.unsetAction.splice(index, 1);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.updateStarted && changes.updateStarted.currentValue) {
      this.previousValue = {
        title: this.getAuTitle(this.archiveUnit),
        description: this.getAuDescription(this.archiveUnit),
        descriptionLevel: this.archiveUnit.DescriptionLevel,
        startDate: this.archiveUnit.StartDate,
        endDate: this.archiveUnit.EndDate,
      };
      this.form.get('title').setValue(this.getAuTitle(this.archiveUnit));
      this.form.get('description').setValue(this.getAuDescription(this.archiveUnit));
      this.form.get('descriptionLevel').setValue(this.archiveUnit.DescriptionLevel);
      this.form.get('startDate').setValue(this.previousValue.startDate);
      this.form.get('endDate').setValue(this.previousValue.endDate);
      this.previousValue = this.form.value;
      this.initListenersOnFormsValuesChanges();
    }

    if (changes.archiveUnit?.currentValue['#id']) {
      this.initTitleAndDescriptionsFlagValues(changes.archiveUnit.currentValue);
      // TODO : Créer Web service de création du chemin d'archive
      // this.uaPath$ = this.archiveService.buildArchiveUnitPath(this.archiveUnit, this.accessContract);
      this.form?.reset();
      this.previousValue = {
        title: this.getAuTitle(changes.archiveUnit.currentValue),
        description: this.getAuDescription(changes.archiveUnit.currentValue),
        descriptionLevel: changes.archiveUnit.currentValue.DescriptionLevel,
        startDate: changes.archiveUnit.currentValue.StartDate,
        endDate: changes.archiveUnit.currentValue.EndDate,
      };
    }
    this.fullPath = false;
  }

  initTitleAndDescriptionsFlagValues(archiveUnit: Unit) {
    if (archiveUnit?.Title) {
      this.hasTitle = true;
    } else if (archiveUnit?.Title_?.fr) {
      this.hasFrTitle = !this.hasTitle;
    } else {
      this.hasEnTitle = !this.hasFrTitle;
    }

    if (archiveUnit.Description == undefined) {
      this.hasNoDescription = true;
    }

    if (archiveUnit?.Description) {
      this.hasDescription = true;
    } else if (archiveUnit?.Description_?.fr) {
      this.hasFrDescription = !this.hasDescription;
    } else if (archiveUnit?.Description_?.en) {
      this.hasEnDescription = !this.hasFrDescription;
    }
  }

  updateMetadataDesc() {
    this.previousValue = {
      title: this.getAuTitle(this.archiveUnit),
      description: this.getAuDescription(this.archiveUnit),
      descriptionLevel: this.archiveUnit.DescriptionLevel,
      startDate: this.archiveUnit.StartDate,
      endDate: this.archiveUnit.EndDate,
    };
    this.form.get('title').setValue(this.getAuTitle(this.archiveUnit));
    this.form.get('description').setValue(this.getAuDescription(this.archiveUnit));
    this.form.get('descriptionLevel').setValue(this.archiveUnit.DescriptionLevel);
    this.form.get('startDate').setValue(this.previousValue.startDate);
    this.form.get('endDate').setValue(this.previousValue.endDate);
    this.previousValue = this.form.value;
    this.initListenersOnFormsValuesChanges();
  }

  private initListenersOnFormsValuesChanges() {
    this.updateFormSub = this.form.valueChanges
      .pipe(
        map(() => {
          diff(this.form.value, this.previousValue);
        }),
        filter((formData) => !isEmpty(formData)),
        map((formData) =>
          extend(
            {
              id: this.archiveUnit['#id'],
              title: this.previousValue.title,
              description: this.previousValue.description,
              descriptionLevel: this.previousValue.descriptionLevel,
              startDate: this.getStartDate(this.previousValue.startDate),
              endDate: this.getStartDate(this.previousValue.endDate),
            },
            formData
          )
        ),
        switchMap((formData) => of(formData)),
        catchError((error) => of(error))
      )
      .subscribe((formData: any) => console.log('value au = ', formData));
  }

  private formHasValidTitle(): boolean {
    const title = this.form.get('title');
    return title != null && !title.invalid && !title.pending;
  }

  private formHasValidDescription(): boolean {
    const description = this.form.get('description');
    return !description.invalid && !description.pending;
  }

  private formHasValidDescriptionLevel(): boolean {
    const descriptionLevel = this.form.get('descriptionLevel');
    return !descriptionLevel.invalid && !descriptionLevel.pending;
  }

  private formDescriptionHasChanged() {
    const formVal = this.form.get('description').value;
    const previousVal = this.previousValue.description;
    if ((formVal == undefined || formVal == '') && (previousVal == undefined || previousVal == '')) {
      return false;
    }
    return formVal != previousVal;
  }

  private formHasChanges() {
    return (
      this.form.get('title').value != this.previousValue.title ||
      this.formDescriptionHasChanged() ||
      this.form.get('descriptionLevel').value != this.previousValue.descriptionLevel ||
      this.getStartDate(this.form.get('startDate').value) != this.getStartDate(this.previousValue.startDate) ||
      this.getStartDate(this.form.get('endDate').value) != this.getStartDate(this.previousValue.endDate)
    );
  }

  private formIsValid() {
    return this.formHasValidTitle() && this.formHasValidDescription() && this.formHasValidDescriptionLevel();
  }

  formHasChangesAndIsValid(): boolean {
    return this.formIsValid() && this.formHasChanges();
  }

  cancelUpdate() {
    if (this.form.dirty) {
      const dialogToOpen = this.updateArchiveUnitDescMetadataAlerteFormCancelDialog;
      const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
      this.updateArchiveUnitDescMetadataAlerteFormCancelDialogSubscription = dialogRef
        .afterClosed()
        .pipe(filter((result) => !!result))
        .subscribe(() => {
          this.updateStarted = false;
          this.previousValue = null;
          this.form.reset();
          this.showNormalPanel.emit();
        });
    } else {
      this.updateStarted = false;
      this.previousValue = null;
      this.form.reset();
      this.showNormalPanel.emit();
    }
  }

  clearDate(date: 'startDate' | 'endDate') {
    if (date === 'startDate') {
      if (this.archiveUnit && this.archiveUnit.StartDate) {
        this.unsetAction.push('StartDate');
      }
      this.form.get(date).reset(null, { emitEvent: false });
    } else if (date === 'endDate') {
      if (this.archiveUnit && this.archiveUnit.EndDate) {
        this.unsetAction.push('EndDate');
      }
      this.form.get(date).reset(null, { emitEvent: false });
    } else {
      console.error('clearDate() error: unknown date ' + date);
    }
  }

  private getStartDate(originStartDate: Date): string {
    if (originStartDate) {
      const startDate =
        this.getDay(new Date(originStartDate).getDate()) +
        '/' +
        this.getMonth(new Date(originStartDate).getMonth() + 1) +
        '/' +
        new Date(originStartDate).getFullYear().toString();
      return startDate;
    }
  }

  private getMonth(num: number): string {
    if (num > 9) {
      return num.toString();
    } else {
      return '0' + num.toString();
    }
  }

  private getDay(day: number): string {
    if (day > 9) {
      return day.toString();
    } else {
      return '0' + day.toString();
    }
  }

  getAuTitle(unit: any) {
    return unit?.Title ? unit?.Title : unit?.Title_ ? (unit?.Title_?.fr ? unit?.Title_?.fr : unit?.Title_?.en) : unit?.Title_?.en;
  }

  getAuDescription(unit: any) {
    return unit?.Description
      ? unit?.Description
      : unit.Description_
        ? unit.Description_?.fr
          ? unit.Description_?.fr
          : unit.Description_?.en
        : unit.Description_?.en;
  }

  onDownloadObjectFromUnit(archiveUnit: Unit) {
    return this.archiveService.launchDownloadObjectFromUnit(archiveUnit['#id'], this.archiveUnit['#object'], this.archiveUnit['#tenant'],
      this.accessContract);
  }

  showArchiveUniteFullPath() {
    this.fullPath = true;
  }

  ngOnDestroy() {
    this.updateFormSub?.unsubscribe();
  }
}
