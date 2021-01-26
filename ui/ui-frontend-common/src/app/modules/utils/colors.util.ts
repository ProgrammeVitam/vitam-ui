const DARK_SUFFIX = '-dark';
const LIGHT_SUFFIX = '-light';
const LIGHTEN_PATTERN = /-light-(\d*)/gm;
const DARKEN_PATTERN = /-dark-(\d*)/gm;

class RGB {
  constructor(public r: number, public g: number, public b: number) { }
}

class HSL {
  constructor(public h: number, public s: number, public l: number) { }
}

/**
 * Find, compute or return default value for the given name and maps of colors.
 * @param name the color name
 * @param defaultMap the default color map if no overriding in priority and fallback maps
 * @param fallbackMap the fallback map. The function will search in it if no color is found in priority map. Should be application config
 * @param priorityMap the priority map. If the color is found in it, the fallbackMap is not used. Should be customer config
 * @return The hex RBG color find or computed from all sources
 */
export function getColorFromMaps(name: string, defaultMap: any, fallbackMap: any, priorityMap: any): string {

  const customColor = getColorFromMap(name, priorityMap);

  if ( customColor ) {
    return customColor;
  }


  const applicationColor = getColorFromMap(name, fallbackMap);
  if ( applicationColor ) {
    return applicationColor;
  }

  return getColorFromMap(name, defaultMap);
}

function getColorFromMap(colorName: string, colorMap: any) {
  if (!colorMap) { return null; }

  if (colorMap[colorName]) {
    return colorMap[colorName];
  }

  if ( colorName.endsWith(DARK_SUFFIX) && colorMap[colorName.substring(0, colorName.length - DARK_SUFFIX.length)] ) {
    return convertToDarkColor(colorMap[colorName.substring(0, colorName.length - DARK_SUFFIX.length)]);
  }

  if ( colorName.endsWith(LIGHT_SUFFIX) && colorMap[colorName.substring(0, colorName.length - LIGHT_SUFFIX.length)] ) {
    return convertToLightColor(colorMap[colorName.substring(0, colorName.length - LIGHT_SUFFIX.length)]);
  }

  LIGHTEN_PATTERN.lastIndex = 0;
  let match = LIGHTEN_PATTERN.exec(colorName);
  if ( match && match.length === 2 && colorMap[colorName.substring(0, colorName.length - match[0].length)] ) {
    return convertToLightColor(colorMap[colorName.substring(0, colorName.length - match[0].length)], +match[1]);
  }

  DARKEN_PATTERN.lastIndex = 0;
  match = DARKEN_PATTERN.exec(colorName);
  if ( match && match.length === 2 && colorMap[colorName.substring(0, colorName.length - match[0].length)] ) {
    return convertToDarkColor(colorMap[colorName.substring(0, colorName.length - match[0].length)], +match[1]);
  }

  DARKEN_PATTERN.lastIndex = 0;
  match = DARKEN_PATTERN.exec(colorName);
  if ( match && match.length === 2 && colorMap[colorName.substring(0, colorName.length - match[0].length)] ) {
    return convertToDarkColor(colorMap[colorName.substring(0, colorName.length - match[0].length)], +match[1]);
  }

  return null;
}

/**
 * Apply a +X to the color lightness.
 * @param color the color to lighten. Must be hex color with #fff or #ffffff format
 * @param lightModificator the value of lighten operation. Default value to 10 if not set
 */
function convertToLightColor(color: string, lightModificator: number = 10) {
  if (!color) {
    return color;
  }

  const rgbValue: RGB = hexToRgb(color);
  const hslValue: HSL = rgbToHsl(rgbValue);

  // lighten
  hslValue.l = Math.min(hslValue.l + lightModificator, 100);
  const lightRGBvalue: RGB = hslToRgb(hslValue);

  return '#' + toHex(lightRGBvalue.r) + toHex(lightRGBvalue.g) + toHex(lightRGBvalue.b);
}

export function convertLighten(rgbValue: RGB, lightModificator: number) {
  const hslValue: HSL = rgbToHsl(rgbValue);

  // lighten
  hslValue.l = hslValue.l + lightModificator;
  if (hslValue.l > 100) {
    hslValue.l = 100;
  } else if (hslValue.l < 0 ) {
    hslValue.l = 0;
  }
  const lightRGBvalue: RGB = hslToRgb(hslValue);

  return '#' + toHex(lightRGBvalue.r) + toHex(lightRGBvalue.g) + toHex(lightRGBvalue.b);
}

/**
 * Apply a -X to the color lightness.
 * @param color the color to darken. Must be hex color with #fff or #ffffff format
 * @param lightModificator the value of darken  operation. Default value to 10 if not set
 */
export function convertToDarkColor(color: string, lightModificator: number = 10) {
  if (!color) {
    return color;
  }

  const rgbValue: RGB = hexToRgb(color);
  const hslValue: HSL = rgbToHsl(rgbValue);

  // darken
  hslValue.l = Math.max(hslValue.l - lightModificator, 0);
  const darkRGBvalue: RGB = hslToRgb(hslValue);

  return '#' + toHex(darkRGBvalue.r) + toHex(darkRGBvalue.g) + toHex(darkRGBvalue.b);
}

function toHex(componentValue: number) {
  const hex = componentValue.toString(16);
  return hex.length === 1 ? '0' + hex : hex;
}

export function hexToRgb(hex): RGB {
  const shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
  hex = hex.replace(shorthandRegex, (m, r, g, b) =>  r + r + g + g + b + b );

  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result ?
    new RGB(parseInt(result[1], 16), parseInt(result[2], 16), parseInt(result[3], 16)) :
    null;
}

export function hexToRgbString(hex) {
  const rgb = hexToRgb(hex);
  return rgb.r + ', ' + rgb.g + ', ' + rgb.b;
}

function hslToRgb(inputHSL): RGB {
  const hsl: HSL = new HSL( inputHSL.h * 360, inputHSL.s / 100, inputHSL.l / 100);
  let rgb: RGB;

  const c = (1 - Math.abs(2 * hsl.l - 1)) * hsl.s;
  const x = c * (1 - Math.abs((hsl.h / 60) % 2 - 1));
  const m = hsl.l - c / 2;

  if (hsl.h < 60) {
    rgb = new RGB(c, x, 0);
  } else if (hsl.h < 120) {
    rgb = new RGB(x, c, 0);
  } else if (hsl.h < 180) {
    rgb = new RGB(0, c, x);
  } else if (hsl.h < 240) {
    rgb = new RGB(0, x, c);
  } else if (hsl.h < 300) {
    rgb = new RGB(x, 0, c);
  } else if (hsl.h < 360) {
    rgb = new RGB(c, 0, x);
  }

  rgb.r = Math.round((rgb.r + m) * 255);
  rgb.g = Math.round((rgb.g + m) * 255);
  rgb.b = Math.round((rgb.b + m) * 255);

  return rgb;

}

export function rgbToHsl(inputRGB: RGB): HSL {
  const rgb: RGB = new RGB(inputRGB.r / 255, inputRGB.g / 255, inputRGB.b / 255);
  const hsl: HSL = new HSL(0, 0, 0);

  const max = Math.max(rgb.r, rgb.g, rgb.b);
  const min = Math.min(rgb.r, rgb.g, rgb.b);
  hsl.l = (max + min) / 2;

  // if min = max: achromatic, h = s = 0 => Nothing to update
  if ( max !== min ) {
    const d = max - min;
    hsl.s = hsl.l > 0.5 ? d / (2 - max - min) : d / (max + min);
    switch (max) {
      case rgb.r: hsl.h = (rgb.g - rgb.b) / d + (rgb.g < rgb.b ? 6 : 0); break;
      case rgb.g: hsl.h = (rgb.b - rgb.r) / d + 2; break;
      case rgb.b: hsl.h = (rgb.r - rgb.g) / d + 4; break;
    }
    hsl.h /= 6;
  }

  hsl.s = hsl.s * 100;
  hsl.s = Math.round(hsl.s);
  hsl.l = hsl.l * 100;
  hsl.l = Math.round(hsl.l);

  return hsl;
}
