@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsCheck
Feature: API contextes : verifier l'existence

  Scenario: d'un contexte par son identifiant
    When un utilisateur verifie l'existence d'un contexte par son identifiant
    Then le serveur retourne vrai

  Scenario: d'un contexte par son code et son nom
    When un utilisateur verifie l'existence d'un contexte par son code et son nom
    Then le serveur retourne vrai


