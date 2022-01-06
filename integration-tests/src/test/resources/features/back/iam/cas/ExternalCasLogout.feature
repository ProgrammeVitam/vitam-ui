@Api
@ApiIam
@ApiIamCAS
@ApiIamCASLogout
Feature: API CAS : Logout

  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CAS_LOGOUT fait un logout dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGOUT
    Then la subrogation a bien été supprimée
    And le token d'authentification a bien été supprimé
