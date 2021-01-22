import {TestBed} from '@angular/core/testing';

import {VitamUIRadioGroupService} from './vitamui-radio-group.service';

describe('VitamUIRadioGroupService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [VitamUIRadioGroupService]
  }));

  it('should be created', () => {
    const service: VitamUIRadioGroupService = TestBed.inject(VitamUIRadioGroupService);
    expect(service).toBeTruthy();
  });
});
