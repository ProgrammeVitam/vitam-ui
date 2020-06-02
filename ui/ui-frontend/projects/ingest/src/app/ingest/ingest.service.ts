import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { IngestApiService } from '../core/api/ingest-api.service';


@Injectable({
  providedIn: 'root'
})
export class IngestService {

  constructor(private ingestApiService: IngestApiService) { }

  headers = new HttpHeaders();

  ingest(): Observable<any> {
    return this.ingestApiService.ingest(this.headers);
  }
}
