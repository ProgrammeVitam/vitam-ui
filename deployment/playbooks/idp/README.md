IDP Configuration:
==================
**TODO : see how idp are configured and maybe factorise id loading**

# Google idp:

Id: "google_id"


    google/
    ├── google_idp.cert                 # Google idp certificate
    ├── google_idp_keystore_password    # Google idp keystore password
    └── google_idp.metadata.xml         # Google idp metadata

L'idp sera chargé que si les 3 fichiers précédents sont présent (à faire mieux)
