import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ExtendedOntology } from '../../../object-viewer/models';
import { Collection, SchemaService } from '../../../schema';

@Injectable()
export class ArchiveUnitSchemaService {
  constructor(private schemaService: SchemaService) {}

  getInternalOntologyFieldsList(): Observable<ExtendedOntology[]> {
    return this.schemaService.getSchema(Collection.ARCHIVE_UNIT);
  }
}
