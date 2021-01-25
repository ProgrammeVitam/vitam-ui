@Api
@ApiReferential
@ApiReferentialAgencies
@ApiReferentialAgenciesCreation
Feature: API Agencies : Création et import d'agences

  Scenario: Import d'un fichier référentiel d'agences valide
    When un utilisateur importe des agences à partir d'un fichier csv valide
    Then l'import des agences a réussi

  Scenario: Import d'un fichier référentiel d'agences invalide
    When un utilisateur importe des agences à partir d'un fichier csv invalide
    Then l'import des agences a échoué
