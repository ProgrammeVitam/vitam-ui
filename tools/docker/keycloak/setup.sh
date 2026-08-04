#!/usr/bin/env bash
#
# Starts the Keycloak test IdP and wires it into a VitamUI organization:
#
#   1. brings up the container and imports the `vitamui` realm,
#   2. realigns both clients and both test users on what .env declares,
#   3. generates the SAML service provider keystore and downloads the IdP metadata,
#   4. renders and applies the mongo script creating the two providers and their
#      test users.
#
# Idempotent: it can be re-run after any change to .env.
#
# Usage: ./setup.sh [--no-mongo] [--recreate] [--check]
#
#   --check  only verifies that what is stored in mongo still matches the live
#            Keycloak, and changes nothing.

set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

APPLY_MONGO=1
RECREATE=0
CHECK_ONLY=0
for arg in "$@"; do
    case "$arg" in
        --no-mongo) APPLY_MONGO=0 ;;
        --recreate) RECREATE=1 ;;
        --check) CHECK_ONLY=1 ;;
        -h | --help)
            sed -n '2,15p' "$0" | cut -c 3-
            exit 0
            ;;
        *)
            echo "Unknown option: $arg" >&2
            exit 1
            ;;
    esac
done

# Same precedence rule as docker compose: a variable already present in the
# environment wins over the .env file, so `MONGO_DB=scratch ./setup.sh` works.
load_env() {
    local key value
    while IFS='=' read -r key value; do
        [[ $key =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || continue
        [[ -n ${!key+set} ]] && continue
        export "$key=$value"
    done < <(grep -vE '^[[:space:]]*(#|$)' ./.env)
}
load_env

GENERATED_DIR=generated
KEYSTORE_FILE="$GENERATED_DIR/sp-keystore.p12"
IDP_METADATA_FILE="$GENERATED_DIR/idp-metadata.xml"
MONGO_SCRIPT="$GENERATED_DIR/vitamui-providers.js"

CAS_LOGIN_URL="${VITAMUI_CAS_URL}/login"
SAML_ENTITY_ID="${CAS_LOGIN_URL}/${SAML_TECHNICAL_NAME}"
SAML_ACS_URL="${CAS_LOGIN_URL}?client_name=${SAML_TECHNICAL_NAME}"
OIDC_CLIENT_UUID=11111111-1111-1111-1111-111111111111
SAML_CLIENT_UUID=22222222-2222-2222-2222-222222222222
OIDC_USER_UUID=33333333-3333-3333-3333-333333333333
SAML_USER_UUID=44444444-4444-4444-4444-444444444444

for tool in docker curl jq keytool base64 envsubst; do
    command -v "$tool" >/dev/null || {
        echo "Missing required tool: $tool" >&2
        exit 1
    }
done

mkdir -p "$GENERATED_DIR"

# --- Consistency check --------------------------------------------------------

# The SAML provider carries a *copy* of the IdP signing certificate, so it goes
# stale the moment Keycloak regenerates its realm keys — and the only symptom is
# a "Signature is not trusted" in the CAS logs behind an opaque "unauthorized"
# page. The OIDC provider is immune: CAS refetches the discovery document and
# the JWKS at each authentication.
check_consistency() {
    local live stored discovery status=0

    live=$(curl -sf "${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/protocol/saml/descriptor" |
        grep -o '<ds:X509Certificate>[^<]*' | head -1 | cut -d'>' -f2) || true
    if [[ -z $live ]]; then
        echo "  [KO] no SAML descriptor at ${KEYCLOAK_BASE_URL} — is the container up?"
        return 1
    fi

    # Without this probe an unreachable mongo surfaces as "no SAML provider",
    # which sends the reader off re-running ./setup.sh for nothing.
    if ! mongosh --quiet "$MONGO_URI" --eval 'db.getMongo()' >/dev/null 2>&1; then
        echo "  [KO] cannot reach the mongo instance declared by MONGO_URI in .env —"
        echo "       is tools/docker/mongo up?"
        return 1
    fi

    stored=$(mongosh --quiet "$MONGO_URI" --eval \
        "print((db.getSiblingDB('${MONGO_DB}').providers.findOne({_id:'keycloak_test_idp_saml'})||{}).idpMetadata||'')" 2>/dev/null |
        grep -o '<ds:X509Certificate>[^<]*' | head -1 | cut -d'>' -f2) || true
    if [[ -z $stored ]]; then
        echo "  [KO] no SAML provider in ${MONGO_DB}.providers — run ./setup.sh"
        status=1
    elif [[ $stored == "$live" ]]; then
        echo "  [OK] the SAML provider carries the certificate Keycloak signs with"
    else
        echo "  [KO] STALE SAML metadata: the stored certificate is not the one Keycloak"
        echo "       signs with. Every assertion will be refused with 'Signature is not"
        echo "       trusted'. Fix it by re-running ./setup.sh"
        status=1
    fi

    discovery=$(mongosh --quiet "$MONGO_URI" --eval \
        "print((db.getSiblingDB('${MONGO_DB}').providers.findOne({_id:'keycloak_test_idp_oidc'})||{}).discoveryUrl||'')" 2>/dev/null) || true
    if [[ -n $discovery ]] && curl -sf -o /dev/null "$discovery"; then
        echo "  [OK] the OIDC discovery url of the provider answers"
    else
        echo "  [KO] unreachable OIDC discovery url: ${discovery:-<absent>}"
        status=1
    fi

    return $status
}

if [[ $CHECK_ONLY -eq 1 ]]; then
    echo "==> Checking Keycloak against ${MONGO_DB}"
    check_consistency
    exit $?
fi

# --- 1. Container -------------------------------------------------------------

if [[ $RECREATE -eq 1 ]]; then
    echo "==> Removing the existing container and its embedded database"
    docker compose down -v
fi

echo "==> Starting Keycloak"
docker compose up -d

echo -n "==> Waiting for realm '${KEYCLOAK_REALM}' "
DISCOVERY_URL="${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/.well-known/openid-configuration"
for _ in $(seq 1 120); do
    if curl -sf -o /dev/null "$DISCOVERY_URL"; then
        echo " ready"
        break
    fi
    echo -n "."
    sleep 2
done
curl -sf -o /dev/null "$DISCOVERY_URL" || {
    echo
    echo "Keycloak did not expose ${DISCOVERY_URL}. Logs:" >&2
    docker compose logs --tail 40 keycloak >&2
    exit 1
}

# --- 2. Realm alignment on .env -----------------------------------------------

# The imported realm carries the default CAS URL and the default test
# identities. Rather than templating the realm file, the clients and the users
# are patched through the admin API so that .env stays the single source of
# truth — including on an already-imported realm, which the IGNORE_EXISTING
# import strategy would otherwise leave untouched.
admin_token() {
    curl -sf --data-urlencode "client_id=admin-cli" \
        --data-urlencode "username=${KEYCLOAK_ADMIN_USER}" \
        --data-urlencode "password=${KEYCLOAK_ADMIN_PASSWORD}" \
        --data-urlencode "grant_type=password" \
        "${KEYCLOAK_BASE_URL}/realms/master/protocol/openid-connect/token" | jq -r '.access_token'
}

TOKEN="$(admin_token)"
[[ -n "$TOKEN" && "$TOKEN" != "null" ]] || {
    echo "Could not obtain an admin token from ${KEYCLOAK_BASE_URL}." >&2
    exit 1
}

ADMIN_REALM_URL="${KEYCLOAK_BASE_URL}/admin/realms/${KEYCLOAK_REALM}"

# Reads a realm resource, applies the jq program given as remaining arguments and
# writes it back. Keycloak expects a full representation on PUT, hence the
# read-modify-write rather than a partial payload.
patch_resource() {
    local url="$1"
    shift
    curl -sf -H "Authorization: Bearer ${TOKEN}" "$url" |
        jq "$@" |
        curl -sf -o /dev/null -X PUT -H "Authorization: Bearer ${TOKEN}" \
            -H 'Content-Type: application/json' --data @- "$url"
}

patch_client() {
    local uuid="$1"
    shift
    patch_resource "${ADMIN_REALM_URL}/clients/${uuid}" "$@"
}

# Keycloak and VitamUI must agree on the address, delegated authentication
# resolving the account by email on both sides. The password is reset on every
# run: it is not readable back, so it cannot be compared.
patch_user() {
    local uuid="$1" email="$2" lastname="$3"
    # The realm file pins the id of the two test users so they stay addressable
    # after a rename. A realm imported before those ids existed keeps its own
    # users, and IGNORE_EXISTING will not replace them.
    curl -sf -o /dev/null -H "Authorization: Bearer ${TOKEN}" "${ADMIN_REALM_URL}/users/${uuid}" || {
        echo "Test user ${uuid} is missing from realm '${KEYCLOAK_REALM}'." >&2
        echo "Re-run with --recreate to import the current realm file." >&2
        exit 1
    }
    patch_resource "${ADMIN_REALM_URL}/users/${uuid}" \
        --arg email "$email" \
        --arg lastname "$lastname" \
        '.username = $email | .email = $email | .lastName = $lastname | .emailVerified = true'
    jq -n --arg password "$IDP_USER_PASSWORD" \
        '{type: "password", value: $password, temporary: false}' |
        curl -sf -o /dev/null -X PUT -H "Authorization: Bearer ${TOKEN}" \
            -H 'Content-Type: application/json' --data @- \
            "${ADMIN_REALM_URL}/users/${uuid}/reset-password"
}

echo "==> Aligning the OIDC client on ${VITAMUI_CAS_URL}"
# post.logout.redirect.uris matters as much as redirectUris: Keycloak validates it BEFORE
# ending the session, so an address it does not accept means no logout at all, not merely a
# failed final redirect. VitamUI ends the logout on the application the user came from, which
# is not under the CAS url the client redirect uris cover.
patch_client "$OIDC_CLIENT_UUID" \
    --arg clientId "$OIDC_CLIENT_ID" \
    --arg secret "$OIDC_CLIENT_SECRET" \
    --arg redirect "${CAS_LOGIN_URL}*" \
    --arg postLogout "$VITAMUI_POST_LOGOUT_REDIRECT_URIS" \
    '.clientId = $clientId
     | .secret = $secret
     | .redirectUris = [$redirect]
     | .attributes."post.logout.redirect.uris" = $postLogout'

echo "==> Aligning the SAML client on ${SAML_ENTITY_ID}"
patch_client "$SAML_CLIENT_UUID" \
    --arg entityId "$SAML_ENTITY_ID" \
    --arg acs "$SAML_ACS_URL" \
    --arg redirect "${CAS_LOGIN_URL}*" \
    '.clientId = $entityId
     | .redirectUris = [$redirect]
     | .attributes."saml_assertion_consumer_url_post" = $acs
     | .attributes."saml_assertion_consumer_url_redirect" = $acs
     | .attributes."saml_single_logout_service_url_post" = $acs
     | .attributes."saml_single_logout_service_url_redirect" = $acs'

echo "==> Aligning the test users on ${OIDC_USER_EMAIL} / ${SAML_USER_EMAIL}"
patch_user "$OIDC_USER_UUID" "$OIDC_USER_EMAIL" "OIDC"
patch_user "$SAML_USER_UUID" "$SAML_USER_EMAIL" "SAML"

# --- 3. SAML service provider material ----------------------------------------

# pac4j reads the keystore from an in-memory resource, so it cannot generate the
# key pair itself: the keystore handed to VitamUI must already hold one.
if [[ -f "$KEYSTORE_FILE" ]]; then
    echo "==> Reusing the service provider keystore ${KEYSTORE_FILE}"
else
    echo "==> Generating the service provider keystore ${KEYSTORE_FILE}"
    keytool -genkeypair \
        -alias vitamui-sp \
        -keyalg RSA -keysize 2048 -validity 3650 \
        -dname "CN=VitamUI CAS SP, OU=VitamUI, O=VitamUI, C=FR" \
        -keystore "$KEYSTORE_FILE" -storetype PKCS12 \
        -storepass "$SP_KEYSTORE_PASSWORD" -keypass "$SP_KEYSTORE_PASSWORD" >/dev/null
fi

echo "==> Downloading the SAML IdP metadata"
curl -sf "${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/protocol/saml/descriptor" -o "$IDP_METADATA_FILE"

# --- 4. VitamUI side ----------------------------------------------------------

echo "==> Rendering ${MONGO_SCRIPT}"
SP_KEYSTORE_BASE64="$(base64 -w0 "$KEYSTORE_FILE")"
IDP_METADATA_XML="$(cat "$IDP_METADATA_FILE")"
export SP_KEYSTORE_BASE64 IDP_METADATA_XML

# Restricted variable list: the template is javascript, an unrestricted envsubst
# would eat every mongo operator ($set, $inc...).
envsubst '
  ${OIDC_TECHNICAL_NAME} ${SAML_TECHNICAL_NAME}
  ${OIDC_EMAIL_DOMAIN} ${SAML_EMAIL_DOMAIN}
  ${OIDC_USER_EMAIL} ${SAML_USER_EMAIL}
  ${OIDC_CLIENT_ID} ${OIDC_CLIENT_SECRET}
  ${KEYCLOAK_BASE_URL} ${KEYCLOAK_REALM}
  ${SP_KEYSTORE_BASE64} ${SP_KEYSTORE_PASSWORD} ${IDP_METADATA_XML}
  ${VITAMUI_REFERENCE_USER_EMAIL} ${VITAMUI_CUSTOMER_ID} ${VITAMUI_GROUP_ID}
  ${MONGO_DB}
' <provision/vitamui-providers.js.tpl >"$MONGO_SCRIPT"

if [[ $APPLY_MONGO -eq 0 ]]; then
    echo
    echo "Skipping the mongo step. Apply it with:"
    echo "  mongosh \"\$MONGO_URI\" $(pwd)/${MONGO_SCRIPT}"
    exit 0
fi

command -v mongosh >/dev/null || {
    echo "mongosh not found: re-run with --no-mongo and apply ${MONGO_SCRIPT} yourself." >&2
    exit 1
}

echo "==> Applying ${MONGO_SCRIPT} to ${MONGO_DB}"
mongosh --quiet "$MONGO_URI" "$MONGO_SCRIPT"

echo "==> Checking what was written against the live Keycloak"
check_consistency || exit 1

cat <<EOF

Keycloak    : ${KEYCLOAK_BASE_URL}  (admin: ${KEYCLOAK_ADMIN_USER} / ${KEYCLOAK_ADMIN_PASSWORD})
OIDC test   : ${OIDC_USER_EMAIL} / ${IDP_USER_PASSWORD}
SAML test   : ${SAML_USER_EMAIL} / ${IDP_USER_PASSWORD}

Log in on ${VITAMUI_CAS_URL}/login with either address.
EOF
