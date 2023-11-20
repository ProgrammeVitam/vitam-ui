import { PluralPipe } from './plural.pipe';

describe('PluralPipe', () => {
  it('create an instance', () => {
    const pipe = new PluralPipe();

    expect(pipe).toBeTruthy();
  });

  it('should add \'s\' after the word when count is greater than 1', () => {
    const pipe = new PluralPipe();
    const word = 'archive';
    const count = 2;
    const expectation = 'archives';
    const result = pipe.transform(word, count);

    expect(count).toBeGreaterThan(1);
    expect(result).toEqual(expectation);
  });

  it('should not add \'s\' after the word when count is lesser than 2', () => {
    const pipe = new PluralPipe();
    const word = 'archive';
    const count = 1;
    const expectation = 'archive';
    const result = pipe.transform(word, count);

    expect(count).toBeLessThan(2);
    expect(result).toEqual(expectation);
  });

  it('should not add \'s\' after the word when it already finished by \'s\'', () => {
    const pipe = new PluralPipe();
    const word = 'archives';
    const count = 2;
    const expectation = 'archives';
    const result = pipe.transform(word, count);

    expect(count).toBeGreaterThan(1);
    expect(result).toEqual(expectation);
    expect(word).toEqual(result);
  });
});
