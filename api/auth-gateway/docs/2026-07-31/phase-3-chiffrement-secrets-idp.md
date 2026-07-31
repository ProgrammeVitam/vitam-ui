# Phase 3 §3 — Chiffrement des secrets IdP au repos

## Résumé

Les 4 champs sensibles de `IdentityProvider` (`clientSecret`, `keystoreBase64`, `keystorePassword`, `privateKeyPassword`) sont chiffrés au repos dans la collection Mongo `providers` de la base `iam`, dès qu'une clef est fournie à IAM.

L'activation est **opt-in explicite** : par défaut (clef non fournie), IAM tourne en mode passthrough — les valeurs restent en clair, un WARN est loggé au boot, et rien ne se passe côté données. Ce choix évite le piège d'une clef embarquée en dur (fausse sécurité, tout le monde avec accès au code peut déchiffrer).

## Composants

| Fichier | Rôle |
|---|---|
| `api/api-iam/iam/.../idp/domain/EncryptedSecretCipher.java` | AES-GCM 256 bits. Format on disk : `{enc:v1}<base64url(iv12 \|\| ciphertext \|\| tag16)>`. Passthrough quand `secrets.idp-key` absente. |
| `api/api-iam/iam/.../idp/domain/EncryptedSecretConverter.java` | `PropertyValueConverter` Spring Data Mongo. Read tolérant (retourne clair si non préfixé). Write chiffre (ou passe-through selon mode cipher). |
| `api/api-iam/iam/.../config/IamMongoConfig.java` | Bean `@Primary MongoCustomConversions` qui registre le converter sur les 4 chemins de `IdentityProvider`. |
| `api/api-iam/iam/.../idp/domain/IdentityProviderSecretMigrationRunner.java` | `CommandLineRunner` au boot : re-save les IdP dont un champ sensible est encore en clair. Idempotent. No-op si cipher désactivé. |
| `api/api-iam/iam/src/main/resources/application.yml` | Propriété `secrets.idp-key: ~` (namespace hors `iam.*` pour éviter la validation stricte). |

## Activation d'une clef

### En dev local

```bash
openssl rand -base64 32
```

Copie la sortie et remplace `secrets.idp-key: ~` par `secrets.idp-key: <base64>` dans `api/api-iam/iam/src/main/resources/application.yml`. Redémarre IAM.

Au boot :
```
INFO  EncryptedSecretCipher initialised — IdP secret fields will be encrypted at rest.
INFO  IdP <id> secrets migrated to encrypted storage
INFO  IdentityProvider secret migration: N migrated, 0 already encrypted, N total
```

### En staging / prod

Ne pas commit la clef dans le repo. Elle vit dans le fichier de conf spécifique à l'env (surchargé via `spring.config.location` ou monté par l'orchestration). Le format reste `secrets.idp-key: <base64>`.

## Vérifier

**1. Le mode actif** — chercher au boot :
- `EncryptedSecretCipher initialised` → chiffrement actif
- `no secrets.idp-key configured — IdP secrets are stored IN THE CLEAR` → passthrough

**2. L'état Mongo** :

```bash
mongosh "mongodb://<user>:<pwd>@<host>:27018/iam" --eval '
  db.providers.find({}, {
    _id:1, clientSecret:1, keystorePassword:1,
    privateKeyPassword:1, keystoreBase64:1
  }).forEach(p => { print(JSON.stringify(p, null, 2).substring(0, 400)); print("---"); })'
```

Avec cipher actif, chaque champ non-null commence par `"{enc:v1}"`. Sans cipher, valeurs en clair.

**3. Le read transparent** — faire un login OIDC ou SAML depuis le portal. Le SAS reçoit un `clientSecret` en clair via `/cas/idp/{id}` (IAM déchiffre au read). Login end-to-end réussi = chaîne complète OK.

**4. L'idempotence** — 2ème restart IAM :
```
DEBUG IdentityProvider secret migration: nothing to do (N providers)
```

## Compatibilité CAS legacy (develop)

Le chiffrement est transparent pour tout consommateur qui passe par IAM. CAS de develop appelle IAM (via ses services `IdentityProviderService` / `IdentityProviderHelper`) qui déchiffre au read. Aucun changement de code côté CAS n'est requis.

Point d'attention : CAS doit pointer vers la **même Mongo `iam`** que le SAS pour partager le storage. C'est déjà le cas dans les environnements existants.

## Rollback — pièges

### Ne PAS rollback la clef en prod sans purger

Une fois des rows chiffrées avec la clef K :
- Si IAM redémarre **sans clef** → cipher en passthrough → au read, il ne déchiffre rien → SAS reçoit littéralement `"{enc:v1}..."` comme `clientSecret` → login IdP KO.
- Si IAM redémarre avec une **autre clef** K' → tentative de déchiffrement AES-GCM avec la mauvaise clef → `AEADBadTagException` → 500 au read.

Deux options si rollback nécessaire :
1. **Restaurer la clef d'origine** (backup).
2. **Purger et re-provisionner** les rows chiffrées : soit exporter en clair avant la bascule (via un IAM avec la bonne clef, dump en clair), soit re-créer les IdP à la main.

### Rotation de clef

Pas implémentée dans cette version. Le format `{enc:v1}` prévoit un versioning : une future version pourrait porter `{enc:v2:<keyid>}` et supporter plusieurs clefs en parallèle pour permettre une rotation en 2 passes (lecture multi-clef pendant N jours, puis coupure de l'ancienne).

En attendant, si une rotation est absolument nécessaire :
1. IAM en marche avec la vieille clef → dump chaque `IdentityProviderDto` en clair via l'API.
2. Bascule la clef, restart IAM (passthrough temporaire ou nouvelle clef — ne pas migrer sur base ancienne).
3. Purger `providers` en Mongo, re-`POST` chaque IdP via l'API → sera écrit avec la nouvelle clef.

Procédure fastidieuse à automatiser en Phase 4 si nécessaire.

### Perte de clef

Si la clef est perdue et pas de backup :
- Les IdP chiffrés deviennent **irrécupérables** (AES-GCM sans clef = pas d'attaque connue).
- Il faut re-créer les IdP à la main (revoir les valeurs auprès des tenants).
- Sauvegarder la clef comme un secret critique : coffre-fort d'entreprise, HSM, vault applicatif.

## Dette / limites connues

- **Rotation** : non implémentée (voir ci-dessus).
- **Champs supportés** : uniquement les 4 champs de `IdentityProvider`. Étendre la liste dans `IamMongoConfig` + `IdentityProviderSecretMigrationRunner.SENSITIVE_FIELDS` si d'autres champs sensibles doivent être ajoutés.
- **Algo** : AES-GCM 256 fixe. Le prefix `{enc:v1}` permet un changement d'algo à terme (v2 = un autre schéma, deserialisation multi-version dans `decrypt`).
- **Portée** : chiffrement Mongo uniquement. Les valeurs qui transitent sur le réseau (IAM → SAS via mTLS) restent en clair par nécessité — le canal mTLS assure la confidentialité en transit.
