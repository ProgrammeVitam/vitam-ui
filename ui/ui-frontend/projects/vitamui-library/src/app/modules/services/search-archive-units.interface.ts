import { Observable } from 'rxjs';
import { PagedResult, SearchCriteriaDto } from '../models/criteria/search-criteria.interface';

export interface SearchArchiveUnitsInterface {
  searchArchiveUnitsByCriteria(searchCriteria: SearchCriteriaDto, transactionId?: string): Observable<PagedResult>;
}
