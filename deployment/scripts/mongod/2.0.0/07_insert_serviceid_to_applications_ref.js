db = db.getSiblingDB('iam')

print("Start 07_insert_serviceid_to_applications_ref.js");

// =============== INGEST ==========

db.applications.updateOne({
    "identifier" : "HOLDING_FILLING_SCHEME_APP",
}, {
    $set: {
{% if vitamui.ingest.base_url is defined %}
	"serviceId": "^{{ vitamui.ingest.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/ingest/.*",
{% endif %}
    },
    }
);

// =============== ARCHIVE-SEARCH ==========

db.applications.updateOne({
    "identifier" : "ARCHIVE_SEARCH_MANAGEMENT_APP"
}, {
    $set: {
{% if vitamui.archive_search.base_url is defined %}
	"serviceId": "^{{ vitamui.archive_search.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/archive-search/.*",
{% endif %}
    },
    }
);

// =============== IDENTITY ==========

db.applications.updateOne({
    "identifier" : "ACCOUNTS_APP",
}, {
    $set: {
     "serviceId": "^{{ vitamui.portal.base_url|default(url_prefix) }}/.*"
    },
    }
);

db.applications.updateOne({
        "identifier" : "HIERARCHY_PROFILE_APP",
}, {
    $set: {
    {% if vitamui.identity.base_url is defined %}
    	"serviceId": "^{{ vitamui.identity.base_url }}/.*",
    {% else %}
    	"serviceId": "^{{ url_prefix }}/identity/.*",
    {% endif %}
    },
    }
);


db.applications.updateOne({
        "identifier" : "SUBROGATIONS_APP",
}, {
    $set: {
    {% if vitamui.identity.base_url is defined %}
    	"serviceId": "^{{ vitamui.identity.base_url }}/.*",
    {% else %}
    	"serviceId": "^{{ url_prefix }}/identity/.*",
    {% endif %}
    },
    }
);

db.applications.updateOne({
    "identifier" : "PROFILES_APP",
}, {
    $set: {
{% if vitamui.identity.base_url is defined %}
	"serviceId": "^{{ vitamui.identity.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/identity/.*",
{% endif %}
    },
    }
);


db.applications.updateOne({
    "identifier" : "GROUPS_APP",
}, {
    $set: {
{% if vitamui.identity.base_url is defined %}
	"serviceId": "^{{ vitamui.identity.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/identity/.*",
{% endif %}
    },
    }
);


db.applications.updateOne({
    "identifier" : "USERS_APP",
}, {
    $set: {
{% if vitamui.identity.base_url is defined %}
	"serviceId": "^{{ vitamui.identity.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/identity/.*",
{% endif %}
    },
    }
);


db.applications.updateOne({
    "identifier" : "CUSTOMERS_APP",
}, {
    $set: {
{% if vitamui.identity.base_url is defined %}
	"serviceId": "^{{ vitamui.identity.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/identity/.*",
{% endif %}
    },
    }
);


// =============== REFERENTIAL ==========

db.applications.updateOne({
    "identifier" : "LOGBOOK_OPERATION_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);

db.applications.updateOne({
    "identifier" : "PROBATIVE_VALUE_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);


db.applications.updateOne({
    "identifier" : "DSL_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);


db.applications.updateOne({
    "identifier" : "SECURE_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);


db.applications.updateOne({
    "identifier" : "AUDIT_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);


db.applications.updateOne({
    "identifier" : "ONTOLOGY_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);


db.applications.updateOne({
    "identifier" : "SECURITY_PROFILES_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);


db.applications.updateOne({
    "identifier" : "CONTEXTS_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);


db.applications.updateOne({
    "identifier" : "FILE_FORMATS_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);

db.applications.updateOne({
    "identifier" : "AGENCIES_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);

db.applications.updateOne({
    "identifier" : "ACCESS_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);

db.applications.updateOne({
    "identifier" : "INGEST_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);

db.applications.updateOne({
    "identifier" : "RULES_APP",
}, {
    $set: {
{% if vitamui.referential.base_url is defined %}
	"serviceId": "^{{ vitamui.referential.base_url }}/.*",
{% else %}
	"serviceId": "^{{ url_prefix }}/referential/.*",
{% endif %}
    },
    }
);


print("End 07_insert_serviceid_to_applications_ref.js");
