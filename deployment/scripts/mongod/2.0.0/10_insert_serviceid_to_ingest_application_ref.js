db = db.getSiblingDB('iam')

print("Start 10_insert_serviceid_to_ingest_application_ref.js");

db.applications.updateMany(
   { },
   { $unset: { serviceId2: "" } }
);

db.applications.updateOne({
    "identifier" : "INGEST_MANAGEMENT_APP"
}, {
    $set: {
       {% if vitamui.ui_ingest.base_url is defined %}
       	"serviceId": "^{{ vitamui.ui_ingest.base_url }}/.*",
       {% else %}
       	"serviceId": "^{{ url_prefix }}/ingest/.*",
       {% endif %}
    },
    }
);

print("End 10_insert_serviceid_to_ingest_application_ref.js");
