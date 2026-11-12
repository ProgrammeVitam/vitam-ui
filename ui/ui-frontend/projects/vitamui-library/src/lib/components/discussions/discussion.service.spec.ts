/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { OAuthStorage } from 'angular-oauth2-oidc';
import { AuthService } from '../../../app/modules/auth.service';
import { ConfigService } from '../../../app/modules/config.service';
import { BASE_URL } from '../../../app/modules/injection-tokens';
import { TenantSelectionService } from '../../../app/modules/tenant-selection.service';
import { DiscussionDto, DiscussionService } from './discussion.service';

describe('DiscussionService', () => {
  let service: DiscussionService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BASE_URL, useValue: 'http://localhost' },
        { provide: ConfigService, useValue: { config: null } },
        { provide: OAuthStorage, useValue: { getItem: () => null } },
        { provide: TenantSelectionService, useValue: { getLastTenantIdentifier: () => 1 } },
        { provide: AuthService, useValue: { user: { id: 'userId' } } },
      ],
    });

    service = TestBed.inject(DiscussionService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('findEntityIdsWithUnreadDiscussions', () => {
    it('should resolve to an empty array without calling the backend when there are no entity ids', () => {
      let result: string[];
      service.findEntityIdsWithUnreadDiscussions('TRANSACTION', []).subscribe((r) => (result = r));

      expect(result).toEqual([]);
      httpTestingController.expectNone(() => true);
    });

    it('should batch-query the backend for the given entity ids', () => {
      let result: string[];
      service.findEntityIdsWithUnreadDiscussions('TRANSACTION', ['id1', 'id2']).subscribe((r) => (result = r));

      const req = httpTestingController.expectOne('http://localhost/discussions/unread');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ entityType: 'TRANSACTION', entityIds: ['id1', 'id2'] });
      req.flush(['id1']);

      expect(result).toEqual(['id1']);
    });
  });

  describe('readStatusChanged', () => {
    it('should emit when a discussion is marked as read', () => {
      let emitted = false;
      service.readStatusChanged.subscribe(() => (emitted = true));

      const discussion: DiscussionDto = {
        discussion: {
          id: 'discussionId',
          title: 'title',
          entities: [],
          createdAt: new Date(),
          lastMessageAt: new Date(),
          messages: [],
          status: 'IN_PROGRESS',
        },
        lastReadAt: new Date(),
        unread: true,
      };
      service.markAsRead(discussion);

      expect(emitted).toBe(true);
      httpTestingController.expectOne(`http://localhost/discussions/${discussion.discussion.id}/read`).flush(null);
    });
  });
});
