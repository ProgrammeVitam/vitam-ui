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
// Usage:
//   mongosh "mongodb://<admin>:<password>@<host>:<port>/admin?replicaSet=<rs>" \
//       --quiet --file deployment/scripts/diagnose_mongo_logical_sessions.js
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
function probe(label, fn) {
    try {
        return fn();
    } catch (error) {
        print("  ! " + label + " unavailable: " + error.message);
        return null;
    }
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
    report("version", probe("version", () => adminDb.runCommand({ buildInfo: 1 }).version));
    report("replica set", hello.setName || "(none)");
    report("is primary", hello.isWritablePrimary === true);
    if (hello.isWritablePrimary !== true) {
        warn(
            "Not connected to the primary. The session reaper only runs there, " +
                "so re-run this script against the primary before drawing conclusions."
        );
    }
}

const commandLine = probe("getCmdLineOpts", () => adminDb.runCommand({ getCmdLineOpts: 1 }));
const clusterRole =
    commandLine && commandLine.parsed && commandLine.parsed.sharding
        ? commandLine.parsed.sharding.clusterRole
        : null;
report("clusterRole", clusterRole || "(none - plain replica set)");

const shardIdentity = probe("shardIdentity lookup", () =>
    adminDb.system.version.findOne({ _id: "shardIdentity" })
);
report("shardIdentity document", shardIdentity ? "present" : "absent");

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
const refreshMillis =
    probe(
        "logicalSessionRefreshMillis",
        () => adminDb.runCommand({ getParameter: 1, logicalSessionRefreshMillis: 1 }).logicalSessionRefreshMillis
    ) || 300000;
const uptimeSeconds = serverStatus ? serverStatus.uptime : 0;
const elapsedCycles = uptimeSeconds / (refreshMillis / 1000);
const settled = elapsedCycles >= 2;

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
        // The refresh job runs every logicalSessionRefreshMillis (5 minutes by
        // default). Anything much older means it is erroring out every cycle.
        const staleMinutes = (Date.now() - new Date(lastJob).getTime()) / 60000;
        report("last job age (minutes)", staleMinutes.toFixed(1));
        if (staleMinutes > 15) {
            fail(
                "The sessions collection job last completed " +
                    staleMinutes.toFixed(0) +
                    " minutes ago; it should run every 5 minutes. Nothing is being reaped."
            );
        }
    }

    if (recordCache.sessionsCollectionJobCount === 0) {
        conclude("sessionsCollectionJobCount is 0: the reaper has never completed a cycle since startup.");
    }

    // The most reliable signal. On a misconfigured node the job still ticks, so
    // sessionsCollectionJobCount and the timestamp both look healthy, but every
    // cycle bails out before touching a single record: entriesRefreshed stays at
    // 0 and the duration at 0ms while sessions are piling up in the cache.
    if (
        recordCache.sessionsCollectionJobCount > 0 &&
        recordCache.activeSessionsCount > 0 &&
        recordCache.lastSessionsCollectionJobEntriesRefreshed === 0
    ) {
        conclude(
            "The sessions collection job completes without refreshing any record " +
                "(lastSessionsCollectionJobEntriesRefreshed = 0) while " +
                recordCache.activeSessionsCount +
                " sessions are active. The job is running but doing nothing, " +
                "so the cache can only grow."
        );
    }
} else {
    warn("logicalSessionRecordCache is not exposed by serverStatus on this node.");
}

const maxSessions = probe(
    "maxSessions",
    () => adminDb.runCommand({ getParameter: 1, maxSessions: 1 }).maxSessions
);
if (maxSessions !== null) {
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

    // estimatedDocumentCount() reads collection metadata instead of scanning,
    // which matters if the collection has grown to millions of documents.
    report(
        "approximate document count",
        probe("estimatedDocumentCount", () => configDb.system.sessions.estimatedDocumentCount())
    );

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
}

const ttlMonitor = probe(
    "ttlMonitorEnabled",
    () => adminDb.runCommand({ getParameter: 1, ttlMonitorEnabled: 1 }).ttlMonitorEnabled
);
if (ttlMonitor !== null) {
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

if (changeStreams !== null) {
    if (changeStreams.length === 0) {
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
}

// ---------------------------------------------------------------------------
// 5. Verdict
// ---------------------------------------------------------------------------

heading("Verdict");

if (findings.length === 0) {
    print("  No anomaly detected. The session reaper looks healthy on this node.");
    print("  If sessions still accumulate, re-run this script on every replica set");
    print("  member and compare activeSessionsCount over time.");
} else {
    findings.forEach((finding, index) => {
        print("  [" + finding.level + " " + (index + 1) + "] " + finding.message);
        print("");
    });
}
