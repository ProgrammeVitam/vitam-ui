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
import { Component, inject, signal, output, model, OnChanges, SimpleChanges, Injector, linkedSignal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { disabled, form, FormField, FormRoot, required } from '@angular/forms/signals';
import { Observable, of } from 'rxjs';
import { catchError, skip, switchMap } from 'rxjs/operators';
import {
  ApplicationId,
  DateTimePipe,
  diff,
  getTypeDetailAndStringSize,
  Ontology,
  Option,
  requiredValue,
  Role,
  SecurityService,
  VitamUICommonModule,
  VitamUILibraryModule,
} from 'vitamui-library';
import { OntologyService } from '../../ontology.service';
import { collections, sizes, types } from '../../ontology-form-options';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';

interface OntologyInformationForm {
  identifier: string;
  shortName: string;
  type: string;
  typeDetail: string;
  stringSize: string;
  collections: string[];
  description: string;
  creationDate: string;
}

@Component({
  selector: 'app-ontology-information-tab',
  templateUrl: './ontology-information-tab.component.html',
  styleUrls: ['./ontology-information-tab.component.scss'],
  imports: [CommonModule, FormRoot, FormField, VitamUICommonModule, VitamUILibraryModule, TranslatePipe],
  providers: [DateTimePipe],
})
export class OntologyInformationTabComponent implements OnChanges {
  private readonly ontologyService = inject(OntologyService);
  private readonly securityService = inject(SecurityService);
  private readonly dateTimePipe = inject(DateTimePipe);
  private readonly injector = inject(Injector);

  readonly updated = output<boolean>();
  /** The ontology fields (except identifier and creation date which are never editable) are disabled */
  readonly editDisabled = signal(true);
  submitted = signal(false);
  readonly sizeFieldVisible = signal(false);
  types = types;
  sizes = sizes;
  collections: Option[] = [];

  private readonly formModel = signal<OntologyInformationForm>({
    identifier: null,
    shortName: null,
    type: null,
    typeDetail: null,
    stringSize: null,
    collections: null,
    description: null,
    creationDate: null,
  });
  // Value set by the last form reset, to ignore the type change of the reset
  private resetValue: OntologyInformationForm = this.formModel();
  readonly form = form(
    this.formModel,
    (path) => {
      disabled(path.identifier);
      disabled(path.creationDate);
      disabled(path.shortName, () => this.editDisabled());
      disabled(path.type, () => this.editDisabled());
      disabled(path.typeDetail, () => this.editDisabled());
      disabled(path.stringSize, () => this.editDisabled());
      disabled(path.collections, () => this.editDisabled());
      disabled(path.description, () => this.editDisabled());
      required(path.identifier);
      required(path.shortName);
      required(path.type);
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
  readonly creationDateForm = form(
    linkedSignal(() => this.dateTimePipe.transform(this.formModel().creationDate, 'dd/MM/yyyy')),
    (path) => disabled(path),
  );

  readonly inputOntology = model<Ontology>(undefined);

  previousValue = (): any => {
    return (Object.keys(this.formModel()) as (keyof Ontology)[]).reduce((acc: any, key) => {
      acc[key] = this.inputOntology()[key] || null;
      return acc;
    }, {} as Partial<Ontology>);
  };

  constructor() {
    toObservable(this.form.type().value, { injector: this.injector })
      .pipe(skip(1))
      .subscribe((key) => {
        if (this.formModel() === this.resetValue) return; // Not a user change
        if (!this.editDisabled()) this.sizeFieldVisible.set(['TEXT', 'GEO_POINT', 'KEYWORD'].includes(key));
        const typeDetailAndStringSize = getTypeDetailAndStringSize(key);
        if (typeDetailAndStringSize) {
          this.formModel.update((value) => ({ ...value, ...typeDetailAndStringSize }));
        }
      });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['inputOntology']) {
      this.setInputOntology(this.inputOntology());
    }
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.formModel(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<Ontology> {
    const payload = {
      id: this.inputOntology().id,
      identifier: this.previousValue().identifier,
      ...this.enabledValue(),
    };

    return of(payload).pipe(switchMap((formData: any) => this.ontologyService.patch(formData).pipe(catchError(() => of(null)))));
  }

  onSubmit() {
    this.submitted.set(true);
    this.prepareSubmit().subscribe(
      () => {
        this.ontologyService.get(this.inputOntology().identifier).subscribe((response) => {
          this.submitted.set(false);
          this.setInputOntology(response);
          this.ontologyService.updated.next(this.sortedInputOntology());
        });
      },
      () => {
        this.submitted.set(false);
      },
    );
  }

  private setInputOntology(ontology: Ontology) {
    this.inputOntology.set(ontology);

    const canUpdateVocabulary = this.securityService.hasRole(ApplicationId.ONTOLOGY_APP, Role.ROLE_UPDATE_ONTOLOGIES);
    this.editDisabled.set(!canUpdateVocabulary || ontology.origin === 'INTERNAL');

    if (!ontology.description) {
      this.inputOntology().description = '';
    }

    if (!ontology.collections) {
      this.inputOntology().collections = [];
    }

    this.collections = this.editDisabled()
      ? // When ontology is disabled we cannot modify it, so we can let the values as they are
        this.inputOntology().collections.map((collection) => ({ key: collection, label: collection }))
      : collections;
    this.sizeFieldVisible.set(['TEXT', 'GEO_POINT', 'KEYWORD'].includes(this.inputOntology().type));

    this.resetForm(this.sortedInputOntology());
    this.updated.emit(false);
  }

  private sortedInputOntology(): Ontology {
    return { ...this.inputOntology(), collections: this.inputOntology().collections.sort() };
  }

  resetForm(ontology: Ontology) {
    // Like reactive forms reset, the missing values are reset to null
    this.resetValue = {
      identifier: ontology.identifier ?? null,
      shortName: ontology.shortName ?? null,
      type: ontology.type ?? null,
      typeDetail: ontology.typeDetail ?? null,
      stringSize: ontology.stringSize ?? null,
      collections: ontology.collections ?? null,
      description: ontology.description ?? null,
      creationDate: ontology.creationDate ?? null,
    };
    this.form().reset(this.resetValue);
  }

  /** Values of the enabled fields (like reactive forms `value`) */
  private enabledValue(): Partial<OntologyInformationForm> {
    const value = this.formModel();
    return Object.fromEntries(
      (Object.keys(value) as (keyof OntologyInformationForm)[])
        .filter((key) => !this.form[key]().disabled())
        .map((key) => [key, value[key]]),
    );
  }
}
