import { getColorFromMaps } from './colors.util';

describe('Colors', () => {

  const appColorMap = {
    appColor: '#234',
    bothColor: '#ABC'
  };
  const customerColorMap = {
    bothColor: '#DEF',
    custom: '#5cbaa9'
  };

  it('should take default value if no app/custom defined', () => {
    expect(getColorFromMaps('default-color', '#123', appColorMap, customerColorMap)).toEqual('#123');
  });

  it('should take applicationColor if defined', () => {
    expect(getColorFromMaps('appColor', '#123', appColorMap, customerColorMap)).toEqual('#234');
  });

  it('should take customColor if both are defined', () => {
    expect(getColorFromMaps('bothColor', '#123', appColorMap, customerColorMap)).toEqual('#DEF');
  });

  it('should compute lighten from app color', () => {
    expect(getColorFromMaps('appColor-light', '#123', appColorMap, customerColorMap)).toEqual('#334c66');
  });

  it('should compute darken from custom color', () => {
    expect(getColorFromMaps('bothColor-dark', '#123', appColorMap, customerColorMap)).toEqual('#a8d4ff');
  });

  it('should compute darker color', () => {
    expect(getColorFromMaps('appColor-light-50', '#123', appColorMap, customerColorMap)).toEqual('#99b2cc');
  });

  it('should not overflow white color', () => {
    expect(getColorFromMaps('appColor-light-100', '#123', appColorMap, customerColorMap)).toEqual('#ffffff');
  });

  it('should not overflow black color', () => {
    expect(getColorFromMaps('appColor-dark-100', '#123', appColorMap, customerColorMap)).toEqual('#000000');
  });

});
