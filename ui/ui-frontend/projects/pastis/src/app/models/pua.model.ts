
import {PuaDefinitions} from './pua-definitions.model';
import { PuaProperties } from './pua.propreties.model';

export class PUA {
    '$schema': string;
    'type': 'object';
    'additionalProperties': boolean;
    'definitions': PuaDefinitions;
    'properties': PuaProperties;
  }
