@Api
@ApiIam
@ApiIamUsers
@ApiIamUsersGet
Feature: API Users : récupération d'utilisateurs

  Scenario: Tous les utilisateurs
    When un utilisateur avec le rôle ROLE_GET_USERS récupère tous les utilisateurs dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS
    Then le serveur renvoie une erreur car la récupération de tous les utilisateurs n'est pas possible


  Scenario: Un utilisateur par son identifant
    When un utilisateur avec le rôle ROLE_GET_USERS récupère un utilisateur par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS
    Then le serveur retourne l'utilisateur avec cet identifiant

  Scenario: Un utilisateur d'un autre client par son identifant
    When un utilisateur avec le rôle ROLE_GET_USERS récupère un utilisateur d'un autre client par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS
    Then le serveur ne renvoie aucun utilisateur

  Scenario: Un utilisateur par son identifant mais sans le bon niveau
    When un utilisateur avec le rôle ROLE_GET_USERS sans le bon niveau récupère un utilisateur par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS
    Then le serveur ne renvoie aucun utilisateur

  Scenario Outline: Un utilisateur par son identifant, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_USERS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_GET_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api Users
    Given deux tenants et un rôle par défaut pour la récupération d'utilisateurs
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_USERS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_GET_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur récupère un utilisateur par son identifiant
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


  Scenario: Tous les utilisateurs avec pagination
    When un utilisateur avec le rôle ROLE_GET_USERS récupère tous les utilisateurs avec pagination dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS
    Then le serveur retourne les utilisateurs paginés

  Scenario: Tous les utilisateurs d'un autre client avec pagination
    When un utilisateur avec le rôle ROLE_GET_USERS récupère tous les utilisateurs d'un autre client avec pagination dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS
    Then le serveur retourne les utilisateurs paginés du bon client

  Scenario Outline: Tous les utilisateurs avec pagination, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_USERS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_GET_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api Users
    Given deux tenants et un rôle par défaut pour la récupération d'utilisateurs
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_USERS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_GET_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur récupère tous les utilisateurs avec pagination
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


  Scenario Outline: Récupération paginée des utilisateurs de niveau <(get) vide ou TEST ou TEST.BIS ou AUTRE> par un utilisateur de niveau <(user) vide ou TEST ou TEST.BIS ou AUTRE>
    Given un tenant et customer system
    And il existe plusieurs utilisateurs de différents niveaux
      | vide | TEST | TEST.BIS | AUTRE |
    And un niveau <(user) vide ou TEST ou TEST.BIS ou AUTRE>
    And un utilisateur de ce niveau avec le rôle ROLE_GET_USERS
    When cet utilisateur récupère les utilisateurs de niveau <(get) vide ou TEST ou TEST.BIS ou AUTRE>
    Then la liste renvoyée par le serveur <(vide) contient ou non> l'utilisateur de niveau vide
    And la liste renvoyée par le serveur <(TEST) contient ou non> l'utilisateur de niveau TEST
    And la liste renvoyée par le serveur <(TEST.BIS) contient ou non> l'utilisateur de niveau TEST.BIS
    And la liste renvoyée par le serveur <(AUTRE) contient ou non> l'utilisateur de niveau AUTRE
    And la liste renvoyée par le serveur <(mainUser) contient ou non> l'utilisateur ayant effectué l'action de récupération

  Examples:
  | (user) vide ou TEST ou TEST.BIS ou AUTRE | (get) vide ou TEST ou TEST.BIS ou AUTRE | (vide) contient ou non | (TEST) contient ou non | (TEST.BIS) contient ou non | (AUTRE) contient ou non | (mainUser) contient ou non |
  | TEST | TEST | ne contient pas | ne contient pas | ne contient pas | ne contient pas | contient |
  | TEST | TEST.BIS | ne contient pas | ne contient pas | contient | ne contient pas | ne contient pas |
  | TEST | AUTRE | ne contient pas | ne contient pas | ne contient pas | ne contient pas | ne contient pas |
  | TEST | vide | ne contient pas | ne contient pas | ne contient pas | ne contient pas | ne contient pas |
  | TEST.BIS | TEST.BIS | ne contient pas | ne contient pas | ne contient pas | ne contient pas | contient |
  | TEST.BIS | TEST | ne contient pas | ne contient pas | ne contient pas | ne contient pas | ne contient pas |
  | TEST.BIS | AUTRE | ne contient pas | ne contient pas | ne contient pas | ne contient pas | ne contient pas |
  | TEST.BIS | vide | ne contient pas | ne contient pas | ne contient pas | ne contient pas | ne contient pas |
  | AUTRE | AUTRE | ne contient pas | ne contient pas | ne contient pas | ne contient pas | contient |
  | AUTRE | TEST | ne contient pas | ne contient pas | ne contient pas | ne contient pas | ne contient pas |
  | AUTRE | TEST.BIS | ne contient pas | ne contient pas | ne contient pas | ne contient pas | ne contient pas |
  | AUTRE | vide | ne contient pas | ne contient pas | ne contient pas | ne contient pas | ne contient pas |
  | vide | vide | contient | ne contient pas | ne contient pas | ne contient pas | contient |
  | vide | AUTRE | ne contient pas | ne contient pas | ne contient pas | contient | ne contient pas |
  | vide | TEST | ne contient pas | contient | ne contient pas | ne contient pas | ne contient pas |
  | vide | TEST.BIS | ne contient pas | ne contient pas | contient | ne contient pas | ne contient pas |

  Scenario Outline: Récupération paginée de tous les utilisateurs par un utilisateur de niveau <(user) vide ou TEST ou TEST.BIS ou AUTRE>
    Given un tenant et customer system
    And il existe plusieurs utilisateurs de différents niveaux
      | vide | TEST | TEST.BIS | AUTRE |
    And un niveau <(user) vide ou TEST ou TEST.BIS ou AUTRE>
    And un utilisateur de ce niveau avec le rôle ROLE_GET_USERS
    When cet utilisateur récupère tous les utilisateurs
    Then la liste renvoyée par le serveur <(vide) contient ou non> l'utilisateur de niveau vide
    And la liste renvoyée par le serveur <(TEST) contient ou non> l'utilisateur de niveau TEST
    And la liste renvoyée par le serveur <(TEST.BIS) contient ou non> l'utilisateur de niveau TEST.BIS
    And la liste renvoyée par le serveur <(AUTRE) contient ou non> l'utilisateur de niveau AUTRE
    And la liste renvoyée par le serveur <(mainUser) contient ou non> l'utilisateur ayant effectué l'action de récupération

  Examples:
  | (user) vide ou TEST ou TEST.BIS ou AUTRE | (vide) contient ou non | (TEST) contient ou non | (TEST.BIS) contient ou non | (AUTRE) contient ou non | (mainUser) contient ou non |
  | TEST | ne contient pas | ne contient pas | contient | ne contient pas | contient |
  | TEST.BIS | ne contient pas | ne contient pas | ne contient pas | ne contient pas | contient |
  | AUTRE | ne contient pas | ne contient pas | ne contient pas | ne contient pas | contient |
  | vide | contient | contient | contient | contient | contient |
