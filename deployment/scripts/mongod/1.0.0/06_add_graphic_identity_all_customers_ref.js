// ========================================= ADD GRAPHIC IDENTITY ALL CUSTOMERS =========================================
dbIam.customers.updateMany(
    {},
    {
        $set: {
            "graphicIdentity": {
                "hasCustomGraphicIdentity": false
            }
        }
    }
);
