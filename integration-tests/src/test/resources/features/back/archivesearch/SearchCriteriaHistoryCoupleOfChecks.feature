@Api
@ApiArchiveSearch
@ApiSearchCriteriaHistoryCoupleOfChecks
Feature: API search criteria history : récupérer

  Scenario: les critères de recherche enregistrés
    Given un critere de recherche a été créé
    When l'utilisateur avec le rôle ROLE_ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH récupère tous les critères enregistrés
    Then le nom de la recherche est "Search Name"
      And la liste des critères est de taille 2
      And l'utilisateur associé a l'identifiant "1"
