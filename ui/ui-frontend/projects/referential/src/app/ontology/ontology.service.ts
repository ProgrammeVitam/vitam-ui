import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {tap} from 'rxjs/operators';
import {SearchService, VitamUISnackBar} from 'ui-frontend-common';

import {Ontology} from 'projects/vitamui-library/src/public-api';
import {OntologyApiService} from '../core/api/ontology-api.service';
import {VitamUISnackBarComponent} from '../shared/vitamui-snack-bar';

@Injectable({
  providedIn: 'root'
})
export class OntologyService extends SearchService<Ontology> {

  updated = new Subject<Ontology>();

  constructor(
    private ontologyApiService: OntologyApiService,
    private snackBar: VitamUISnackBar,
    http: HttpClient) {
    super(http, ontologyApiService, 'ALL');
  }

  get(id: string): Observable<Ontology> {
    return this.ontologyApiService.getOne(encodeURI(id));
  }

  existsProperties(properties: { name?: string, identifier?: string }): Observable<any> {
    const existOntology: any = {};
    if (properties.name) {
      existOntology.name = properties.name;
    }
    if (properties.identifier) {
      existOntology.identifier = properties.identifier;
    }

    const ontology = existOntology as Ontology;
    return this.ontologyApiService.check(ontology, this.headers);
  }

  create(ontology: Ontology) {
    return this.ontologyApiService.create(ontology, this.headers)
      .pipe(
        tap(
          (response: Ontology) => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              data: {type: 'ontologyCreate', name: response.identifier},
              duration: 10000
            });
          },
          (error: any) => {
            this.snackBar.open(error.error.message, null, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000
            });
          }
        )
      );
  }

  patch(data: { id: string, [key: string]: any }): Observable<Ontology> {
    return this.ontologyApiService.patch(data)
      .pipe(
        tap((response) => this.updated.next(response)),
        tap(
          (response) => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000,
              data: {type: 'ontologyUpdate', name: response.identifier}
            });
          },
          (error) => {
            this.snackBar.open(error.error.message, null, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000
            });
          }
        )
      );
  }

  delete(ontology: Ontology): Observable<any> {
    return this.ontologyApiService.delete(ontology.id).pipe(
      tap(() => {
          this.snackBar.openFromComponent(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
            data: {type: 'ontologyDelete', name: ontology.identifier}
          });
        },
        (error) => {
          this.snackBar.open(error.error.message, null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000
          });
        })
    );
  }

  export() {
    this.ontologyApiService.export().subscribe(
      (response) => {
        const a = document.createElement('a');
        document.body.appendChild(a);
        a.style.display = 'none';

        const blob = new Blob([response], {type: 'octet/stream'});
        const url = window.URL.createObjectURL(blob);
        a.href = url;
        a.download = 'agencies.csv';
        a.click();
        window.URL.revokeObjectURL(url);
      }, (error) => {
        this.snackBar.open(error.error.message, null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000
        });
      }
    );
  }

  exists(id: string): Observable<boolean> {
    const ontology = {identifier: id} as Ontology;
    return this.ontologyApiService.check(ontology, this.headers);
  }
}
