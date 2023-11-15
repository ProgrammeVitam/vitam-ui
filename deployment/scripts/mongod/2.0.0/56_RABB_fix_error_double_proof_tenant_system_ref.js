db = db.getSiblingDB('iam')

print("START 56_RABB_fix_error_double_proof_tenant_system_ref.js");

// There was two proof tenants linked to system customer, which is not authorised 
db.tenants.updateOne({
    "_id": "auto_tenant"
}, {
    $set: {
        "proof": false
    },
})

print("END 56_RABB_fix_error_double_proof_tenant_system_ref.js");
