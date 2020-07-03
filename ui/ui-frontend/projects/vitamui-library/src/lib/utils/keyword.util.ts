import {PositionType} from 'ui-frontend-common';
import {MetadataPermission} from '../models/metadata.interface';
import {Unit} from '../models/unit.interface';

export function getBooleanValue(unit: Unit, key: string): boolean {
  const value = getKeywordValue(unit, key);

  return value === 'true';
}

export function getPositionType(unit: Unit): PositionType {
  return getKeywordValue(unit, 'position_type') as PositionType;
}

export function getKeywordValue(unit: Unit, keywordReference: string): string {
  if (!unit.Keyword) {
    return null;
  }

  const keyword = unit.Keyword.find((k) => k.KeywordReference === keywordReference);

  return keyword ? keyword.KeywordContent as PositionType : null;
}

export function getMetadataPermission(unit: Unit): MetadataPermission[] {
  const serializedMetadata = getKeywordValue(unit, 'metadata');

  if (!serializedMetadata) {
    return [];
  }

  return JSON.parse(serializedMetadata);
}
