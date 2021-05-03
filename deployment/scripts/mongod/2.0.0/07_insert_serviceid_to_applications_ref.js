db = db.getSiblingDB('iam')

print("Start 07_insert_serviceid_to_applications_ref.js");

// =============== INGEST ==========

db.applications.update({
    "identifier" : "INGEST_MANAGEMENT_APP"
}, {
    $set: {
       {% if vitamui.ingest.base_url is defined %}
       	"serviceId2": "^{{ vitamui.ingest.base_url }}/.*",
       {% else %}
       	"serviceId2": "^{{ url_prefix }}/ingest/.*",
       {% endif %}
    },
    }
);

db.applications.update({
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

db.applications.update({
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

db.applications.update({
    "identifier" : "ACCOUNTS_APP",
}, {
    $set: {
     "serviceId": "^{{ vitamui.portal.base_url|default(url_prefix) }}/.*"
    },
    }
);

db.applications.update({
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


db.applications.update({
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

db.applications.update({
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


db.applications.update({
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


db.applications.update({
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


db.applications.update({
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

db.applications.update({
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

db.applications.update({
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


db.applications.update({
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


db.applications.update({
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


db.applications.update({
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


db.applications.update({
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


db.applications.update({
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


db.applications.update({
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


db.applications.update({
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

db.applications.update({
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

db.applications.update({
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

db.applications.update({
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

db.applications.update({
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
