import { AbstractControl } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';
import { DisplayObject } from '../../object-viewer/models';
import { DisplayObjectType, EffectiveCardinality } from '../../object-viewer/types';

export interface Actions {
  [key: string]: Action;
}

export interface Action {
  name: string;
  label: string;
  handler: Function;
}

export interface EditObject extends DisplayObject {
  control: AbstractControl;
  kind?: 'object' | 'object-array' | 'primitive-array' | 'primitive' | 'unknown';
  default?: any;
  actions?: Actions;
  children?: EditObject[];
  type?: DisplayObjectType;
  favoriteKeys?: string[];
  required?: boolean;
  virtual?: boolean;
  childrenChange: BehaviorSubject<EditObject[]>;
  pattern?: string;
  options?: string[];
  hint?: string;
  cardinality: EffectiveCardinality;
}
