import { Injectable } from '@angular/core';
import { ArchiveService } from './archive.service';
import { Observable } from 'rxjs';
import { Router, Resolve, ActivatedRouteSnapshot } from '@angular/router';
import { map, take } from 'rxjs/operators';
import { HttpHeaders } from '@angular/common/http';
import { Unit } from './models/unit.interface';


@Injectable()
export class ArchiveSearchResolverService implements Resolve<Unit> {

  constructor(private archiveService: ArchiveService, private router: Router) { }

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
   
    const id = route.paramMap.get('id');
    const accessContractId = route.paramMap.get('accessContractId');
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', accessContractId);
    

    return this.archiveService.findArchiveUnit(id, headers)
      .pipe(
        take(1),
        map((archiveUnit: Unit) => {
          if (archiveUnit) {
            return archiveUnit;
          } else {
            this.router.navigate(['/']);
            return null;
          }
        })
      );
  }
}
