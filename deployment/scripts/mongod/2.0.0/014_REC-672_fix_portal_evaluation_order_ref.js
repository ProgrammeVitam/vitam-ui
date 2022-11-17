print("START 014_REC-672_fix_portal_evaluation_order_ref.js");

db = db.getSiblingDB("cas");

// Set portal service evaluation number to 10000 to avoid url conflict
db.services.updateOne(
  {
    _id:  NumberInt(1),
  },
  {
    $set: {
          evaluationOrder: 10000 
    }
  }
);

// Remove workaround
db.services.updateOne(
  {
    _id:  NumberInt(1),
    name: "Z Portal Application"
  },
  {
    $set: {
      name: "Portal Application"
    }
  }
);


print("END 014_REC-672_fix_portal_evaluation_order_ref.js");
