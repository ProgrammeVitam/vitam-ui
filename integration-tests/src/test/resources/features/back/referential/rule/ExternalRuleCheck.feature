@Api
@ApiReferential
@ApiReferentialRules
@ApiReferentialRulesCheck
Feature: API Rules : verifier l'existence d'une regle

  Scenario: verifier l'existence d'une regle par son identifiant
    Given la regle RuleTest existe
    When un utilisateur verifie l'existence de la regle RuleTest par son identifiant
    Then le serveur retourne vrai

  Scenario: verifier l'existence d'une regle par son identifiant
    Given la regle RuleTest n'existe pas
    When un utilisateur verifie l'existence de la regle RuleTest par son identifiant
    Then le serveur retourne faux


