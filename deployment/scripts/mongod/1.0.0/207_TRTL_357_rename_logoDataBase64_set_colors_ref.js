dbIam.customers.updateMany(
    {},
    {
        $rename: {
            "graphicIdentity.logoDataBase64": "graphicIdentity.logoPortalBase64"
        }
    }
);

dbIam.customers.find(
    {
        "graphicIdentity.themeColors.vitamui-background": { $exists: false },
        "graphicIdentity.themeColors.vitamui-primary": { $exists: true }
    }
).forEach(function (customer) {
    if (customer.graphicIdentity && customer.graphicIdentity.themeColors && customer.graphicIdentity.themeColors['vitamui-primary']) {
        dbIam.customers.updateOne(
            { "_id": customer._id },
            {
                $set: {
                    "graphicIdentity.logoFooterBase64": customer.graphicIdentity.logoPortalBase64,
                    "graphicIdentity.logoHeaderBase64": customer.graphicIdentity.logoPortalBase64,
                    "graphicIdentity.themeColors.vitamui-background": "#F5F5F5",
                    "graphicIdentity.themeColors.vitamui-tertiary": customer.graphicIdentity.themeColors['vitamui-primary'],
                    "graphicIdentity.themeColors.vitamui-header-footer": customer.graphicIdentity.themeColors['vitamui-primary']
                }
            }
        );
    }
});
