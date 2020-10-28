@Api
@ApiReferential
@ApiReferentialIngestContracts
@ApiReferentialIngestContractsCreation
Feature: API IngestContracts : création d'un nouveau contrat d'entrée applicatif

  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_INGEST_CONTRACT ajoute un nouveau contrat d'entrée en utilisant un certificat full access avec le rôle ROLE_CREATE_INGEST_CONTRACT
    Then le serveur retourne le contrat d'entrée créé
