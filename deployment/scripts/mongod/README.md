# MongoDB Scripts for VitamUI

## Overview

These scripts handle initializing and updating the MongoDB databases for VitamUI.

To ensure safety and tracking, the mongo_init role evaluates files chronologically and registers completed scripts in the `versioning.changelog` collection. Once a script is logged there, it will never be executed again on that environment.

---

## Directory Structure & Naming Convention

Scripts must be placed in specific major/minor version subdirectories and follow a strict naming layout to guarantee they execute in the correct alphabetical order.

```text
└── vX.Y/                             <-- Target Major.Minor version directory (e.g., v1.0)
    └── Z-NN_your_description.js      <-- Migration script file
```

### Filename Breakdown

* **`Z`**: The bugfix version of the branch (`0` to `9`).
* **`NN`**: A sequential two-digit number for the script within that bugfix version (e.g., `01`, `02`).
* **`your_description`**: A short, snake_case description of what the script does.

> **Allowed Formats:** Standard JavaScript (`.js`) or Jinja2 templates (`.js.j2`). If you use `.js.j2`, the variables will be automatically compiled via Ansible's template engine into clean `.js` files before being sent to MongoDB.

---

## Script Execution & Backporting Rules

### Alphabetical Execution

Within each version directory, unexecuted scripts are processed strictly in alphabetical order.

### Backporting to Older Versions

If a fix needs to be backported, place the script in the targeted version folder of the **oldest** directory it applies to. The mongo_init role checks files globally against the `versioning.changelog` collection to ensure no target environment misses the update.

### Merging Scripts (Pre-Release)

You may merge multiple scripts (for example, combining multiple edits to the same collection to reduce execution overhead) **before an official release**. Once a script has been deployed to a customer or production environment, its history in `versioning.changelog` is registered.

See **Merging Scripts (Post-Release)** for more details if you are planning to merge scripts after an official release.

### Merging Scripts (Post-Release)

Combine the logic into a single file and keep the filename of the very first script in that sequence. Delete the subsequent script files.

* **Existing Environments**: The mongo_init role sees the old filename is already registered in `versioning.changelog` and safely skips it (which is fine, since those environments already have the state from the original separate runs).
* **New Environments**: The mongo_init role will execute the single combined script from scratch.

> **Warning**: Do not introduce new database modifications during a merge. Because existing environments will skip the reused filename, any brand-new logic added to that file will never be executed. New changes always require a new file (and hence a new sequence number).

---

## Preparing a Script

When writing your MongoDB script, you do not need to write connection boilerplate. The mongo_init role automatically exposes the following pre-defined database context variables:

* `dbAdmin`: The Admin database context
* `dbIam`: The IAM database context
* `dbSecurity`: The Security database context
* `dbCas`: The CAS database context
* `dbArchiveSearch`: The Archive Search database context
* `dbVersioning`: The Versioning database context

> **Code Style Note:** Always use **4 spaces for indentation** in both standard `.js` files and `.js.j2` templates to ensure formatting consistency across the repository. Do not use hard tabs.

### Example Usage

Use these global variables directly to execute your operations:

```javascript
// Example: Add the admin_system user
dbIam.users.updateOne(
    { "username": "admin_system" },
    {
        $set: {
            "status": "ACTIVE",
            "updatedAt": new Date()
        }
    }
);
```
