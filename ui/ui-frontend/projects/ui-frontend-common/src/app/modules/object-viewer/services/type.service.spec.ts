import { TestBed } from '@angular/core/testing';
import { ObjectViewerModule } from '../object-viewer.module';
import { TypeService } from './type.service';

describe('TypeService', () => {
  let service: TypeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ObjectViewerModule],
    });
    service = TestBed.inject(TypeService);
  });

  describe('isConsistent', () => {
    it('should value be consistent', () => {
      const consistentValues = [['ok'], { key: 'name' }, [{ children: [{ key: 'name' }] }], [{ children: [{ key: null, value: 'ok' }] }]];

      consistentValues.forEach((value) => expect(service.isConsistent(value)).toBeTruthy());
    });
    it('should value be inconsistent', () => {
      const unconsistentValues = [[''], [], [[]], [{}], [{ children: [{ key: null }] }]];

      unconsistentValues.forEach((value) => expect(service.isConsistent(value)).toBeFalsy());
    });
  });
});
