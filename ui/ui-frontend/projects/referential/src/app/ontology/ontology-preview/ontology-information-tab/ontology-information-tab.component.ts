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
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import {
  ApplicationId,
  diff,
  Ontology,
  Option,
  Role,
  SecurityService,
  setTypeDetailAndStringSize,
  VitamUICommonModule,
  VitamUILibraryModule,
} from 'vitamui-library';
import { OntologyService } from '../../ontology.service';
import { collections, sizes, types } from '../../ontology-form-options';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-ontology-information-tab',
  templateUrl: './ontology-information-tab.component.html',
  styleUrls: ['./ontology-information-tab.component.scss'],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, TranslateModule, VitamUICommonModule, VitamUILibraryModule],
})
export class OntologyInformationTabComponent {
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  form: FormGroup;
  disabled = true;
  submitted = false;
  sizeFieldVisible = false;
  types = types;
  sizes = sizes;
  collections: Option[] = [];

  @Input()
  set inputOntology(ontology: Ontology) {
    this._inputOntology = ontology;

    const canUpdateVocabulary = this.securityService.hasRole(ApplicationId.ONTOLOGY_APP, Role.ROLE_UPDATE_ONTOLOGIES);
    this.disabled = !canUpdateVocabulary || ontology.origin === 'INTERNAL';

    Object.entries(this.form.controls)
      .filter(([key]) => !['identifier', 'creationDate'].includes(key))
      .forEach(([_key, control]) => (this.disabled ? control.disable() : control.enable()));

    if (!ontology.description) {
      this._inputOntology.description = '';
    }

    if (!ontology.collections) {
      this._inputOntology.collections = [];
    }

    this.collections = this.disabled
      ? // When ontology is disabled we cannot modify it, so we can let the values as they are
        this._inputOntology.collections.map((collection) => ({ key: collection, label: collection }))
      : collections;
    this.sizeFieldVisible = ['TEXT', 'GEO_POINT', 'KEYWORD'].includes(this._inputOntology.type);

    this.resetForm(this.inputOntology);
    this.updated.emit(false);
  }

  get inputOntology(): Ontology {
    return { ...this._inputOntology, collections: this._inputOntology.collections.sort() };
  }

  private _inputOntology: Ontology;

  previousValue = (): any => {
    return (Object.keys(this.form.controls) as (keyof Ontology)[]).reduce((acc: any, key) => {
      acc[key] = this._inputOntology[key] || null;
      return acc;
    }, {} as Partial<Ontology>);
  };

  constructor(
    private formBuilder: FormBuilder,
    private ontologyService: OntologyService,
    private securityService: SecurityService,
  ) {
    this.form = this.formBuilder.group({
      identifier: [{ value: null, disabled: true }, Validators.required],
      shortName: [{ value: null, disabled: true }, Validators.required],
      type: [{ value: null, disabled: true }, Validators.required],
      typeDetail: [{ value: null, disabled: true }],
      stringSize: [{ value: null, disabled: true }],
      collections: [{ value: null, disabled: true }, Validators.required],
      description: [{ value: null, disabled: true }],
      creationDate: [{ value: null, disabled: true }],
    });

    this.form.get('type').valueChanges.subscribe((key) => {
      if (!this.disabled) this.sizeFieldVisible = ['TEXT', 'GEO_POINT', 'KEYWORD'].includes(key);
      setTypeDetailAndStringSize(key, this.form);
    });
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<Ontology> {
    const payload = {
      id: this.previousValue().id,
      identifier: this.previousValue().identifier,
      ...this.form.value,
    };

    return of(payload).pipe(switchMap((formData: any) => this.ontologyService.patch(formData).pipe(catchError(() => of(null)))));
  }

  onSubmit() {
    this.submitted = true;
    this.prepareSubmit().subscribe(
      () => {
        this.ontologyService.get(this._inputOntology.identifier).subscribe((response) => {
          this.submitted = false;
          this.inputOntology = response;
          this.ontologyService.updated.next(this.inputOntology);
        });
      },
      () => {
        this.submitted = false;
      },
    );
  }

  resetForm(ontology: Ontology) {
    this.form.reset(ontology, { emitEvent: false });
  }
}
