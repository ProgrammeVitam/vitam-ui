@Api
@ExternalParamProfile
@ExternalParamProfileCoupleOfChecks
Feature: API external parameter profile : récupérer

  Scenario: les profils de paramétrage externe des contrats d'accès
    When un utilisateur avec le rôle ROLE_CREATE_PROFILES crée un profil avec référence au paramétrage externe
    When un utilisateur crée un paramétrage externe
    When un utilisateur avec le rôle ROLE_SEARCH_EXTERNAL_PARAM_PROFILE récupère toutes les entrées des profils et du paramétrage externe par page associé à son profil en utilisant un certificat full access avec le rôle ROLE_SEARCH_EXTERNAL_PARAM_PROFILE
    Then le serveur retourne la totalité des résultats
