import { AppendZIfNoTimezonePipe } from './append-z-if-no-timezone.pipe';

describe('AppendZIfNoTimezonePipe', () => {
  it('create an instance', () => {
    const pipe = new AppendZIfNoTimezonePipe();
    expect(pipe).toBeTruthy();
  });

  it('should return falsy values with no transformation', () => {
    const pipe = new AppendZIfNoTimezonePipe();
    expect(pipe.transform(null)).toBe(null);
    expect(pipe.transform(undefined)).toBe(undefined);
  });

  it('should transform a Date instance to a String expressed in Z timezone', () => {
    const pipe = new AppendZIfNoTimezonePipe();
    expect(pipe.transform(new Date('2024-10-24T14:02:36.000Z'))).toBe('2024-10-24T14:02:36.000Z');
    expect(pipe.transform(new Date('2024-10-24T14:02:36.000+00:00'))).toBe('2024-10-24T14:02:36.000Z');
    expect(pipe.transform(new Date('2024-10-24T16:02:36.000+02:00'))).toBe('2024-10-24T14:02:36.000Z');
    expect(pipe.transform(new Date('2024-10-24T12:02:36.000-02:00'))).toBe('2024-10-24T14:02:36.000Z');
    expect(pipe.transform(new Date('Thu Oct 24 2024 16:02:36 GMT+0200'))).toBe('2024-10-24T14:02:36.000Z');
    expect(pipe.transform(new Date('Thu Oct 24 2024 14:02:36 GMT+0000'))).toBe('2024-10-24T14:02:36.000Z');
    expect(pipe.transform(new Date('Thu Oct 24 2024 14:02:36 Z'))).toBe('2024-10-24T14:02:36.000Z');
  });

  it('should keep String as is if it already has a timezone', () => {
    const pipe = new AppendZIfNoTimezonePipe();
    expect(pipe.transform('2024-10-24T14:02:36.000Z')).toBe('2024-10-24T14:02:36.000Z');
    expect(pipe.transform('2024-10-24T14:02:36.000+00:00')).toBe('2024-10-24T14:02:36.000+00:00');
    expect(pipe.transform('2024-10-24T14:02:36.000+02:00')).toBe('2024-10-24T14:02:36.000+02:00');
    expect(pipe.transform('2024-10-24T14:02:36.000-02:00')).toBe('2024-10-24T14:02:36.000-02:00');
    expect(pipe.transform('Thu Oct 24 2024 16:02:36 GMT+0200')).toBe('Thu Oct 24 2024 16:02:36 GMT+0200');
    expect(pipe.transform('Thu Oct 24 2024 16:02:36 GMT-0200')).toBe('Thu Oct 24 2024 16:02:36 GMT-0200');
  });

  it('should append "Z" timezone to string if they have no timezone', () => {
    const pipe = new AppendZIfNoTimezonePipe();
    expect(pipe.transform('2024-10-24T14:02:36.000')).toBe('2024-10-24T14:02:36.000Z');
  });
});
