/* tslint:disable:no-magic-numbers max-file-line-count */

import {BASE_URL} from 'ui-frontend-common';

import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {Type} from '@angular/core';
import {inject, TestBed} from '@angular/core/testing';
import {FileType, Node} from 'projects/vitamui-library/src/public-api';
import {DescriptionLevel} from '../../models/description-level.enum';
import {FilingPlanService} from './filing-plan.service';

describe('FilingPlanService', () => {
  let httpTestingController: HttpTestingController;

  beforeEach(() => {

    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
      ],
      providers: [
        FilingPlanService,
        {provide: BASE_URL, useValue: '/fake-api'},
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
  });

  it('should be created', inject([FilingPlanService], (service: FilingPlanService) => {
    expect(service).toBeTruthy();
  }));

  it('should load a collection\'s tree', inject([FilingPlanService], (service: FilingPlanService) => {

    const rootNode: Node[] = [
      {
        id: 'prefix-2',
        vitamId: '2',
        label: 'label2',
        type: FileType.FOLDER_HOLDING,
        ingestContractIdentifier: null,
        parents: [],
        checked: false,
        children: []
      },
    ];

    const children: Node[] = [
      {
        id: 'prefix-2.1',
        vitamId: '2.1',
        label: 'label2.1',
        type: FileType.FOLDER_INGEST,
        ingestContractIdentifier: null,
        parents: rootNode,
        checked: false,
        children: []
      },
      {
        id: 'prefix-2.2',
        vitamId: '2.2',
        label: 'label2.2',
        type: null,
        ingestContractIdentifier: null,
        parents: rootNode,
        checked: false,
        children: []
      }
    ];
    rootNode[0].children = children;

    const subChild: Node = {
      id: 'prefix-2.2.1',
      vitamId: '2.2.1',
      label: 'label2.2.1',
      type: null,
      ingestContractIdentifier: null,
      parents: [children[1]],
      checked: false,
      children: []
    };
    children[1].children.push(subChild);

    service.loadTree(42, 'test_contract_id', 'prefix').subscribe((tree) => {
      console.log('Result: ', tree);
      console.log('Expected: ', rootNode);
      expect(tree).toEqual(rootNode);
    });
    expect(service.pending).toBe(true);

    const requests = httpTestingController.match('/fake-api/search/filingplan');
    expect(requests.length).toEqual(1);
    expect(requests[0].request.method).toEqual('GET');
    requests[0].flush({
      $hits: null,
      $results: [
        {
          '#id': '2',
          Title: 'label2',
          '#allunitups': [],
          '#unitups': null,
          '#unitType': 'HOLDING_UNIT',
          DescriptionLevel: DescriptionLevel.FILE
        },
        {
          '#id': '2.1',
          Title: 'label2.1',
          '#allunitups': ['2'],
          '#unitups': ['2'],
          DescriptionLevel: DescriptionLevel.FILE
        },
        {'#id': '2.2', Title: 'label2.2', '#allunitups': ['2'], '#unitups': ['2']},
        {'#id': '2.2.1', Title: 'label2.2.1', '#allunitups': ['2', '2.2'], '#unitups': ['2.2']},
      ]
    });

    expect(service.pending).toBe(false);

    httpTestingController.verify();

  }));

  it('should return an empty tree if an error occurs', inject([FilingPlanService], (service: FilingPlanService) => {

    service.loadTree(42, 'test_contract_id', '').subscribe((tree) => {
      expect(tree).toEqual([]);
    });

    const req = httpTestingController.expectOne('/fake-api/search/filingplan');
    req.error(new ErrorEvent('Network error', {
      message: 'Error',
    }));

    httpTestingController.verify();

  }));
});
