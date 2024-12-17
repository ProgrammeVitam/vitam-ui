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
import { EditObject } from './edit-object.model';

export type EditObjectOperator = (oe: EditObject) => EditObject;
export type EditObjectPredicate = (oe: EditObject) => boolean;

export const setDisplayState = (editObject: EditObject, display: boolean): EditObject => {
  if (!editObject.displayRule) {
    console.warn(`Element '${editObject.path}' has no displayRule`);

    return editObject;
  }

  editObject.displayRule = { ...editObject.displayRule, ui: { ...editObject.displayRule.ui, display } };

  return editObject;
};

export const display = (editObject: EditObject): EditObject => setDisplayState(editObject, true);

export const hide = (editObject: EditObject): EditObject => setDisplayState(editObject, false);

export const setAccordionState = (editObject: EditObject, open: boolean): EditObject => {
  if (!editObject.displayRule) {
    console.warn(`Element '${editObject.path}' has no displayRule`);

    return editObject;
  }

  editObject.displayRule = { ...editObject.displayRule, ui: { ...editObject.displayRule.ui, open } };
  editObject.open = open;

  return editObject;
};

export const expand = (editObject: EditObject): EditObject => {
  return setAccordionState(editObject, true);
};

export const collapse = (editObject: EditObject): EditObject => {
  return setAccordionState(editObject, false);
};

export const label = (editObject: EditObject, label: string = editObject.displayRule.ui.label || editObject.key): EditObject => {
  editObject.displayRule.ui.label = label;

  return editObject;
};

export const sequence = (operators: EditObjectOperator[]): EditObjectOperator => {
  return (editObject: EditObject): EditObject => {
    return operators.reduce((currentObject, operator) => operator(currentObject), editObject);
  };
};

export const conditional = (
  predicate: EditObjectPredicate,
  operator: EditObjectOperator,
  provider: (editObject: EditObject) => EditObject = (obj) => obj,
): EditObjectOperator => {
  return (editObject: EditObject): EditObject => {
    const providedObject = provider(editObject);
    return predicate(providedObject) ? operator(providedObject) : providedObject;
  };
};

export const hasDisplayRule: EditObjectPredicate = (editObject: EditObject): boolean => {
  return Boolean(editObject?.displayRule);
};

export const hasCardinalityZero: EditObjectPredicate = (editObject: EditObject): boolean => {
  return editObject?.cardinality === 'ZERO';
};

export const hasInconsistentValue = (typeService: any): EditObjectPredicate => {
  return (editObject: EditObject): boolean => {
    return !typeService.isConsistent(editObject?.value);
  };
};

export const hasInPath =
  (prefix: string) =>
  (editObject: EditObject): boolean => {
    return editObject?.path.includes(prefix);
  };

export const hasNoLabel: EditObjectPredicate = (editObject: EditObject): boolean => {
  return Boolean(editObject?.displayRule?.ui?.label);
};

export const hasAllChildrenHidden = (editObject: EditObject): boolean => {
  return Boolean(editObject.children.length && !editObject.children.some((child) => child.displayRule.ui.display));
};
