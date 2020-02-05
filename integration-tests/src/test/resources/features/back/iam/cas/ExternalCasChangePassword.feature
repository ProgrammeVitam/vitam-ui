@Api
@ApiIam
@ApiIamCAS
@ApiIamCASChangePassword
Feature: API CAS : changement de mot de passe

  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CAS_CHANGE_PASSWORD change le mot de passe d'un utilisateur dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_CHANGE_PASSWORD
    Then le mot de passe a été changé car l'utilisateur peut s'authentifier avec son nouveau mot de passe

  Scenario: Utilisateur générique
    When un utilisateur avec le rôle ROLE_CAS_CHANGE_PASSWORD change le mot de passe d'un utilisateur générique dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_CHANGE_PASSWORD
    Then le serveur retourne un utilisateur indisponible

  Scenario: Utilisateur désactivé
    When un utilisateur avec le rôle ROLE_CAS_CHANGE_PASSWORD change le mot de passe d'un utilisateur désactivé dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_CHANGE_PASSWORD
    Then le serveur retourne un utilisateur indisponible

  Scenario: Utilisateur inexistant
    When un utilisateur avec le rôle ROLE_CAS_CHANGE_PASSWORD change le mot de passe d'un utilisateur inexistant dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_CHANGE_PASSWORD
    Then le serveur retourne un utilisateur non trouvé

  Scenario Outline: Cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_CAS_CHANGE_PASSWORD sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_CAS_CHANGE_PASSWORD <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api CAS
    Given deux tenants et un rôle par défaut pour le changement de mot de passe
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_CAS_CHANGE_PASSWORD sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_CAS_CHANGE_PASSWORD <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur change le mot de passe d'un utilisateur
    Then le serveur <autorise ou refuse> l'accès à l'API CAS

  Examples:
    | (userRole) avec ou sans | (headerTenant) principal ou secondaire | (certRole) avec ou sans | (certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess | autorise ou refuse |
    | avec | principal | avec | sur le tenant principal | autorise |
    | sans | principal | avec | sur le tenant principal | refuse |
    | avec | secondaire | avec | sur le tenant principal | refuse |
    | sans | secondaire | avec | sur le tenant principal | refuse |
    | avec | principal | sans | sur le tenant principal | refuse |
    | sans | principal | sans | sur le tenant principal | refuse |
    | avec | secondaire | sans | sur le tenant principal | refuse |
    | sans | secondaire | sans | sur le tenant principal | refuse |
    | avec | principal | avec | sur le tenant secondaire | refuse |
    | sans | principal | avec | sur le tenant secondaire | refuse |
    | avec | secondaire | avec | sur le tenant secondaire | refuse |
    | sans | secondaire | avec | sur le tenant secondaire | refuse |
    | avec | principal | sans | sur le tenant secondaire | refuse |
    | sans | principal | sans | sur le tenant secondaire | refuse |
    | avec | secondaire | sans | sur le tenant secondaire | refuse |
    | sans | secondaire | sans | sur le tenant secondaire | refuse |
    | avec | principal | avec | étant fullAccess | autorise |
    | sans | principal | avec | étant fullAccess | refuse |
    | avec | secondaire | avec | étant fullAccess | refuse |
    | sans | secondaire | avec | étant fullAccess | refuse |
    | avec | principal | sans | étant fullAccess | refuse |
    | sans | principal | sans | étant fullAccess | refuse |
    | avec | secondaire | sans | étant fullAccess | refuse |
    | sans | secondaire | sans | étant fullAccess | refuse |
