import { AbstractControl } from '@angular/forms';
import { DisplayObject } from '../../object-viewer/models';
import { DisplayObjectType } from '../../object-viewer/types';

export interface EditObject extends DisplayObject {
  control: AbstractControl;
  kind?: 'object' | 'object-array' | 'primitive-array' | 'primitive' | 'unknown';
  default?: any;
  actions?: any;
  children?: EditObject[];
  type?: DisplayObjectType;
  favoriteKeys?: string[];
}
