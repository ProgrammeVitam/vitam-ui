import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { IngestService } from './ingest.service';



@Injectable()
export class IngestResolverService implements Resolve<any>{

    constructor(private ingestService: IngestService, private router: Router) { }

    resolve(route: ActivatedRouteSnapshot): Observable<any> {

        const id = route.paramMap.get('id');
        console.log(id);
        return this.ingestService.get(id)
            .pipe(
                take(1),
                map((ingest: any) => {

                    if(ingest) {
                        return ingest;

                    } else {
                        this.router.navigate(['/']);

                        return null;
                    }
                })
            );

    }

}

