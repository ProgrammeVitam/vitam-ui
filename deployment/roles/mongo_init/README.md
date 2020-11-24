Init of mongo
=========

Script allowing to apply scripts on database.
Scripts are mutualized between dev's docker and deployment.

All scripts are stored into the directory `{PROJECT_ROOT}/deployment/scripts/mongod/`.
Each version of the application, embedding Mongo scripts, is represented by a folder.
Into each version's folder, scripts are indexed according the following format: {index}_{name_of_the_script}.js(.j2).

- 0.0.0
    - 1_init-database.js.j2
    - 2_create-users.js.j2
- 1.0.0
    - 1_init-security-context.js.j2
    - 100_init-security-context_dev.js.j2

The role creates an ordered list of scripts which will be executed on database.
For the sort , we use an option of the Unix command `sort`. The option `-V` allows to sort versions numbers.

Once the list of eligibles scripts is set, we must version them (if the feature is enabled) before their execution.
For this purpose, we inject the content of the script into the template `versionned_script.js.j2`. 
According to this script, we check by its filename if the script has already been executed. If not, the original script is executed and an entry is added into the collection **changelog**.

Requirements
------------

- Mongo database must be deployed and ready.
- The following variables must be defined:
    - **mongod_source_template_dir** (source: playbook): Path where scripts are located.
    - **mongodb.versioning.enable** (source: mongodb_vars.yml): Boolean indicating if a script must be versioned (i.n its execution is recorded into a database. On the next execution of this role, this script will be ignored because it will have already been executed.)
    - **mongodb.included_scripts** (source: mongodb_vars.yml): List of regexs allowing to determine which scripts will be included and applied on database. By default, we accept all.
    - **mongodb.excluded_scripts** (source: mongodb_vars.yml): List of regexs allowing to determine which scripts will be excluded. By default, dev and demo scripts are excluded.

- The following variable(s) can be defined:
    - **mock_insert_data** (source: extra-vars): Flag used in a versioning context, scripts won't be executed. It can be used for a data recovery - the collection *changelog* will be provided with scripts' information.
    - **mongodb.docker.enable** (source: mongodb_vars.yml): Boolean indicating if we use Docker for the database. In this case, a volume must be mounted on the output directory **mongod_output_dir_entry_point**.
    - **mongodb.docker.image_name** (source: mongodb_vars.yml): Name of the docker image.
    - **mongodb.docker.internal_dir** (source: mongodb_vars.yml): Internal path into the container where the host's directory **mongod_output_dir_entry_point** is mapped.

Author Information
------------------
Projet VITAMUI
