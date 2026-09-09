#!/usr/bin/env bash
#
# Removes everything ./setup.sh created: the two providers and their test users
# in the iam database, then the container and its embedded database.
#
# Usage: ./teardown.sh [--keep-mongo]

set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

CLEAN_MONGO=1
for arg in "$@"; do
    case "$arg" in
        --keep-mongo) CLEAN_MONGO=0 ;;
        *)
            echo "Unknown option: $arg" >&2
            exit 1
            ;;
    esac
done

# Same precedence rule as docker compose: the environment wins over .env.
load_env() {
    local key value
    while IFS='=' read -r key value; do
        [[ $key =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || continue
        [[ -n ${!key+set} ]] && continue
        export "$key=$value"
    done < <(grep -vE '^[[:space:]]*(#|$)' ./.env)
}
load_env

if [[ $CLEAN_MONGO -eq 1 ]] && command -v mongosh >/dev/null; then
    echo "==> Removing the providers and test users from ${MONGO_DB}"
    mongosh --quiet "$MONGO_URI" --eval "
        db = db.getSiblingDB('${MONGO_DB}');
        const providers = ['keycloak_test_idp_oidc', 'keycloak_test_idp_saml'];
        const users = ['keycloak_test_user_oidc', 'keycloak_test_user_saml'];
        const userInfos = ['keycloak_test_userinfo_oidc', 'keycloak_test_userinfo_saml'];
        // Read before deleting: the providers carry the only trace of the
        // organization setup.sh wrote to, and the email domains must be pulled
        // from that organization alone — another one may legitimately use them.
        const customerIds = db.providers
            .find({_id: {\$in: providers}}, {customerId: 1})
            .toArray()
            .map(function (provider) { return provider.customerId; })
            .filter(function (customerId) { return Boolean(customerId); });
        print('providers removed: ' + db.providers.deleteMany({_id: {\$in: providers}}).deletedCount);
        print('users removed    : ' + db.users.deleteMany({_id: {\$in: users}}).deletedCount);
        print('userInfos removed: ' + db.userInfos.deleteMany({_id: {\$in: userInfos}}).deletedCount);
        if (customerIds.length) {
            db.customers.updateMany(
                {_id: {\$in: customerIds}},
                {\$pull: {emailDomains: {\$in: ['${OIDC_EMAIL_DOMAIN}', '${SAML_EMAIL_DOMAIN}']}}}
            );
        }
    "
fi

echo "==> Removing the container and its volumes"
docker compose down -v

echo "==> Removing generated files"
rm -rf generated
