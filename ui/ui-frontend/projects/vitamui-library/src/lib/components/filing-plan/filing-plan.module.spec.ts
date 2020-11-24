import {FilingPlanModule} from './filing-plan.module';

describe('FilingPlanModule', () => {
  let filingPlanModule: FilingPlanModule;

  beforeEach(() => {
    filingPlanModule = new FilingPlanModule();
  });

  it('should create an instance', () => {
    expect(filingPlanModule).toBeTruthy();
  });
});
