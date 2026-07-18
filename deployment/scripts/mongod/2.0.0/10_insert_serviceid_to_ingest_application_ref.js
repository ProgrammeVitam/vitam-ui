dbIam.applications.updateMany(
    {},
    { $unset: { serviceId2: "" } }
);

dbIam.applications.updateOne(
    { "identifier": "INGEST_MANAGEMENT_APP" },
    {
        $set: {
        {% if vitamui.ui_ingest.base_url is defined %}
            "serviceId": "^{{ vitamui.ui_ingest.base_url }}/.*",
        {% else %}
            "serviceId": "^{{ url_prefix }}/ingest/.*",
        {% endif %}
        }
    }
);
