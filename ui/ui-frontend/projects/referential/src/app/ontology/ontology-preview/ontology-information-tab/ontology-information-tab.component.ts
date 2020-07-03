import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Ontology} from 'projects/vitamui-library/src/public-api';
import {Observable, of} from 'rxjs';
import {catchError, filter, map, switchMap} from 'rxjs/operators';
import {diff, Option} from 'ui-frontend-common';
import {extend, isEmpty} from 'underscore';
import {OntologyService} from '../../ontology.service';

@Component({
  selector: 'app-ontology-information-tab',
  templateUrl: './ontology-information-tab.component.html',
  styleUrls: ['./ontology-information-tab.component.scss']
})
export class OntologyInformationTabComponent implements OnInit {
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  form: FormGroup;

  isInternal = true;

  submited = false;

  // FIXME: Get list from common var ?
  types: Option[] = [
    {key: 'DATE', label: 'Date', info: ''},
    {key: 'TEXT', label: 'Texte', info: ''},
    {key: 'KEYWORD', label: 'Mot clé', info: ''},
    {key: 'BOOLEAN', label: 'Boolean', info: ''},
    {key: 'LONG', label: 'Long', info: ''},
    {key: 'DOUBLE', label: 'Double', info: ''},
    {key: 'ENUM', label: 'Énumérer', info: ''},
    {key: 'GEO_POINT', label: 'Point Géographique', info: ''}
  ];

  // FIXME: Get list from common var ?
  collections: Option[] = [
    {key: 'Unit', label: 'Unité Archivistique', info: ''},
    {key: 'ObjectGroup', label: 'Groupe d\'objet', info: ''}
  ];

  @Input()
  set inputOntology(ontology: Ontology) {
    this._inputOntology = ontology;

    this.isInternal = ontology.origin === 'INTERNAL';

    if (!ontology.description) {
      this._inputOntology.description = '';
    }

    this.resetForm(this.inputOntology);
    this.updated.emit(false);
  }

  get inputOntology(): Ontology {
    return this._inputOntology;
  }

  // tslint:disable-next-line:variable-name
  private _inputOntology: Ontology;

  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({emitEvent: false});
    } else if (this.form.disabled) {
      this.form.enable({emitEvent: false});
      this.form.get('identifier').disable({emitEvent: false});
    }
  }

  previousValue = (): Ontology => {
    return this._inputOntology;
  }

  constructor(
    private formBuilder: FormBuilder,
    private ontologyService: OntologyService
  ) {
    this.form = this.formBuilder.group({
      identifier: [null, Validators.required],
      shortName: [null, Validators.required],
      type: [null, Validators.required],
      collections: [null, Validators.required],
      description: [null, Validators.required]
    });
    this.form.disable({emitEvent: false});
  }

  ngOnInit(): void {
    if (this._inputOntology.origin === 'EXTERNAL') {
      this.form.enable({emitEvent: false});
    }
  }

  isInvalid(): boolean {
    // TODO
    return false;
    /*     return this.form.get('name').invalid || this.form.get('name').pending ||
      this.form.get('description').invalid || this.form.get('description').pending ||
      this.form.get('status').invalid || this.form.get('status').pending ||
      this.form.get('archiveProfiles').invalid || this.form.get('archiveProfiles').pending; */
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<Ontology> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({id: this.previousValue().id, identifier: this.previousValue().identifier}, formData)),
      switchMap((formData: { id: string, [key: string]: any }) => this.ontologyService.patch(formData).pipe(catchError(() => of(null)))));
  }

  onSubmit() {
    this.submited = true;
    this.prepareSubmit().subscribe(() => {
      this.ontologyService.get(this._inputOntology.identifier).subscribe(
        response => {
          this.submited = false;
          this.inputOntology = response;
        }
      );
    }, () => {
      this.submited = false;
    });
  }

  resetForm(ontology: Ontology) {
    this.form.reset(ontology, {emitEvent: false});
  }
}
