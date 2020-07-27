import { getColorFromMaps } from './colors.util';

describe('Colors', () => {

  const defaultMap = {
    defaultColor: '#123',
    appColor: '#123',
    bothColor: '#123'
  };

  const appColorMap = {
    appColor: '#234',
    bothColor: '#ABC'
  };
  const customerColorMap = {
    bothColor: '#DEF',
    custom: '#5cbaa9'
  };

  it('should return null if the color name does not exist', () => {
    expect(getColorFromMaps('this-is-not-a-valid-key', defaultMap, appColorMap, customerColorMap)).toBeNull();
  });

  it('should take default value if no app/custom defined', () => {
    expect(getColorFromMaps('defaultColor', defaultMap, appColorMap, customerColorMap)).toEqual('#123');
  });

  it('should take applicationColor if defined', () => {
    expect(getColorFromMaps('appColor', defaultMap, appColorMap, customerColorMap)).toEqual('#234');
  });

  it('should take customColor if both are defined', () => {
    expect(getColorFromMaps('bothColor', defaultMap, appColorMap, customerColorMap)).toEqual('#DEF');
  });

  it('should compute lighten from app color', () => {
    expect(getColorFromMaps('appColor-light', defaultMap, appColorMap, customerColorMap)).toEqual('#334d66');
  });

  it('should compute darken from custom color', () => {
    expect(getColorFromMaps('bothColor-dark', defaultMap, appColorMap, customerColorMap)).toEqual('#a8d4ff');
  });

  it('should compute darker color', () => {
    expect(getColorFromMaps('appColor-light-50', defaultMap, appColorMap, customerColorMap)).toEqual('#99b3cc');
  });

  it('should not overflow white color', () => {
    expect(getColorFromMaps('appColor-light-100', defaultMap, appColorMap, customerColorMap)).toEqual('#ffffff');
  });

  it('should not overflow black color', () => {
    expect(getColorFromMaps('appColor-dark-100', defaultMap, appColorMap, customerColorMap)).toEqual('#000000');
  });

});
