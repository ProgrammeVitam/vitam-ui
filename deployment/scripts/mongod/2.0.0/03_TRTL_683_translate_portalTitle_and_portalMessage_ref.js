dbIam.customers.find(
    { "graphicIdentity.hasCustomGraphicIdentity": true }
).forEach(function (customer) {
    if (customer.graphicIdentity.portalTitle) {
        var dict = {};
        dict[customer.language] = customer.graphicIdentity.portalTitle;

        dbIam.customers.updateOne(
            { "_id": customer._id },
            {
                $set: {
                    "graphicIdentity.portalTitle": dict
                }
            }
        );
    }
    if (customer.graphicIdentity.portalMessage) {
        var dict = {};
        dict[customer.language] = customer.graphicIdentity.portalMessage;

        dbIam.customers.updateOne(
            { "_id": customer._id },
            {
                $set: {
                    "graphicIdentity.portalMessage": dict
                }
            }
        );
    }
});

dbIam.customers.updateMany(
    {},
    {
        $rename: {
            "graphicIdentity.portalTitle": "portalTitles",
            "graphicIdentity.portalMessage": "portalMessages"
        }
    }
);
