/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import {
  convertToDarkColor,
  convertToLightColor,
  getColorFromMaps,
  hexToRgb,
  hexToRgbString,
  rgbToHsl,
  setLuminosity,
  toHex,
} from './colors.util';

describe('Colors', () => {
  const defaultMap = {
    defaultColor: '#9E9E9E',
    appColor: '#9E9E9E',
    bothColor: '#9E9E9E',
  };
  const appColorMap = {
    appColor: '#9C31B5',
    bothColor: '#9C31B5',
  };
  const customerColorMap = {
    bothColor: '#296EBC',
  };

  it('should return null if the color name does not exist', () => {
    expect(getColorFromMaps('this-is-not-a-valid-key', defaultMap, appColorMap, customerColorMap)).toBeNull();
  });

  it('should take default value if no app/custom defined', () => {
    expect(getColorFromMaps('defaultColor', defaultMap, appColorMap, customerColorMap)).toEqual('#9E9E9E');
  });

  it('should take applicationColor if defined', () => {
    expect(getColorFromMaps('appColor', defaultMap, appColorMap, customerColorMap)).toEqual('#9C31B5');
  });

  it('should take customColor if both are defined', () => {
    expect(getColorFromMaps('bothColor', defaultMap, appColorMap, customerColorMap)).toEqual('#296EBC');
  });

  it('should fall back to the application then default maps when the priority/fallback maps are missing', () => {
    expect(getColorFromMaps('appColor', defaultMap, appColorMap, null)).toEqual('#9C31B5');
    expect(getColorFromMaps('appColor', defaultMap, undefined, null)).toEqual('#9E9E9E');
    expect(getColorFromMaps('appColor', null, undefined, null)).toBeNull();
  });

  describe('toHex', () => {
    it('should pad single digit values with a leading zero', () => {
      expect(toHex(0)).toEqual('00');
      expect(toHex(5)).toEqual('05');
      expect(toHex(15)).toEqual('0f');
    });

    it('should not pad two digit values', () => {
      expect(toHex(16)).toEqual('10');
      expect(toHex(255)).toEqual('ff');
    });
  });

  describe('hexToRgb', () => {
    it('should parse a 6 digit hex color, with or without #, case insensitive', () => {
      expect(hexToRgb('#1a2B3c')).toEqual({ r: 26, g: 43, b: 60 });
      expect(hexToRgb('FF8000')).toEqual({ r: 255, g: 128, b: 0 });
    });

    it('should expand the 3 digit shorthand notation', () => {
      expect(hexToRgb('#fff')).toEqual({ r: 255, g: 255, b: 255 });
      expect(hexToRgb('a0c')).toEqual({ r: 170, g: 0, b: 204 });
    });

    it('should return null for invalid colors', () => {
      expect(hexToRgb('#ggg')).toBeNull();
      expect(hexToRgb('#12345')).toBeNull();
      expect(hexToRgb('red')).toBeNull();
      expect(hexToRgb('')).toBeNull();
    });
  });

  describe('hexToRgbString', () => {
    it('should format the rgb components as a comma separated string', () => {
      expect(hexToRgbString('#ff8000')).toEqual('255, 128, 0');
      expect(hexToRgbString('#000')).toEqual('0, 0, 0');
    });
  });

  describe('rgbToHsl', () => {
    it('should convert achromatic colors with a zero hue and saturation', () => {
      expect(rgbToHsl({ r: 255, g: 255, b: 255 })).toEqual({ h: 0, s: 0, l: 100 });
      expect(rgbToHsl({ r: 0, g: 0, b: 0 })).toEqual({ h: 0, s: 0, l: 0 });
      expect(rgbToHsl({ r: 128, g: 128, b: 128 })).toEqual({ h: 0, s: 0, l: 50 });
    });

    it('should compute the hue as a fraction of the color wheel depending on the dominant component', () => {
      expect(rgbToHsl({ r: 255, g: 0, b: 0 })).toEqual({ h: 0, s: 100, l: 50 });
      expect(rgbToHsl({ r: 0, g: 255, b: 0 }).h).toBeCloseTo(1 / 3);
      expect(rgbToHsl({ r: 0, g: 0, b: 255 }).h).toBeCloseTo(2 / 3);
      expect(rgbToHsl({ r: 255, g: 0, b: 255 }).h).toBeCloseTo(5 / 6);
    });

    it('should use the light formula for saturation when lightness is above 50%', () => {
      expect(rgbToHsl({ r: 170, g: 187, b: 204 })).toEqual({ h: expect.closeTo(7 / 12), s: 25, l: 73 });
      expect(rgbToHsl({ r: 51, g: 102, b: 153 })).toEqual({ h: expect.closeTo(7 / 12), s: 50, l: 40 });
    });
  });

  describe('setLuminosity', () => {
    it('should return the input as is when no color is given', () => {
      expect(setLuminosity('', 50)).toEqual('');
      expect(setLuminosity(null, 50)).toBeNull();
      expect(setLuminosity(undefined, 50)).toBeUndefined();
    });

    it('should keep pure colors unchanged at 50% luminosity for every hue sector', () => {
      ['#ff0000', '#ffff00', '#00ff00', '#00ffff', '#0000ff', '#ff00ff'].forEach((color) => {
        expect(setLuminosity(color, 50)).toEqual(color);
      });
    });

    it('should set the requested luminosity', () => {
      expect(setLuminosity('#ff0000', 25)).toEqual('#800000');
      expect(setLuminosity('#00ffff', 25)).toEqual('#008080');
      expect(setLuminosity('#336699', 50)).toEqual('#4080bf');
    });

    it('should clamp the luminosity between 0 and 100', () => {
      expect(setLuminosity('#ff0000', 150)).toEqual('#ffffff');
      expect(setLuminosity('#ff0000', -5)).toEqual('#000000');
    });

    it('should accept the shorthand notation', () => {
      expect(setLuminosity('#f00', 25)).toEqual('#800000');
    });
  });

  describe('convertToLightColor', () => {
    it('should return the input as is when no color is given', () => {
      expect(convertToLightColor('')).toEqual('');
      expect(convertToLightColor(null)).toBeNull();
    });

    it('should increase the lightness by 10 by default', () => {
      expect(convertToLightColor('#000000')).toEqual('#1a1a1a');
      expect(convertToLightColor('#ff0000')).toEqual('#ff3333');
    });

    it('should increase the lightness by the given amount and clamp to white', () => {
      expect(convertToLightColor('#000000', 50)).toEqual('#808080');
      expect(convertToLightColor('#ff0000', 100)).toEqual('#ffffff');
    });
  });

  describe('convertToDarkColor', () => {
    it('should decrease the lightness by 10 by default', () => {
      expect(convertToDarkColor('#ff0000')).toEqual('#cc0000');
      expect(convertToDarkColor('#0000ff')).toEqual('#0000cc');
    });

    it('should decrease the lightness by the given amount and clamp to black', () => {
      expect(convertToDarkColor('#ffffff', 20)).toEqual('#cccccc');
      expect(convertToDarkColor('#ff0000', 100)).toEqual('#000000');
    });

    it('should return the input as is when no color is given', () => {
      expect(convertToDarkColor(null)).toBeNull();
    });
  });
});
