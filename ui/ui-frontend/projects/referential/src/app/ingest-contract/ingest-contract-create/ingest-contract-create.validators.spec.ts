import {IngestContractCreateValidators} from './ingest-contract-create.validators';

describe('IngestContractCreateValidators', () => {
  it('should create an instance', () => {
    const ingestContractServiceSpy = jasmine.createSpyObj('IngestContractService', ['existsProperties']);
    expect(new IngestContractCreateValidators(ingestContractServiceSpy)).toBeTruthy();
  });
});
