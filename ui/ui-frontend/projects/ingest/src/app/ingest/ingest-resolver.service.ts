import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, Router } from '@angular/router';
 import { Observable } from 'rxjs';
 import { map, take } from 'rxjs/operators';

 import { IngestService } from './ingest.service';
//import{Ingest} from 'ui-frontend-common'


@Injectable()
export class IngestResolverService implements Resolve<Event>{
//constructor() {}
  constructor(private ingestService: IngestService, private router: Router) { }
 // ingest  = "oussama";
  resolve(route: ActivatedRouteSnapshot): Observable<Event> {
    
    const id = route.paramMap.get('id');
    console.log(id);
    return this.ingestService.get(id)
      .pipe(
        take(1),
        map((ingest: Event) => {

          if (ingest) {
            return ingest;

          } else {
            this.router.navigate(['/']);

            return null;
          }
        })
      );
   
  }
  
}

