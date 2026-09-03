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

/** Which lifecycle logbook (unit or object group) a raw event was fetched from. */
export type LifecycleOrigin = 'UA' | 'GOT';

/**
 * One event (or sub-event) of the consolidated unit/object-group lifecycle history, rebuilt into a tree
 * from the flat `evParentId` references returned by Vitam.
 */
export interface ConsolidatedLifecycleEvent {
  evId: string;
  evParentId: string;
  evType: string;
  evTypeProc: string;
  evIdProc: string;
  evDateTime: string;
  outcome: string;
  outDetail: string;
  outMessg: string;
  origin: LifecycleOrigin;
  /** Parsed JSON content of evDetData, or null if evDetData is empty or not valid JSON. */
  parsedDetail: unknown;
  /** Raw evDetData content, kept as a fallback when it cannot be parsed as JSON. */
  rawDetail: string;
  hasDetail: boolean;
  children: ConsolidatedLifecycleEvent[];
}

/** One operation (identified by evIdProc/evTypeProc) grouping the lifecycle events it triggered. */
export interface OperationLifecycleGroup {
  evTypeProc: string;
  evIdProc: string;
  /** Date used to sort operations, taken from their most recent triggering event. */
  date: string;
  events: ConsolidatedLifecycleEvent[];
}
