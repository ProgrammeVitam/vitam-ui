import { TestBed } from '@angular/core/testing';
import { DataStructureService } from './data-structure.service';
import { DisplayRuleHelperService } from './display-rule-helper.service';
import { TypeService } from './type.service';

describe('DisplayRuleHelperService', () => {
  let service: DisplayRuleHelperService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DisplayRuleHelperService, TypeService, DataStructureService],
    });
    service = TestBed.inject(DisplayRuleHelperService);
  });

  describe('convertDataPathToSchemaPath', () => {
    it('should convert data paths into schema paths', () => {
      expect(service).toBeTruthy();
      expect(service.convertDataPathToSchemaPath('Signature[0]')).toEqual('Signature');
      expect(service.convertDataPathToSchemaPath('Signature[0].Signer[0]')).toEqual('Signature.Signer');
      expect(service.convertDataPathToSchemaPath('Signature[0].Signer[0].Activity')).toEqual('Signature.Signer.Activity');
    });
  });
});
