/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *
 *
 */
import { TestBed } from '@angular/core/testing';
import { SedaData } from '../../models/seda-data';
import { SedaService } from './seda.service';

describe('SedaService', () => {
  let service: SedaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SedaService);
  });

  it('should create service', () => {
    expect(service).toBeTruthy();
  });

  it('should be mandatory seda node', () => {
    const node: SedaData = service.sedaRules[0];

    const archiveTransferNode: SedaData = service.findNode('ArchiveTransfer', node);

    expect(archiveTransferNode).toBeTruthy('Node not found');
    expect(service.isMandatory(archiveTransferNode)).toBeTruthy('The node is not mandatory');
  });

  it('should be deletable seda node', () => {
    const node: SedaData = service.sedaRules[0];

    const managementNode: SedaData = service.findNode('Management', node);

    expect(managementNode).toBeTruthy('Node not found');
    expect(service.isDeletable(managementNode)).toBeTruthy('The node is not deletable');
  });

  it('should be multiple seda node', () => {
    const node: SedaData = service.sedaRules[0];

    const ruleNodes: SedaData[] = service.findAllNodes('Rule', node);

    ruleNodes.forEach((ruleNode) => {
      expect(ruleNode).toBeTruthy('Node not found');
      expect(service.isMultiple(ruleNode)).toBeTruthy('The node is not multiple');
    });
  });

  it('should be extensible seda node', () => {
    const node: SedaData = service.sedaRules[0];

    const eventNodes: SedaData[] = service.findAllNodes('Content', node);

    eventNodes.forEach((eventNode) => {
      expect(eventNode).toBeTruthy('Node not found');
      expect(service.isExtensible(eventNode)).toBeTruthy('The node is not extensible');
    });
  });
});
