@Api
@ApiIam
@ApiIamUsers
@ApiIamUsersCreation
Feature: API User : création d'utilisateurs

	@Traces
	Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_USERS ajoute un nouvel utilisateur dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_USERS
    Then le serveur retourne l'utilisateur créé
    And une trace de création utilisateur est présente dans vitam

  Scenario: Cas readonly
    When un utilisateur avec le rôle ROLE_CREATE_USERS ajoute un nouvel utilisateur en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_USERS
    Then le serveur refuse la création de l'utilisateur à cause du readonly

  Scenario: Cas mauvais client de l'utilisateur
    When un utilisateur avec le rôle ROLE_CREATE_USERS ajoute un nouvel utilisateur mais avec un mauvais client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_USERS
    Then le serveur refuse la création de l'utilisateur à cause du mauvais client

  Scenario: Cas email existant de l'utilisateur
    When un utilisateur avec le rôle ROLE_CREATE_USERS ajoute un nouvel utilisateur mais avec un email existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_USERS
    Then le serveur refuse la création de l'utilisateur à cause de l'email existant

  Scenario: Cas groupe inexistant de l'utilisateur
    When un utilisateur avec le rôle ROLE_CREATE_USERS ajoute un nouvel utilisateur mais avec un groupe inexistant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_USERS
    Then le serveur refuse la création de l'utilisateur à cause du groupe inexistant

  Scenario Outline: Cas sécurité, par un utilisateur <(userRole) avec ou sans> les rôles ROLE_CREATE_USERS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> les rôles ROLE_CREATE_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api Users
    Given deux tenants et un rôle par défaut pour l'ajout d'un utilisateur
    And un utilisateur <(userRole) avec ou sans> les rôles ROLE_CREATE_USERS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> les rôles ROLE_CREATE_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur ajoute un nouvel utilisateur
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

  Scenario Outline: Création d'un utilisateur de niveau <(newUser) vide ou TEST ou TEST.BIS ou AUTRE> par un utilisateur de niveau <(user) vide ou TEST ou TEST.BIS ou AUTRE> => le serveur autorise l'accès
    Given un tenant et customer system
    And un niveau <(user) vide ou TEST ou TEST.BIS ou AUTRE>
    And un utilisateur de ce niveau avec le rôle ROLE_CREATE_USERS
    And un groupe de niveau <(newUser) vide ou TEST ou TEST.BIS ou AUTRE>
    When cet utilisateur crée un nouvel utilisateur avec pour attribut ce groupe
    Then le serveur retourne le nouvel utilisateur
    And le nouvel utilisateur est bien affecté au groupe donné
    And le niveau du nouvel utilisateur est bien le niveau <(newUser) vide ou TEST ou TEST.BIS ou AUTRE>

  Examples:
  | (user) vide ou TEST ou TEST.BIS ou AUTRE | (newUser) vide ou TEST ou TEST.BIS ou AUTRE |
  | TEST | TEST.BIS |
  | vide | TEST.BIS |
  | vide | TEST |
  | vide | vide |
  | vide | AUTRE |

  Scenario Outline: Création d'un utilisateur de niveau <(newUser) vide ou TEST ou TEST.BIS ou AUTRE> par un utilisateur de niveau <(user) vide ou TEST ou TEST.BIS ou AUTRE> => le serveur refuse l'accès
    Given un tenant et customer system
    And un niveau <(user) vide ou TEST ou TEST.BIS ou AUTRE>
    And un utilisateur de ce niveau avec le rôle ROLE_CREATE_USERS
    And un groupe de niveau <(newUser) vide ou TEST ou TEST.BIS ou AUTRE>
    When cet utilisateur crée un nouvel utilisateur avec pour attribut ce groupe
    Then le serveur refuse l'accès l'API Users

  Examples:
  | (user) vide ou TEST ou TEST.BIS ou AUTRE | (newUser) vide ou TEST ou TEST.BIS ou AUTRE |
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
