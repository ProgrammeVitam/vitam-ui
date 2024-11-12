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
import { TestBed } from '@angular/core/testing';
import { ObjectViewerModule } from '../object-viewer.module';
import { DataStructureService } from './data-structure.service';

describe('DataStructureService', () => {
  let service: DataStructureService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ObjectViewerModule],
    });
    service = TestBed.inject(DataStructureService);
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should flatten data', () => {
    const computer = {
      name: 'asus',
      cpus: [
        { name: 'xeon', cores: 4 },
        { name: 'xeon', cores: 4 },
        { name: 'xeon', cores: 4 },
        { name: 'xeon', cores: 4 },
      ],
      description: 'description',
      nested: {
        information: 'information',
      },
    };
    const expected = {
      name: 'asus',
      'cpus[0].name': 'xeon',
      'cpus[0].cores': 4,
      'cpus[1].name': 'xeon',
      'cpus[1].cores': 4,
      'cpus[2].name': 'xeon',
      'cpus[2].cores': 4,
      'cpus[3].name': 'xeon',
      'cpus[3].cores': 4,
      description: 'description',
      'nested.information': 'information',
    };

    expect(service.flatten(computer)).toEqual(expected);
  });

  it('should unflatten data', () => {
    const flattened = {
      name: 'asus',
      'cpus[0].name': 'xeon',
      'cpus[0].cores': 4,
      'cpus[1].name': 'xeon',
      'cpus[1].cores': 4,
      'cpus[2].name': 'xeon',
      'cpus[2].cores': 4,
      'cpus[3].name': 'xeon',
      'cpus[3].cores': 4,
      description: 'description',
      'nested.information': 'information',
    };
    const expected = {
      name: 'asus',
      cpus: [
        { name: 'xeon', cores: 4 },
        { name: 'xeon', cores: 4 },
        { name: 'xeon', cores: 4 },
        { name: 'xeon', cores: 4 },
      ],
      description: 'description',
      nested: {
        information: 'information',
      },
    };
    expect(service.unflatten(flattened)).toEqual(expected);
  });

  it('should flatten complexe data', () => {
    const data = {
      name: 'Complex Data Example',
      age: 30,
      address: {
        street: '123 Main St',
        city: 'Big City',
        country: 'Country',
      },
      friends: [
        {
          name: 'John Doe',
          age: 28,
        },
        {
          name: 'Jane Smith',
          age: 32,
          address: {
            street: '456 Elm St',
            city: 'Small Town',
            country: 'Country',
          },
          hobbies: ['Reading', 'Painting'],
        },
      ],
      projects: [
        {
          name: 'Project A',
          status: 'In Progress',
          team: ['John Doe', 'Jane Smith'],
        },
        {
          name: 'Project B',
          status: 'Completed',
          team: ['Alice', 'Bob', 'Eve'],
        },
      ],
      nestedLevel1: {
        nestedLevel2: {
          nestedLevel3: {
            nestedLevel4: {
              nestedLevel5: {
                nestedLevel6: {
                  nestedLevel7: {
                    nestedLevel8: {
                      nestedLevel9: {
                        nestedLevel10: 'Final Nested Level!',
                      },
                    },
                  },
                },
              },
            },
          },
        },
      },
    };
    const flattened = {
      name: 'Complex Data Example',
      age: 30,
      'address.street': '123 Main St',
      'address.city': 'Big City',
      'address.country': 'Country',
      'friends[0].name': 'John Doe',
      'friends[0].age': 28,
      'friends[1].name': 'Jane Smith',
      'friends[1].age': 32,
      'friends[1].address.street': '456 Elm St',
      'friends[1].address.city': 'Small Town',
      'friends[1].address.country': 'Country',
      'friends[1].hobbies[0]': 'Reading',
      'friends[1].hobbies[1]': 'Painting',
      'projects[0].name': 'Project A',
      'projects[0].status': 'In Progress',
      'projects[0].team[0]': 'John Doe',
      'projects[0].team[1]': 'Jane Smith',
      'projects[1].name': 'Project B',
      'projects[1].status': 'Completed',
      'projects[1].team[0]': 'Alice',
      'projects[1].team[1]': 'Bob',
      'projects[1].team[2]': 'Eve',
      'nestedLevel1.nestedLevel2.nestedLevel3.nestedLevel4.nestedLevel5.nestedLevel6.nestedLevel7.nestedLevel8.nestedLevel9.nestedLevel10':
        'Final Nested Level!',
    };

    expect(service.flatten(data)).toEqual(flattened);
  });

  it('should unflatten complexe data', () => {
    const unflattened = {
      name: 'Complex Data Example',
      age: 30,
      address: {
        street: '123 Main St',
        city: 'Big City',
        country: 'Country',
      },
      friends: [
        {
          name: 'John Doe',
          age: 28,
        },
        {
          name: 'Jane Smith',
          age: 32,
          address: {
            street: '456 Elm St',
            city: 'Small Town',
            country: 'Country',
          },
          hobbies: ['Reading', 'Painting'],
        },
      ],
      projects: [
        {
          name: 'Project A',
          status: 'In Progress',
          team: ['John Doe', 'Jane Smith'],
        },
        {
          name: 'Project B',
          status: 'Completed',
          team: ['Alice', 'Bob', 'Eve'],
        },
      ],
      nestedLevel1: {
        nestedLevel2: {
          nestedLevel3: {
            nestedLevel4: {
              nestedLevel5: {
                nestedLevel6: {
                  nestedLevel7: {
                    nestedLevel8: {
                      nestedLevel9: {
                        nestedLevel10: 'Final Nested Level!',
                      },
                    },
                  },
                },
              },
            },
          },
        },
      },
    };
    const flattened = {
      name: 'Complex Data Example',
      age: 30,
      'address.street': '123 Main St',
      'address.city': 'Big City',
      'address.country': 'Country',
      'friends[0].name': 'John Doe',
      'friends[0].age': 28,
      'friends[1].name': 'Jane Smith',
      'friends[1].age': 32,
      'friends[1].address.street': '456 Elm St',
      'friends[1].address.city': 'Small Town',
      'friends[1].address.country': 'Country',
      'friends[1].hobbies[0]': 'Reading',
      'friends[1].hobbies[1]': 'Painting',
      'projects[0].name': 'Project A',
      'projects[0].status': 'In Progress',
      'projects[0].team[0]': 'John Doe',
      'projects[0].team[1]': 'Jane Smith',
      'projects[1].name': 'Project B',
      'projects[1].status': 'Completed',
      'projects[1].team[0]': 'Alice',
      'projects[1].team[1]': 'Bob',
      'projects[1].team[2]': 'Eve',
      'nestedLevel1.nestedLevel2.nestedLevel3.nestedLevel4.nestedLevel5.nestedLevel6.nestedLevel7.nestedLevel8.nestedLevel9.nestedLevel10':
        'Final Nested Level!',
    };

    expect(service.unflatten(flattened)).toEqual(unflattened);
  });

  it('should restore original after flat/unflat data', () => {
    const data = {
      name: 'asus',
      cpus: [
        { name: 'xeon', cores: 4 },
        { name: 'xeon', cores: 4 },
        { name: 'xeon', cores: 4 },
        { name: 'xeon', cores: 4 },
      ],
      description: 'description',
      nested: {
        information: 'information',
      },
    };
    const flattened = service.flatten(data);
    const unflattened = service.unflatten(flattened);

    expect(unflattened).toEqual(data);
  });

  it('should find deep value', () => {
    const unflattened = {
      name: 'asus',
      cpus: [
        { name: 'xeon', cores: 4 },
        { name: 'xeon', cores: 4 },
        { name: 'xeon', cores: 4 },
        { name: 'xeon', cores: 4 },
      ],
      description: 'description',
      nested: {
        information: 'information',
      },
    };

    expect(service.deepValue(unflattened, 'name')).toEqual('asus');
    expect(service.deepValue(unflattened, 'nested.information')).toEqual('information');
    expect(service.deepValue(unflattened, 'cpus[0].cores')).toEqual(4);
  });

  it('should merge arrays', () => {
    const defaultValue: any = {
      ArchivalAgencyArchiveUnitIdentifier: [],
      FilePlanPosition: [],
      OriginatingAgencyArchiveUnitIdentifier: [],
      OriginatingSystemId: [],
      SystemId: [],
      TransferringAgencyArchiveUnitIdentifier: [],
    };
    const data = {
      OriginatingSystemId: ['MC127858'],
      OriginatingAgencyArchiveUnitIdentifier: ['MC127858'],
      TransferringAgencyArchiveUnitIdentifier: ['TS99'],
      ArchivalAgencyArchiveUnitIdentifier: ['P-36'],
      FilePlanPosition: ['Dis0008'],
    };
    const mergedData = service.deepMerge(defaultValue, data);
    const expected: any = {
      ArchivalAgencyArchiveUnitIdentifier: ['P-36'],
      OriginatingAgencyArchiveUnitIdentifier: ['MC127858'],
      FilePlanPosition: ['Dis0008'],
      OriginatingSystemId: ['MC127858'],
      SystemId: [],
      TransferringAgencyArchiveUnitIdentifier: ['TS99'],
    };

    expect(mergedData).toEqual(expected);
  });
});
