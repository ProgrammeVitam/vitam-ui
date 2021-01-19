@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsCheck
Feature: API contextes : vérifier l'existence

  Scenario: d'un contexte par son identifiant
    When un utilisateur vérifie l'existence d'un contexte par son identifiant
    Then le serveur retourne vrai

  Scenario: d'un contexte par son code et son nom
    When un utilisateur vérifie l'existence d'un contexte par son code et son nom
    Then le serveur retourne vrai


