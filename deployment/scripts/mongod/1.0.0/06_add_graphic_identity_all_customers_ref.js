db = db.getSiblingDB('iam')

print("START add_graphic_identity_all_customers_ref.js");

// ========================================= ADD GRAPHIC IDENTITY ALL CUSTOMERS =========================================

db.customers.updateMany(
  {},
  {
    $set: {
    	"graphicIdentity": {
    		"hasCustomGraphicIdentity": false
      }
	  }
  }
);

print("END add_graphic_identity_all_customers_ref.js");
