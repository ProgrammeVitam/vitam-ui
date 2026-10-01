// Diagnose MongoDB logical session cache growth (bug #15294).
//
// Symptom this script investigates:
//   "Unable to add session ID ... into the cache because the number of active
//    sessions is too high"
//   appearing after days or weeks of uptime and only cleared by a mongod restart.
//
// The cache of active logical sessions is held in memory by mongod. It is
// trimmed by a background job that persists sessions into config.system.sessions
// and lets a TTL index on "lastUse" expire them. If that job cannot run, nothing
// is ever reaped, the cache grows monotonically until it reaches maxSessions,
// and restarting the process is the only way to empty it.
//
// The job picks its implementation from the node role:
//   - plain replica set  -> the primary creates config.system.sessions itself
//   - clusterRole shardsvr -> the collection is expected to come from a mongos
//                             and the config servers, which VitamUI does not deploy
//
// This script is read-only and safe to run on a production primary.
//
// Usage. The reaper only runs on the primary, and replicaSet= is what takes the
// connection there whichever member is used to seed it:
//   mongosh "mongodb://<host>:<port>/admin?replicaSet=<rs>" \
//       --username <admin> --password \
//       --quiet --file deployment/scripts/diagnose_mongo_logical_sessions.js
//
// That same routing makes this URI useless to compare members against one
// another: it would read the primary every time, whatever host is named. Each
// member has to be reached by a direct connection instead:
//   mongosh "mongodb://<member>:<port>/admin?directConnection=true" \
//       --username <admin> --password \
//       --quiet --file deployment/scripts/diagnose_mongo_logical_sessions.js
//
// --password with no value makes mongosh prompt for it, which keeps the
// password out of the shell history and of the process arguments every other
// user of the machine can read.
//
// Run it twice a few minutes apart: activeSessionsCount rising while
// sessionsCollectionJobCount stays flat is the signature of a stalled reaper.

const SESSIONS_NAMESPACE = "config.system.sessions";
const TTL_INDEX_KEY_FIELD = "lastUse";

const findings = [];

function heading(title) {
    print("");
    print("=== " + title + " ===");
}

function report(label, value) {
    print("  " + label + ": " + value);
}

// Every probe is optional: a restricted account or an older server must degrade
// into a partial report rather than abort the whole diagnosis.
//
// Getting there takes both branches below. mongosh rewrites the shell API calls
// made inside fn into awaited ones and awaits the call to probe() itself, but it
// does not await fn() here: a denied command therefore came back as a rejected
// promise, long after the try block had been left, and killed the whole script
// instead of printing one unavailable line. Attaching the handler to the promise
// is what actually catches it. The try/catch is kept for whatever throws
// synchronously, and an explicit await cannot be used instead: mongosh parses
// the file as a plain script, where top level await is a syntax error.
//
// A refused command is not an error either: db.runCommand() hands back the raw
// response document, {ok: 0, errmsg: "not authorized on admin..."}, without
// throwing and without rejecting. Left alone that reads as a successful probe
// whose every field happens to be missing, which is how an unreadable node ends
// up reported as a healthy plain replica set. Anything carrying an "ok" other
// than 1 is therefore turned into an unavailable probe. Collection helpers and
// the wrappers below return arrays, numbers or plain objects with no "ok" field,
// so they go through untouched.
function probe(label, fn) {
    const unavailable = (error) => {
        print("  ! " + label + " unavailable: " + error.message);
        return null;
    };

    const checkCommandOk = (result) => {
        if (result && typeof result === "object" && result.ok !== undefined && result.ok !== 1) {
            return unavailable(
                new Error(result.errmsg || result.codeName || "command failed (ok=" + result.ok + ")")
            );
        }
        return result;
    };

    try {
        const result = fn();
        return result && typeof result.then === "function"
            ? result.then(checkCommandOk, unavailable)
            : checkCommandOk(result);
    } catch (error) {
        return unavailable(error);
    }
}

// getParameter answers {parameterName: value, ok: 1}, so the value can only be
// read once the probe has confirmed the command itself succeeded. Reading it
// inside fn instead would hand back undefined on a refusal, which passes every
// "!== null" guard downstream and turns into NaN a few lines later.
function probeField(label, command, field) {
    const response = probe(label, () => db.getSiblingDB("admin").runCommand(command));
    return response ? response[field] : null;
}

function fail(message) {
    findings.push({ level: "PROBLEM", message: message });
}

function warn(message) {
    findings.push({ level: "WARNING", message: message });
}

print("MongoDB logical session diagnosis - " + new Date().toISOString());

// ---------------------------------------------------------------------------
// 1. Topology: which SessionsCollection implementation does this node use?
// ---------------------------------------------------------------------------

heading("Topology");

const adminDb = db.getSiblingDB("admin");

const hello = probe("hello", () => adminDb.runCommand({ hello: 1 }));
if (hello) {
    report("version", probeField("buildInfo", { buildInfo: 1 }, "version") || "(unknown)");
    report("replica set", hello.setName || "(none)");
    report("is primary", hello.isWritablePrimary === true);
    if (hello.isWritablePrimary !== true) {
        warn(
            "Not connected to the primary. The session reaper only runs there, " +
                "so re-run this script against the primary before drawing conclusions."
        );
    }
} else {
    // Silence here would leave the whole topology section blank, which reads far
    // too much like a node with nothing to report.
    warn(
        "hello could not be read, so neither the replica set this node belongs to " +
            "nor its primary state is known. Every conclusion below is drawn without " +
            "knowing whether the reaper is even expected to run here."
    );
}

// probe() returns null on a command error, and an absent sharding section reads
// as "no cluster role" too. Printing the same thing for both would let a node
// nobody could read pass for a healthy plain replica set, which is the one
// conclusion this script must never reach by accident.
const commandLine = probe("getCmdLineOpts", () => adminDb.runCommand({ getCmdLineOpts: 1 }));
const clusterRoleKnown = commandLine !== null;
const clusterRole =
    clusterRoleKnown && commandLine.parsed && commandLine.parsed.sharding
        ? commandLine.parsed.sharding.clusterRole
        : null;
report("clusterRole", clusterRoleKnown ? clusterRole || "(none - plain replica set)" : "(unknown)");
if (!clusterRoleKnown) {
    warn(
        "getCmdLineOpts could not be read, so the cluster role is unknown and the " +
            "configuration behind this bug cannot be ruled out from here. Check " +
            "sharding.clusterRole in mongod.conf by hand."
    );
}

// findOne() legitimately answers null when the document is absent, so the lookup
// is wrapped: a null wrapper means the probe itself failed, a null document
// means the node is clean.
const shardIdentityLookup = probe("shardIdentity lookup", () => {
    return { document: adminDb.system.version.findOne({ _id: "shardIdentity" }) };
});
const shardIdentity = shardIdentityLookup ? shardIdentityLookup.document : null;
report(
    "shardIdentity document",
    shardIdentityLookup ? (shardIdentity ? "present" : "absent") : "(unknown)"
);

if (clusterRole === "shardsvr") {
    fail(
        "clusterRole is 'shardsvr'. On a shard server mongod does NOT create " +
            SESSIONS_NAMESPACE +
            " itself; it expects a mongos and config servers to have done it. " +
            "VitamUI deploys neither (the inventory only has hosts_vitamui_mongod), " +
            "so the session reaper cannot run and the cache never shrinks. " +
            "Fix: remove the 'sharding: clusterRole: shardsvr' block from " +
            "deployment/roles/mongo/templates/mongod.conf.j2 and restart the nodes " +
            "(secondaries first, then step down the primary)."
    );
    if (shardIdentity) {
        warn(
            "A shardIdentity document exists in admin.system.version. It must be " +
                "removed before restarting without clusterRole, otherwise mongod " +
                "will refuse to start."
        );
    }
}

// ---------------------------------------------------------------------------
// 2. Is the reaper actually running?
// ---------------------------------------------------------------------------

heading("Logical session record cache");

const serverStatus = probe("serverStatus", () => adminDb.runCommand({ serverStatus: 1 }));
const recordCache = serverStatus ? serverStatus.logicalSessionRecordCache : null;

// The refresh job only fires every logicalSessionRefreshMillis, and its first
// useful cycle happens after the node has been elected. A node that just
// restarted legitimately shows no sessions collection and no refreshed entry,
// so hold back the corresponding conclusions until a couple of cycles elapsed.
const refreshMillisProbe = probeField(
    "logicalSessionRefreshMillis",
    { getParameter: 1, logicalSessionRefreshMillis: 1 },
    "logicalSessionRefreshMillis"
);
// Every timing conclusion below is measured against this interval. A server
// started with a longer one than the 5 minute default would make them all wrong,
// so an assumed value is reported as such and never concludes on its own.
const refreshMillisKnown = typeof refreshMillisProbe === "number";
const refreshMillis = refreshMillisKnown ? refreshMillisProbe : 300000;
const uptimeSeconds = serverStatus ? serverStatus.uptime : 0;
const elapsedCycles = uptimeSeconds / (refreshMillis / 1000);
const settled = elapsedCycles >= 2;

report(
    "refresh interval (minutes)",
    (refreshMillis / 60000).toFixed(1) + (refreshMillisKnown ? "" : " (assumed, not read)")
);
report("uptime (minutes)", (uptimeSeconds / 60).toFixed(1));
report("refresh cycles elapsed", elapsedCycles.toFixed(1));
if (!settled) {
    print(
        "  ! This node restarted less than two refresh cycles ago; findings below " +
            "are inconclusive. Re-run in " +
            Math.ceil((2 - elapsedCycles) * (refreshMillis / 60000)) +
            " minutes."
    );
}

// Below the settling threshold a missing collection or an idle job proves
// nothing, so the same evidence is reported as a warning instead of a problem.
const conclude = settled ? fail : warn;

if (recordCache) {
    report("activeSessionsCount", recordCache.activeSessionsCount);
    report("sessionsCollectionJobCount", recordCache.sessionsCollectionJobCount);
    report("lastSessionsCollectionJobTimestamp", recordCache.lastSessionsCollectionJobTimestamp);
    report("lastSessionsCollectionJobDurationMillis", recordCache.lastSessionsCollectionJobDurationMillis);
    report("lastSessionsCollectionJobEntriesRefreshed", recordCache.lastSessionsCollectionJobEntriesRefreshed);
    report("lastSessionsCollectionJobEntriesEnded", recordCache.lastSessionsCollectionJobEntriesEnded);
    report("lastTransactionReaperJobTimestamp", recordCache.lastTransactionReaperJobTimestamp);

    const lastJob = recordCache.lastSessionsCollectionJobTimestamp;
    if (lastJob) {
        // The refresh job runs every logicalSessionRefreshMillis, read above
        // rather than assumed: missing three cycles in a row means it is
        // erroring out every time, not merely running late.
        const refreshMinutes = refreshMillis / 60000;
        const staleMinutes = (Date.now() - new Date(lastJob).getTime()) / 60000;
        report("last job age (minutes)", staleMinutes.toFixed(1));
        if (staleMinutes > 3 * refreshMinutes) {
            const staleConclusion = refreshMillisKnown ? fail : warn;
            staleConclusion(
                "The sessions collection job last completed " +
                    staleMinutes.toFixed(0) +
                    " minutes ago; it should run every " +
                    refreshMinutes.toFixed(0) +
                    (refreshMillisKnown
                        ? " minutes. "
                        : " minutes, assumed since logicalSessionRefreshMillis could not be read. ") +
                    "Nothing is being reaped."
            );
        }
    }

    if (recordCache.sessionsCollectionJobCount === 0) {
        conclude("sessionsCollectionJobCount is 0: the reaper has never completed a cycle since startup.");
    }

    // The most discriminating signal, and the one a single sample cannot carry.
    // On a misconfigured node the job still ticks, so sessionsCollectionJobCount
    // and the timestamp both look healthy, but every cycle bails out before
    // touching a single record. A healthy but quiet node reads exactly the same
    // when its sessions were all opened since the last cycle, so this stays a
    // warning whatever the settling state: only the same reading with a higher
    // activeSessionsCount, one refresh cycle later, tells the two apart.
    if (
        recordCache.sessionsCollectionJobCount > 0 &&
        recordCache.activeSessionsCount > 0 &&
        recordCache.lastSessionsCollectionJobEntriesRefreshed === 0
    ) {
        warn(
            "The last sessions collection job refreshed no record " +
                "(lastSessionsCollectionJobEntriesRefreshed = 0) while " +
                recordCache.activeSessionsCount +
                " sessions are active. Re-run this script after a refresh cycle: " +
                "the same reading with a higher activeSessionsCount means the job " +
                "is running but doing nothing, so the cache can only grow."
        );
    }
} else if (serverStatus) {
    warn("logicalSessionRecordCache is not exposed by serverStatus on this node.");
} else {
    // Distinct from the case above on purpose: a server that does not expose the
    // section is a known shape, a server nobody could read is an open question.
    warn(
        "serverStatus could not be read, so the session cache counters, the uptime " +
            "and the open cursor metrics are all missing. Nothing below rules the " +
            "reaper out; re-run with an account allowed to run serverStatus on admin."
    );
}

const maxSessions = probeField("maxSessions", { getParameter: 1, maxSessions: 1 }, "maxSessions");
if (typeof maxSessions === "number") {
    report("maxSessions", maxSessions);
    if (recordCache && recordCache.activeSessionsCount) {
        const usage = (recordCache.activeSessionsCount / maxSessions) * 100;
        report("cache usage", usage.toFixed(2) + "%");
        if (usage > 50) {
            fail("The logical session cache is " + usage.toFixed(1) + "% full.");
        }
    }
}

// ---------------------------------------------------------------------------
// 3. The decisive check: does the TTL index exist?
// ---------------------------------------------------------------------------

heading("Sessions collection");

const configDb = db.getSiblingDB("config");

const collections = probe("listCollections", () =>
    configDb.getCollectionNames().filter((name) => name === "system.sessions")
);

if (collections !== null && collections.length === 0) {
    report("collection", "ABSENT");
    conclude(
        SESSIONS_NAMESPACE +
            " does not exist. Without it no session is ever persisted or expired, " +
            "so the in-memory cache can only grow."
    );
} else if (collections !== null) {
    report("collection", "present");

    const indexes = probe("getIndexes", () => configDb.system.sessions.getIndexes());
    if (indexes) {
        const ttlIndex = indexes.find(
            (index) => index.key && index.key[TTL_INDEX_KEY_FIELD] !== undefined && index.expireAfterSeconds !== undefined
        );
        if (ttlIndex) {
            report("TTL index", ttlIndex.name + " (expireAfterSeconds=" + ttlIndex.expireAfterSeconds + ")");
        } else {
            fail(
                "No TTL index on " +
                    SESSIONS_NAMESPACE +
                    "." +
                    TTL_INDEX_KEY_FIELD +
                    ". Persisted sessions are never expired."
            );
        }
    }

    // Reported last because it is the one line here that is merely nice to
    // have, and the one most likely to be refused: counting documents in
    // config.system.sessions needs privileges that the root role does not carry
    // (observed on 8.0.23). estimatedDocumentCount() reads collection metadata
    // instead of scanning, which matters once the collection holds millions of
    // documents.
    const documentCount = probe("estimatedDocumentCount", () =>
        configDb.system.sessions.estimatedDocumentCount()
    );
    if (documentCount !== null) {
        report("approximate document count", documentCount);
    }
}

const ttlMonitor = probeField("ttlMonitorEnabled", { getParameter: 1, ttlMonitorEnabled: 1 }, "ttlMonitorEnabled");
if (typeof ttlMonitor === "boolean") {
    report("ttlMonitorEnabled", ttlMonitor);
    if (ttlMonitor === false) {
        fail("The TTL monitor is disabled, so no TTL index expires anything.");
    }
}

// ---------------------------------------------------------------------------
// 4. Client-side counter-hypothesis: cursors pinning their session
// ---------------------------------------------------------------------------
//
// A cursor keeps its logical session alive for as long as it lives, and a change
// stream never ends on its own. If the reaper turns out to be healthy, look here
// instead. Only counts are printed, never the operations themselves.

heading("Open cursors");

if (serverStatus && serverStatus.metrics && serverStatus.metrics.cursor) {
    const cursor = serverStatus.metrics.cursor;
    report("open.total", cursor.open.total);
    report("open.pinned", cursor.open.pinned);
    report("open.noTimeout", cursor.open.noTimeout);
    report("timedOut (since startup)", cursor.timedOut);
}

const changeStreams = probe("currentOp", () => {
    const result = adminDb.aggregate([
        { $currentOp: { allUsers: true, idleCursors: true } },
        { $match: { "cursor.originatingCommand.pipeline.0.$changeStream": { $exists: true } } },
        { $group: { _id: "$ns", count: { $sum: 1 }, oldest: { $min: "$cursor.createdDate" } } }
    ]);
    return result.toArray();
});

if (changeStreams === null) {
    // Without this the section would print nothing at all, which reads exactly
    // like an absence of change streams.
    print("  Open change streams could not be listed: this hypothesis is left open.");
} else if (changeStreams.length === 0) {
    report("open change streams", 0);
} else {
    changeStreams.forEach((entry) => {
        report("change streams on " + entry._id, entry.count + " (oldest opened " + entry.oldest + ")");
    });
    warn(
        "Change streams are open. Each one pins a logical session for its whole " +
            "lifetime. Compare the count against the number of running application " +
            "instances: significantly more means they are being leaked."
    );
}

// ---------------------------------------------------------------------------
// 5. Verdict
// ---------------------------------------------------------------------------

heading("Verdict");

const problems = findings.filter((finding) => finding.level === "PROBLEM").length;

if (findings.length === 0) {
    print("  No anomaly detected. The session reaper looks healthy on this node.");
    print("  If sessions still accumulate, run this script against each member in");
    print("  turn with directConnection=true and compare activeSessionsCount: the");
    print("  replicaSet= form would report the primary every time.");
} else {
    report("problems", problems);
    report("warnings", findings.length - problems);
    print("");
    findings.forEach((finding, index) => {
        print("  [" + finding.level + " " + (index + 1) + "] " + finding.message);
        print("");
    });
}

// Exit status, so that a supervision job can consume the verdict without
// parsing the report. Warnings alone call for a second run of this script, not
// for an alert, hence only problems are counted.
quit(problems > 0 ? 1 : 0);
