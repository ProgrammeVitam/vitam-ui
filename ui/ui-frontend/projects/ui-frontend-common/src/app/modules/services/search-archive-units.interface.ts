import { Observable } from 'rxjs';
import { PagedResult, SearchCriteriaDto } from '../models';

export interface SearchArchiveUnitsInterface {
  searchArchiveUnitsByCriteria(searchCriteria: SearchCriteriaDto, transactionId?: string): Observable<PagedResult>;
}
