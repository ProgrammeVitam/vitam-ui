@Api
@ApiIam
@ApiIamGroups
@ApiIamGroupsCreation
Feature: API Groups : création d'un nouveau groupe

	@Traces
  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_GROUPS ajoute un nouveau groupe dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS
    Then le serveur retourne le groupe créé
    And une trace de création du groupe est présente dans vitam

  Scenario: Cas readonly
    When un utilisateur avec le rôle ROLE_CREATE_GROUPS ajoute un nouveau groupe en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS
    Then le serveur refuse la création du groupe à cause du readonly

  Scenario: Cas mauvais client de l'utilisateur
    When un utilisateur avec le rôle ROLE_CREATE_GROUPS mais avec un mauvais client ajoute un nouveau groupe dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS
    Then le serveur refuse la création du groupe à cause du mauvais client

  Scenario: Cas client désactivé de l'utilisateur
    When un utilisateur avec le rôle ROLE_CREATE_GROUPS et un client désactivé ajoute un nouveau groupe dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS
    Then le serveur refuse la création du groupe à cause du client désactivé

  Scenario: Cas nom existant du groupe
    When un utilisateur avec le rôle ROLE_CREATE_GROUPS ajoute un nouveau groupe avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS
    Then le serveur refuse la création du groupe à cause du nom existant

  Scenario: Cas d'un profil inexistant
    When un utilisateur avec le rôle ROLE_CREATE_GROUPS ajoute un nouveau groupe avec un profil inexistant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS
    Then le serveur refuse la création du groupe à cause du profil inexistant

  Scenario: Cas d'un profil d'un autre client
    When un utilisateur avec le rôle ROLE_CREATE_GROUPS ajoute un nouveau groupe avec un profil d'un autre client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_GROUPS
    Then le serveur refuse la création du groupe à cause du profil d'un autre client

  Scenario Outline: Cas sécurité, par un utilisateur <(userRole) avec ou sans> les rôles ROLE_CREATE_GROUPS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> les rôles ROLE_CREATE_GROUPS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api groups
    Given deux tenants et un rôle par défaut pour l'ajout d'un groupe
    And un utilisateur <(userRole) avec ou sans> les rôles ROLE_CREATE_GROUPS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> les rôles ROLE_CREATE_GROUPS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur ajoute un nouveau groupe
    Then le serveur <autorise ou refuse> l'accès à l'API groups

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

  Scenario Outline: Création d'un groupe de niveau <(group) vide ou TEST ou TEST.BIS ou AUTRE> par un utilisateur de niveau <(user) vide ou TEST ou TEST.BIS ou AUTRE> => le serveur autorise l'accès
    Given un tenant et customer system
    And un niveau <(user) vide ou TEST ou TEST.BIS ou AUTRE>
    And un utilisateur de ce niveau avec le rôle ROLE_CREATE_GROUPS
    When cet utilisateur crée un nouveau groupe de niveau <(group) vide ou TEST ou TEST.BIS ou AUTRE>
    Then le serveur retourne le nouveau groupe
    And le niveau du nouveau groupe est bien le niveau <(group) vide ou TEST ou TEST.BIS ou AUTRE>

  Examples:
  | (user) vide ou TEST ou TEST.BIS ou AUTRE | (group) vide ou TEST ou TEST.BIS ou AUTRE | (group) vide ou TEST ou TEST.BIS ou AUTRE |
  | TEST | TEST.BIS | TEST.BIS |
  | vide | TEST.BIS | TEST.BIS |
  | vide | TEST | TEST |
  | vide | vide | vide |
  | vide | AUTRE | AUTRE |

  Scenario Outline: Création d'un groupe de niveau <(group) vide ou TEST ou TEST.BIS ou AUTRE> par un utilisateur de niveau <(user) vide ou TEST ou TEST.BIS ou AUTRE> => le serveur refuse l'accès
    Given un tenant et customer system
    And un niveau <(user) vide ou TEST ou TEST.BIS ou AUTRE>
    And un utilisateur de ce niveau avec le rôle ROLE_CREATE_GROUPS
    When cet utilisateur crée un nouveau groupe de niveau <(group) vide ou TEST ou TEST.BIS ou AUTRE>
    Then le serveur refuse l'accès pour cause de niveau non autorisé

  Examples:
  | (user) vide ou TEST ou TEST.BIS ou AUTRE | (group) vide ou TEST ou TEST.BIS ou AUTRE |
  | TEST | TEST |
  | TEST | vide |
  | TEST.BIS | vide |
  | TEST.BIS | TEST.BIS |
  | TEST.BIS | TEST |
  | AUTRE | vide |
  | AUTRE | TEST |
  | AUTRE | TEST.BIS |
  | TEST | AUTRE |
  | TEST.BIS | AUTRE |
  | AUTRE | AUTRE |
