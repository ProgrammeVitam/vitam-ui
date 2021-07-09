import {AccessionStatusPipe} from './accession-status.pipe';
import {AccessionRegisterStatus} from "../../../../../vitamui-library/src/lib/models/accession-registers-status";

describe('AccessionStatusPipe', () => {
  it('create an instance', () => {
    const pipe = new AccessionStatusPipe();
    expect(pipe).toBeTruthy();
  });

  it('should display human readable status', () => {
    const pipe = new AccessionStatusPipe();
    expect(pipe.transform(AccessionRegisterStatus.STORED_AND_UPDATED)).toBe('Partiellement éliminée');
    expect(pipe.transform(AccessionRegisterStatus.STORED_AND_COMPLETED)).toBe('En stock et complète');
    expect(pipe.transform(AccessionRegisterStatus.UNSTORED)).toBe('Totalement éliminée');
  });
});
