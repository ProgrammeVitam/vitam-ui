import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { ArchiveService } from './archive.service';
import { Unit } from './models/unit.interface';

@Injectable()
export class ArchiveSearchResolverService implements Resolve<Unit> {
  constructor(private archiveService: ArchiveService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    const id = route.paramMap.get('id');
    const accessContractId = route.paramMap.get('accessContractId');
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', accessContractId);

    return this.archiveService.findArchiveUnit(id, headers).pipe(
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
