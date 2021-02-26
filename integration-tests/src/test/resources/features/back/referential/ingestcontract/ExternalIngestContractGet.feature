@Api
@ApiReferential
@ApiReferentialIngestContracts
@ApiReferentialIngestContractsGet
Feature: API contrat d'entrée : récupérer

 Scenario: tous les contrat d'entrée
    When un utilisateur avec le rôle ROLE_GET_INGEST_CONTRACT récupère tous les contrat d'entrée applicatifs en utilisant un certificat full access avec le rôle ROLE_GET_INGEST_CONTRACT
    Then le serveur retourne tous les contrat d'entrée

 Scenario: un contrat d'entrée par son identifant
    When un utilisateur avec le rôle ROLE_GET_INGEST_CONTRACT récupère un contrat d'entrée par son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_INGEST_CONTRACT
    Then le serveur retourne le contrat d'entrée avec cet identifiant

 Scenario: tous les contrat d'entrée par code et nom
    When un utilisateur avec le rôle ROLE_GET_INGEST_CONTRACT récupère tous les contrat d'entrée par code et nom en utilisant un certificat full access avec le rôle ROLE_GET_INGEST_CONTRACT
    Then le serveur retourne les contrat d'entrée par code et nom

 Scenario: tous les contrat d'entrée par code ou nom
    When un utilisateur avec le rôle ROLE_GET_INGEST_CONTRACT récupère tous les contrat d'entrée par code ou nom en utilisant un certificat full access avec le rôle ROLE_GET_INGEST_CONTRACT
    Then le serveur retourne les contrat d'entrée par code ou nom

 Scenario: tous les contrat d'entrée avec pagination
    When un utilisateur avec le rôle ROLE_GET_INGEST_CONTRACT récupère tous les contrat d'entrée avec pagination en utilisant un certificat full access avec le rôle ROLE_GET_INGEST_CONTRACT
    Then le serveur retourne les contrat d'entrée paginés

 Scenario: l'historique d'un contrat d'entrée
    When un utilisateur avec le rôle ROLE_GET_INGEST_CONTRACT récupère l'historique d'un contrat d'entrée à partir de son son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_INGEST_CONTRACT
    Then le serveur retourne l'historique du contrat d'entrée





