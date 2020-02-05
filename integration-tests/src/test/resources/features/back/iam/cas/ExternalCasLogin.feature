@Api
@ApiIam
@ApiIamCAS
@ApiIamCASLogin
Feature: API CAS : Authentifier

  Scenario: Un utilisateur
    When un utilisateur avec le rôle ROLE_CAS_LOGIN authentifie un utilisateur dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGIN
    Then le serveur retourne l'utilisateur authentifié

  Scenario Outline: Un utilisateur, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_CAS_LOGIN sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_CAS_LOGIN <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api CAS
    Given deux tenants et un rôle par défaut pour authentifier un utilisateur
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_CAS_LOGIN sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_CAS_LOGIN <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur authentifie un utilisateur
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

  Scenario: Un utilisateur avec un mauvais mot de passe
    When un utilisateur avec le rôle ROLE_CAS_LOGIN authentifie un utilisateur avec un mauvais mot de passe dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGIN
    Then le serveur retourne une erreur bad credentials

  Scenario: Un utilisateur générique
    When un utilisateur avec le rôle ROLE_CAS_LOGIN authentifie un utilisateur générique dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGIN
    Then le serveur retourne un utilisateur indisponible

  Scenario: Un utilisateur avec un mauvais mot de passe trop de fois
    When un utilisateur avec le rôle ROLE_CAS_LOGIN authentifie un utilisateur avec un mauvais mot de passe trop de fois dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGIN
    Then le serveur retourne une erreur de trop d'essais
    And l'utilisateur est bloqué

  Scenario: Un utilisateur désactivé
    When un utilisateur avec le rôle ROLE_CAS_LOGIN authentifie un utilisateur désactivé dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGIN
    Then le serveur retourne un utilisateur indisponible

  Scenario: Un utilisateur inexistant
    When un utilisateur avec le rôle ROLE_CAS_LOGIN authentifie un utilisateur inexistant dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_LOGIN
    Then le serveur retourne un utilisateur non trouvé
