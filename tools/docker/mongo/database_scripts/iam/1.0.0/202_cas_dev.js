use cas;

print("START 105_cas_dev.js");

db.services.insert({
	"_id" : NumberInt(300),
	"_class": "org.apereo.cas.services.RegexRegisteredService",
	"serviceId": "^https://.*.vitamui.com:4200.*",
	"name": "Angular Portal Application",
	"logoutType" : "FRONT_CHANNEL",
	"logoutUrl": "https://dev.vitamui.com:9000/logout",
	"attributeReleasePolicy": {
  		"_class": "org.apereo.cas.services.ReturnAllAttributeReleasePolicy"
	}
});

db.services.insert({
	"_id" : NumberInt(301),
	"_class": "org.apereo.cas.services.RegexRegisteredService",
	"serviceId": "^https://.*.vitamui.com:4201.*",
	"name": "Angular Identity Application",
	"logoutType" : "FRONT_CHANNEL",
	"logoutUrl": "https://dev.vitamui.com:9001/logout",
	"attributeReleasePolicy": {
  		"_class": "org.apereo.cas.services.ReturnAllAttributeReleasePolicy"
	}
});

db.services.insert({
	"_id" : NumberInt(302),
	"_class": "org.apereo.cas.services.RegexRegisteredService",
	"serviceId": "^https://.*.vitamui.com.*",
	"name": "Apache Domain",
	"logoutType" : "FRONT_CHANNEL",
	"logoutUrl": "https://dev.vitamui.com/logout",
	"attributeReleasePolicy": {
  		"_class": "org.apereo.cas.services.ReturnAllAttributeReleasePolicy"
	}
});

print("END 105_cas_dev.js");

