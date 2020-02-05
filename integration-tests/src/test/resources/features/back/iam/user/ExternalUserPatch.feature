@Api
@ApiIam
@ApiIamUsers
@ApiIamUsersPatch
Feature: API User : mise à jour partielle d'utilisateurs

	@Traces
  Scenario: Cas normal
    Given un utilisateur a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour partiellement un utilisateur dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS
    Then le serveur retourne l'utilisateur mis à jour partiellement
    And une trace de mise à jour de l'utilisateur est présente dans vitam

  Scenario: Cas readonly
    Given un utilisateur a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour partiellement un utilisateur en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS
    Then le serveur refuse la mise à jour partielle de l'utilisateur à cause du readonly

  Scenario: Cas mauvais client de l'utilisateur
    Given un utilisateur a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour partiellement un utilisateur mais avec un mauvais client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS
    Then le serveur refuse la mise à jour partielle de l'utilisateur à cause du mauvais client

  Scenario: Cas email existant de l'utilisateur
    Given un utilisateur a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour partiellement un utilisateur mais avec un email existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS
    Then le serveur refuse la mise à jour partielle de l'utilisateur à cause de l'email existant

  Scenario: Cas groupe inexistant de l'utilisateur
    Given un utilisateur a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_USERS met à jour partiellement un utilisateur mais avec un groupe inexistant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS
    Then le serveur refuse la mise à jour partielle de l'utilisateur à cause du groupe inexistant

  Scenario: Cas d'un utilisateur sans le bon niveau
    Given un utilisateur a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_USERS sans le bon niveau met à jour partiellement un utilisateur dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_USERS
    Then le serveur refuse la mise à jour partielle de l'utilisateur à cause du mauvais niveau

  Scenario Outline: Cas sécurité, par un utilisateur <(userRole) avec ou sans> les rôles ROLE_UPDATE_USERS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> les rôles ROLE_UPDATE_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api Users
    Given un utilisateur a été créé
    And deux tenants et un rôle par défaut pour la mise à jour partielle d'un utilisateur
    And un utilisateur <(userRole) avec ou sans> les rôles ROLE_UPDATE_USERS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> les rôles ROLE_UPDATE_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur met à jour partiellement un utilisateur
    Then le serveur <autorise ou refuse> l'accès à l'API Users

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


  Scenario: Cas normal de mise à jour de lui-même
    Given un utilisateur a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_ME_USERS se met à jour partiellement dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_ME_USERS
    Then le serveur retourne l'utilisateur mis à jour partiellement

  Scenario: Cas de mise à jour de lui-même avec un autre identifiant
    Given un utilisateur a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_ME_USERS se met à jour partiellement avec un autre identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_ME_USERS
    Then le serveur retourne l'utilisateur mis à jour partiellement

  Scenario: Cas de mise à jour de lui-même sans le rôle ROLE_UPDATE_ME_USERS
    Given un utilisateur a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_ME_USERS se met à jour partiellement dans un tenant auquel il est autorisé en utilisant un certificat full access sans le rôle ROLE_UPDATE_ME_USERS
    Then le serveur refuse la mise à jour de lui-même
