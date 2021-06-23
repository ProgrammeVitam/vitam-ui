db = db.getSiblingDB('iam')

print("START 03_TRTL_683_translate_portalTitle_and_portalMessage_ref.js");

db.customers.find(
  {
    "graphicIdentity.hasCustomGraphicIdentity": true
  }
).forEach(function (customer) {
  if (customer.graphicIdentity.portalTitle) {
    var dict = {};
    dict[customer.language] = customer.graphicIdentity.portalTitle;

    db.customers.update(
      { _id: customer._id },
      { $set: { "graphicIdentity.portalTitle": dict } }
    );
  }
  if (customer.graphicIdentity.portalMessage) {
    var dict = {};
    dict[customer.language] = customer.graphicIdentity.portalMessage;

    db.customers.update(
      { _id: customer._id },
      { $set: { "graphicIdentity.portalMessage": dict } }
    );
  }
});

db.customers.updateMany({}, {
  $rename: {
    "graphicIdentity.portalTitle": "portalTitles",
    "graphicIdentity.portalMessage": "portalMessages"
  }
});

print("END 03_TRTL_683_translate_portalTitle_and_portalMessage_ref.js");
