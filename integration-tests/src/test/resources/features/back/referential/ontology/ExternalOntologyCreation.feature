@Api
@ApiReferential
@ApiReferentialOntologies
@ApiReferentialOntologiesCreation
Feature: API Ontologies : Création et import d'ontologies

  Scenario: Import d'un fichier référentiel d'ontologies valide
    When un utilisateur importe des ontologies à partir d'un fichier json valide
    Then l'import des ontologies a réussi

  Scenario: Import d'un fichier référentiel d'ontologies invalide
    When un utilisateur importe des ontologies à partir d'un fichier json invalide
    Then l'import des ontologies a échoué
