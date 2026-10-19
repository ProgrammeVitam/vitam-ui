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
import { Component, computed, inject, Injector, OnDestroy, OnInit, signal, viewChild } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { form, FormField, FormRoot, maxLength, minLength, required } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { skip } from 'rxjs/operators';
import {
  ConfirmDialogService,
  getTypeDetailAndStringSize,
  Ontology,
  requiredValue,
  VitamUICommonModule,
  VitamUILibraryModule,
} from 'vitamui-library';
import { OntologyService } from '../ontology.service';
import { OntologyCreateValidators } from './ontology-create.validators';
import { collections, sizes, types } from '../ontology-form-options';
import { TranslatePipe } from '@ngx-translate/core';

interface OntologyCreateForm {
  identifier: string;
  shortName: string;
  type: string;
  typeDetail: string;
  stringSize: string;
  collections: string[];
  description: string;
  origin: string;
}

@Component({
  selector: 'app-ontology-create',
  templateUrl: './ontology-create.component.html',
  styleUrls: ['./ontology-create.component.scss'],
  imports: [MatDialogModule, FormRoot, FormField, VitamUICommonModule, VitamUILibraryModule, TranslatePipe],
})
export class OntologyCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<OntologyCreateComponent>>(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly ontologyService = inject(OntologyService);
  private readonly ontologyCreateValidator = inject(OntologyCreateValidators);
  private readonly injector = inject(Injector);

  private readonly formModel = signal<OntologyCreateForm>({
    identifier: null,
    shortName: null,
    type: null,
    typeDetail: null,
    stringSize: null,
    collections: null,
    description: null,
    origin: 'INTERNAL',
  });
  readonly sizeFieldVisible = computed(() => ['TEXT', 'GEO_POINT', 'KEYWORD'].includes(this.formModel().type));
  readonly form = form(
    this.formModel,
    (path) => {
      required(path.identifier);
      minLength(path.identifier, 2);
      maxLength(path.identifier, 100);
      this.ontologyCreateValidator.patternID(path.identifier);
      this.ontologyCreateValidator.uniqueID(path.identifier);
      required(path.shortName);
      minLength(path.shortName, 2);
      maxLength(path.shortName, 100);
      required(path.type);
      required(path.stringSize, { when: () => this.sizeFieldVisible() });
      requiredValue(path.collections);
    },
    {
      submission: {
        action: async () => {
          this.onSubmit();
        },
        ignoreValidators: 'all',
      },
    },
  );
  hasCustomGraphicIdentity = false;
  hasError = true;
  message: string;
  isDisabledButton = signal(false);
  types = types;
  collections = collections;
  sizes = sizes;

  private keyPressSubscription: Subscription;

  readonly fileSearch = viewChild<any>('fileSearch');

  ngOnInit() {
    toObservable(this.form.type().value, { injector: this.injector })
      .pipe(skip(1))
      .subscribe((key) => {
        const typeDetailAndStringSize = getTypeDetailAndStringSize(key);
        if (typeDetailAndStringSize) {
          this.formModel.update((value) => ({ ...value, ...typeDetailAndStringSize }));
        }
      });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.form().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.form().invalid()) {
      this.isDisabledButton.set(true);
      return;
    }
    this.isDisabledButton.set(true);
    this.ontologyService.create(this.form().value() as Ontology).subscribe(
      () => {
        this.isDisabledButton.set(false);
        this.dialogRef.close({ success: true, action: 'none' });
      },
      (error: any) => {
        this.dialogRef.close({ success: false, action: 'none' });
        console.error(error);
      },
    );
  }
}
