@Api
@ApiReferential
@ApiReferentialRules
@ApiReferentialRulesCheck
Feature: API Rules : vérifier l'existence d'une règle

  Scenario: vérifier l'existence d'une règle par son identifiant
    Given la règle RuleTest existe
    When un utilisateur vérifie l'existence de la règle RuleTest par son identifiant
    Then le serveur retourne vrai

  Scenario: vérifier l'existence d'une règle par son identifiant
    Given la règle RuleTest n'existe pas
    When un utilisateur vérifie l'existence de la règle RuleTest par son identifiant
    Then le serveur retourne faux


