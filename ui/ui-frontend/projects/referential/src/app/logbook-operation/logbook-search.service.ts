/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import * as moment from 'moment';
import {
  Event, LogbookApiService, PageRequest, SearchService, VitamSelectOperator, VitamSelectQuery
} from 'ui-frontend-common';

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LogbookSearchService extends SearchService<Event> {

  constructor(http: HttpClient, logbookApi: LogbookApiService) {
    super(http, logbookApi);
  }

  static buildVitamQuery(pageRequest: PageRequest, criteria: any): VitamSelectQuery {
    const baseParameters: Partial<VitamSelectQuery> = {
      $projection: {},
      $filter: {
        $limit: pageRequest.size,
        $offset: Math.max(0, pageRequest.page - 1) * pageRequest.size,
        $orderby: { evDateTime: -1 }
      }
    };

    if (!criteria) {
      return {
        $query: {} as VitamSelectOperator,
        ...baseParameters
      };
    }

    const queryOperators = this.buildQueryOperators(criteria);

    if (queryOperators.length === 0) {
      return {
        $query: {} as VitamSelectOperator,
        ...baseParameters
      };
    }

    return {
      $query: {
        $and: [
          ...this.buildQueryOperators(criteria),
        ]
      },
      ...baseParameters
    };
  }

  private static buildQueryOperators(criteria: any): VitamSelectOperator[] {
    const operators: VitamSelectOperator[] = [];

    if (criteria.types && criteria.types.length > 0) {
      operators.push({ $in: { evTypeProc: criteria.types } });
    }

    if (criteria.status === 'ERROR') {
      operators.push({ $in: { 'events.outcome': ['KO', 'FATAL'] } });
    }

    if (criteria.evDateTime_Start) {
      operators.push({ $gte: { evDateTime: criteria.evDateTime_Start } });
    }

    if (criteria.evDateTime_End) {
      operators.push({ $lte: { evDateTime: moment(criteria.evDateTime_End).endOf('day') } });
    }

    if (criteria.evId) {
      operators.push({ $eq: { evId: criteria.evId } });
    }

    return operators;
  }

}
