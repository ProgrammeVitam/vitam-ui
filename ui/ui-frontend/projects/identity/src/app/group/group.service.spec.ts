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
import { BASE_URL, Direction, Group, Operators, PageRequest, SearchQuery } from 'ui-frontend-common';

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { inject, TestBed } from '@angular/core/testing';

import { Type } from '@angular/core';
import { VitamUISnackBar, VitamUISnackBarComponent } from '../shared/vitamui-snack-bar';
import { GroupService } from './group.service';

describe('GroupService', () => {
  let httpTestingController: HttpTestingController;
  let groupService: GroupService;

  beforeEach(() => {
    const snackBarSpy = jasmine.createSpyObj('VitamUISnackBar', ['open', 'openFromComponent']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        GroupService,
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: BASE_URL, useValue: '/fake-api' },
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
    groupService = TestBed.inject(GroupService);
  });

  it('should be created', inject([GroupService], (service: GroupService) => {
    expect(service).toBeTruthy();
  }));

  it('should call /fake-api/groups?page=0&size=20&orderBy=name&direction=ASC&embedded=ALL', () => {
    groupService.search().subscribe((response) => expect(response).toEqual([]), fail);
    const req = httpTestingController.expectOne(
        '/fake-api/groups?page=0&size=20&orderBy=name&direction=ASC&embedded=ALL'
    );
    expect(req.request.method).toEqual('GET');
    const result: any = { values: [] };
    req.flush(result);
  });

  it('should call /fake-api/groups?page=42&size=15&orderBy=name&direction=DESC&embedded=ALL', () => {
    groupService.search(new PageRequest(42, 15, 'name', Direction.DESCENDANT))
      .subscribe((response) => expect(response).toEqual([]), fail);
    const req = httpTestingController.expectOne(
      '/fake-api/groups?page=42&size=15&orderBy=name&direction=DESC&embedded=ALL');
    expect(req.request.method).toEqual('GET');
    const result: any = { values: [] };
    req.flush(result);
  });

  it('should call /fake-api/groups?page=0&size=15&orderBy=&direction=DESC&embedded=ALL', () => {
    groupService.search(new PageRequest(0, 15, '', Direction.DESCENDANT))
      .subscribe((response) => expect(response).toEqual([null]), fail);
    let req = httpTestingController.expectOne(
      '/fake-api/groups?page=0&size=15&orderBy=&direction=DESC&embedded=ALL');
    expect(req.request.method).toEqual('GET');
    let result: any = { pageNum: 0, hasMore: true, pageSize: 15, values: [null] };
    req.flush(result);

    groupService.loadMore().subscribe(
      (response) => expect(response).toEqual([null, null]),
      fail
    );
    req = httpTestingController.expectOne(
      '/fake-api/groups?page=1&size=15&orderBy=&direction=DESC&embedded=ALL');
    expect(req.request.method).toEqual('GET');
    result = { pageNum: 1, pageSize: 15, hasMore: false, values: [null] };
    req.flush(result);
  });

  it('should not load more results', () => {
    groupService.search().subscribe((response) => expect(response).toEqual([null]), fail);
    const req = httpTestingController.expectOne('/fake-api/groups?page=0&size=20&orderBy=name&direction=ASC&embedded=ALL');
    expect(req.request.method).toEqual('GET');
    const result: any = { hasMore: false, pageSize: 20, pageNum: 0, values: [null] };
    req.flush(result);

    groupService.loadMore().subscribe(
      (response) => expect(response).toEqual([null]),
      fail
    );
    httpTestingController.expectNone('/fake-api/groups?page=1&size=20&orderBy=name&direction=ASC&embedded=ALL');
  });

  it('should return false', () => {
    expect(groupService.canLoadMore).toBeFalsy();
  });

  it('should return true', () => {
    groupService.search().subscribe(
      (response) => {
        expect(response).toEqual([null]);
        expect(groupService.canLoadMore).toBeTruthy();
      },
      fail
    );
    const req = httpTestingController.expectOne('/fake-api/groups?page=0&size=20&orderBy=name&direction=ASC&embedded=ALL');
    expect(req.request.method).toEqual('GET');
    const result: any = { hasMore: true, values: [null] };
    req.flush(result);
  });

  it('should call /fake-api/groups and display a success message', () => {
    const snackBar = TestBed.inject(VitamUISnackBar);
    const expectedGroup: Group = {
        id: '1',
        customerId: '4242442',
        name: 'Group Name',
        description: 'Group Description',
        level : '',
        usersCount: 0,
        profileIds: [],
        profiles: [],
        readonly : false,
    };
    groupService.create(expectedGroup).subscribe(
      (response: Group) => {
        expect(response).toEqual(expectedGroup);
        expect(snackBar.openFromComponent).toHaveBeenCalledTimes(1);
        expect(snackBar.openFromComponent).toHaveBeenCalledWith(VitamUISnackBarComponent, {
          panelClass: 'vitamui-snack-bar',
          data: { type: 'groupCreate', name: expectedGroup.name },
          duration: 10000
        });
      },
      fail
    );
    const req = httpTestingController.expectOne('/fake-api/groups');
    expect(req.request.method).toEqual('POST');
    req.flush(expectedGroup);
  });

  it('should display an error message', () => {
    const snackBar = TestBed.inject(VitamUISnackBar);
    const expectedProfileGroup: Group = {
        id: '1',
        customerId: '4242442',
        name: 'Group Name',
        description: 'Group Description',
        level : '',
        usersCount: 0,
        profileIds: [],
        profiles: [],
        readonly : false
    };
    groupService.create(expectedProfileGroup).subscribe(
      fail,
      () => {
        expect(snackBar.open).toHaveBeenCalledTimes(1);
        expect(snackBar.open).toHaveBeenCalledWith('Expected message', null, { panelClass: 'vitamui-snack-bar', duration: 10000 });
      }
    );
    const req = httpTestingController.expectOne('/fake-api/groups');
    expect(req.request.method).toEqual('POST');
    req.flush({ message: 'Expected message' }, {status: 400, statusText: 'Bad request'});
  });

  it('should call /fake-api/groups/42?embedded=ALL', () => {
    const expectedProfileGroup: Group = {
        id: '1',
        customerId: '4242442',
        name: 'Group Name',
        description: 'Group Description',
        level : '',
        usersCount: 0,
        profileIds: [],
        profiles: [],
        readonly : false
    };
    groupService.get('42').subscribe((profileGroup) => expect(profileGroup).toEqual(expectedProfileGroup), fail);
    const req = httpTestingController.expectOne('/fake-api/groups/42?embedded=ALL');
    expect(req.request.method).toEqual('GET');
    req.flush(expectedProfileGroup);
  });

  it('should return true if the profiles group exists', () => {
    groupService.exists('4242', 'profileGroupName').subscribe(
      (found) => {
        expect(found).toBeTruthy();
      },
      fail
    );

    const criterionArray: any[] = [ { key: 'customerId', value: '4242', operator: Operators.equals },
                                    { key: 'name', value: 'profileGroupName', operator: Operators.equals }];
    const query: SearchQuery = { criteria: criterionArray };
    const req = httpTestingController.expectOne('/fake-api/groups/check?criteria=' + encodeURI(JSON.stringify(query)));
    expect(req.request.method).toEqual('HEAD');
    req.flush('');
  });

  it('should return false if the profiles group does not exist', () => {
    groupService.exists('4242', 'profileGroupName').subscribe(
      (found) => {
        expect(found).toBeFalsy();
      },
      fail
    );
    const criterionArray: any[] = [ { key: 'customerId', value: '4242', operator: Operators.equals },
                                    { key: 'name', value: 'profileGroupName', operator: Operators.equals }];
    const query: SearchQuery = { criteria: criterionArray };
    const req = httpTestingController.expectOne('/fake-api/groups/check?criteria=' + encodeURI(JSON.stringify(query)));
    expect(req.request.method).toEqual('HEAD');
    req.flush('', { status: 204, statusText: 'No Content' });
  });

  it('should call PATCH /fake-api/groups/42', () => {
    const snackBar = TestBed.inject(VitamUISnackBar);
    const expectedProfileGroup: Group = {
        id: '1',
        customerId: '4242442',
        name: 'Group Name',
        description: 'Group Description',
        level : '',
        usersCount: 0,
        profileIds: [],
        profiles: [],
        readonly : false
    };
    groupService.updated.subscribe((profileGroup) => expect(profileGroup).toEqual(expectedProfileGroup), fail);
    groupService.patch({ id: '42', name: expectedProfileGroup.name }).subscribe(
      (profileGroup) => {
        expect(profileGroup).toEqual(expectedProfileGroup);
        expect(snackBar.openFromComponent).toHaveBeenCalledTimes(1);
        expect(snackBar.openFromComponent).toHaveBeenCalledWith(VitamUISnackBarComponent, {
          panelClass: 'vitamui-snack-bar',
          data: { type: 'groupUpdate', name: expectedProfileGroup.name },
          duration: 10000
        });
      },
      fail
    );
    const req = httpTestingController.expectOne('/fake-api/groups/42');
    expect(req.request.method).toEqual('PATCH');
    expect(req.request.body).toEqual({ id: '42', name: expectedProfileGroup.name });
    req.flush(expectedProfileGroup);
  });

  it('should display an error message', () => {
    const snackBar = TestBed.get(VitamUISnackBar);
    const expectedGroup: Group = {
        id: '1',
        customerId: '4242442',
        name: 'Group Name',
        description: 'Group Description',
        level : '',
        usersCount: 0,
        profileIds: [],
        profiles: [],
        readonly : false
    };
    groupService.patch({ id: '42', name: expectedGroup.name }).subscribe(
      fail,
      () => {
        expect(snackBar.open.calls.count()).toBe(1);
        expect(snackBar.open.calls.first().args[0]).toBe('Expected message');
      }
    );
    const req = httpTestingController.expectOne('/fake-api/groups/42');
    expect(req.request.method).toEqual('PATCH');
    req.flush({ message: 'Expected message' }, {status: 400, statusText: 'Bad request'});
  });

});
