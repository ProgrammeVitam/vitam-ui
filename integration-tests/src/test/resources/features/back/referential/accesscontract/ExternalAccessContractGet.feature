@Api
@ApiReferential
@ApiReferentialAccessContracts
@ApiReferentialAccessContractsGet
Feature: API contrat d'accès : récupérer

 Scenario: tous les contrat d'accès
    When un utilisateur avec le rôle ROLE_GET_ACCESS_CONTRACT récupère tous les contrat d'accès applicatifs en utilisant un certificat full access avec le rôle ROLE_GET_ACCESS_CONTRACT
    Then le serveur retourne tous les contrat d'accès

 Scenario: un contrat d'accès par son identifant
    When un utilisateur avec le rôle ROLE_GET_ACCESS_CONTRACT récupère un contrat d'accès par son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_ACCESS_CONTRACT
    Then le serveur retourne le contrat d'accès avec cet identifiant

 Scenario: tous les contrat d'accès par code et nom
    When un utilisateur avec le rôle ROLE_GET_ACCESS_CONTRACT récupère tous les contrat d'accès par code et nom en utilisant un certificat full access avec le rôle ROLE_GET_ACCESS_CONTRACT
    Then le serveur retourne les contrat d'accès par code et nom

 Scenario: tous les contrat d'accès par code ou nom
    When un utilisateur avec le rôle ROLE_GET_ACCESS_CONTRACT récupère tous les contrat d'accès par code ou nom en utilisant un certificat full access avec le rôle ROLE_GET_ACCESS_CONTRACT
    Then le serveur retourne les contrat d'accès par code ou nom

 Scenario: tous les contrat d'accès avec pagination
    When un utilisateur avec le rôle ROLE_GET_ACCESS_CONTRACT récupère tous les contrat d'accès avec pagination en utilisant un certificat full access avec le rôle ROLE_GET_ACCESS_CONTRACT
    Then le serveur retourne les contrat d'accès paginés

 Scenario: l'historique d'un contrat d'accès
    When un utilisateur avec le rôle ROLE_GET_ACCESS_CONTRACT récupère l'historique d'un contrat d'accès à partir de son son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_ACCESS_CONTRACT
    Then le serveur retourne l'historique du contrat d'accès





