@Api
@ApiReferential
@ApiReferentialAccessContracts
@ApiReferentialAccessContractsCreation
Feature: API AccessContracts : création d'un nouveau contrat d'accès applicatif

  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_ACCESS_CONTRACT ajoute un nouveau contrat d'accès en utilisant un certificat full access avec le rôle ROLE_CREATE_ACCESS_CONTRACT
    Then le serveur retourne le contrat d'accès créé
