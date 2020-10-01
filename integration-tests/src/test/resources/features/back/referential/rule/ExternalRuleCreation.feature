@Api
@ApiReferential
@ApiReferentialRules
@ApiReferentialRulesCreation
Feature: API Rules : création d'une nouvelle règle

  Scenario: Cas normal
    Given la règle RuleTest n'existe pas
    When un utilisateur avec le rôle ROLE_CREATE_RULES ajoute une nouvelle règle en utilisant un certificat full access avec le rôle ROLE_CREATE_RULES
    Then le serveur retourne la règle créée
