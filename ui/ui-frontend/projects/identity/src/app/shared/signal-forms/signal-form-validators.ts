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
  PathKind,
  pattern,
  required,
  SchemaPath,
  SchemaPathRules,
  TreeValidationResult,
  validateAsync,
} from '@angular/forms/signals';
import { Observable, switchMap, timer } from 'rxjs';
import { isRootLevel, User } from 'vitamui-library';

export type FieldPath<TValue, TPathKind extends PathKind = PathKind.Root> = SchemaPath<TValue, SchemaPathRules.Supported, TPathKind>;

const LEVEL_PATTERN = /^[A-Z0-9]+(\.[A-Z0-9]+)*$/;

/** Signal forms version of `buildValidators(user)` (vitamui-library): level format, required unless the user is at root level. */
export function levelRules<TPathKind extends PathKind = PathKind.Root>(path: FieldPath<string, TPathKind>, user: User): void {
  pattern(path, LEVEL_PATTERN);
  if (!isRootLevel(user)) {
    required(path);
  }
}

export interface ObservableWithParamsValidatorOptions<TValue, TParams, TResult, TPathKind extends PathKind> {
  /** Computes the parameters of the check: the check is run again (after the debounce time) each time they change. */
  readonly params: (ctx: FieldContext<TValue, TPathKind>) => TParams;
  /** Called with the parameters, once synchronous validators pass and after the debounce time. */
  readonly stream: (params: TParams) => Observable<TResult>;
  /** Maps the stream result to validation errors. */
  readonly onSuccess: (result: TResult, ctx: FieldContext<TValue, TPathKind>) => TreeValidationResult;
  /** Delay (ms) before calling the stream, cancelled when the parameters change. Defaults to 400ms. */
  readonly debounceTime?: number;
}

/**
 * Same as `validateObservable` (vitamui-library), but the check depends on parameters computed from the field context
 * (for example the value of other fields) instead of the field value only.
 */
export function validateObservableWithParams<TValue, TParams, TResult, TPathKind extends PathKind = PathKind.Root>(
  path: FieldPath<TValue, TPathKind>,
  options: ObservableWithParamsValidatorOptions<TValue, TParams, TResult, TPathKind>,
): void {
  validateAsync(path, {
    params: (ctx) => ({ params: options.params(ctx) }),
    factory: (params) =>
      rxResource({
        params,
        stream: ({ params: { params: checkParams } }) =>
          timer(options.debounceTime ?? 400).pipe(switchMap(() => options.stream(checkParams))),
      }),
    onSuccess: options.onSuccess,
    onError: () => undefined,
  });
}
