@Api
@ApiArchiveSearch
@ApiSearchCriteriaHistoryCreation
Feature: API SearchCriteriaHistory : création d'un nouveau critère de recherche

  Scenario: Cas création d'un nouveau critère de recherche
    When un utilisateur avec le rôle ROLE_ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH ajoute un nouveau critère de recherche en utilisant un certificat full access avec le rôle ROLE_CREATE_ARCHIVE_SEARCH
    Then le serveur retourne le nouveau critère créé

