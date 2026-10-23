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
import { rxResource } from '@angular/core/rxjs-interop';
import {
  FieldContext,
  LogicFn,
  metadata,
  PathKind,
  REQUIRED,
  requiredError,
  SchemaPath,
  SchemaPathRules,
  TreeValidationResult,
  validate,
  validateAsync,
} from '@angular/forms/signals';
import { Observable, switchMap, timer } from 'rxjs';
import { RULE_ID_PATTERN_ERROR_KEY } from '../validators/management-rule.validators';

type FieldPath<TValue, TPathKind extends PathKind> = SchemaPath<TValue, SchemaPathRules.Supported, TPathKind>;

const ALLOWED_IDENTIFIER_REGEX = /^[a-zA-Z0-9+=@_-]*$/;
const RULE_ID_FORBIDDEN_CHARACTERS_REGEX = /[À-ÖØ-öø-ÿ ]/;

function isEmptyValue(value: unknown): boolean {
  return value == null || ((typeof value === 'string' || Array.isArray(value)) && value.length === 0);
}

/**
 * Same as signal forms `required`, but with reactive forms `Validators.required` semantics:
 * `false` is a valid value, and an empty array is an empty value.
 */
export function requiredValue<TValue, TPathKind extends PathKind = PathKind.Root>(
  path: FieldPath<TValue, TPathKind>,
  config?: { when?: NoInfer<LogicFn<TValue, boolean, TPathKind>> },
): void {
  metadata(path, REQUIRED, (ctx) => (config?.when ? config.when(ctx) : true));
  validate(path, (ctx) => (ctx.state.required() && isEmptyValue(ctx.value()) ? requiredError() : undefined));
}

/** Signal forms version of `MiscValidators.requiredNotBlank`: the field is required and cannot contain only whitespaces. */
export function requiredNotBlank<TPathKind extends PathKind = PathKind.Root>(path: FieldPath<string, TPathKind>): void {
  metadata(path, REQUIRED, () => true);
  validate(path, ({ value }) => (value() == null || value().trim().length === 0 ? { kind: 'requiredNotBlank' } : undefined));
}

/** Signal forms version of `MiscValidators.allowedIdentifier`. */
export function allowedIdentifier<TPathKind extends PathKind = PathKind.Root>(path: FieldPath<string, TPathKind>): void {
  validate(path, ({ value }) => (!value() || ALLOWED_IDENTIFIER_REGEX.test(value()) ? undefined : { kind: 'incorrectIdentifier' }));
}

/** Signal forms version of `ManagementRuleValidators.ruleIdPattern`. */
export function ruleIdPattern<TPathKind extends PathKind = PathKind.Root>(path: FieldPath<string, TPathKind>): void {
  validate(path, ({ value }) => (RULE_ID_FORBIDDEN_CHARACTERS_REGEX.test(value()) ? { kind: RULE_ID_PATTERN_ERROR_KEY } : undefined));
}

export interface ObservableValidatorOptions<TValue, TResult, TPathKind extends PathKind> {
  /** Called with the field value, once synchronous validators pass and after the debounce time. */
  readonly stream: (value: TValue) => Observable<TResult>;
  /** Maps the stream result to validation errors. */
  readonly onSuccess: (result: TResult, ctx: FieldContext<TValue, TPathKind>) => TreeValidationResult;
  /** Delay (ms) before calling the stream, cancelled when the value changes. Defaults to 400ms. */
  readonly debounceTime?: number;
}

/**
 * Adds an async validator based on an `Observable` (typically a backend call checking that a value does not already exist).
 * Errors of the stream are ignored. The field is `pending` while the validation runs.
 */
export function validateObservable<TValue, TResult, TPathKind extends PathKind = PathKind.Root>(
  path: FieldPath<TValue, TPathKind>,
  options: ObservableValidatorOptions<TValue, TResult, TPathKind>,
): void {
  validateAsync(path, {
    params: ({ value }) => ({ value: value() }),
    factory: (params) =>
      rxResource({
        params,
        stream: ({ params: { value } }) => timer(options.debounceTime ?? 400).pipe(switchMap(() => options.stream(value))),
      }),
    onSuccess: options.onSuccess,
    onError: () => undefined,
  });
}
