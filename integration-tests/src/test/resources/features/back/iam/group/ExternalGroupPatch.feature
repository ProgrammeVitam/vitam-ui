@Api
@ApiIam
@ApiIamGroups
@ApiIamGroupsPatch
Feature: API Groups : mise à jour partielle d'un groupe

	@Traces
  Scenario: Cas normal
    Given un groupe a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_GROUPS met à jour partiellement un groupe dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS
    Then le serveur retourne le groupe partiellement mis à jour
    And une trace de mise à jour du groupe est présente dans vitam

  Scenario: Cas readonly
    Given un groupe a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_GROUPS met à jour partiellement un groupe en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS
    Then le serveur refuse la mise à jour partielle du groupe à cause du readonly

  Scenario: Cas mauvais client de l'utilisateur
    Given un groupe a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_GROUPS mais avec un mauvais client met à jour partiellement un groupe dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS
    Then le serveur refuse la mise à jour partielle du groupe à cause du mauvais client

  Scenario: Cas nom existant du groupe
    Given un groupe a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_GROUPS met à jour partiellement un groupe avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS
    Then le serveur refuse la mise à jour partielle du groupe à cause du nom existant

  Scenario: Cas d'un utilisateur sans le bon niveau
    Given un groupe a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_GROUPS sans le bon niveau met à jour partiellement un groupe dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS
    Then le serveur refuse la mise à jour partielle du groupe à cause du mauvais niveau

  Scenario: Cas d'un profil inexistant
    Given un groupe a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_GROUPS met à jour partiellement un groupe avec un profil inexistant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS
    Then le serveur refuse la mise à jour partielle du groupe à cause du profil inexistant

  Scenario: Cas d'un profil d'un autre client
    Given un groupe a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_GROUPS met à jour partiellement un groupe avec un profil d'un autre client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_GROUPS
    Then le serveur refuse la mise à jour partielle du groupe à cause du profil d'un autre client

  Scenario Outline: Cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_UPDATE_GROUPS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_UPDATE_GROUPS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api Groups
    Given un groupe a été créé
    And deux tenants et un rôle par défaut pour la mise à jour partielle d'un groupe
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_UPDATE_GROUPS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_UPDATE_GROUPS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur met à jour partiellement un groupe
    Then le serveur <autorise ou refuse> l'accès à l'API Groups

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
