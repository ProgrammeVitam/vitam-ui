@Api
@ApiReferential
@ApiReferentialRules
@ApiReferentialRulesCreation
Feature: API Rules : creation d'une nouvelle regle

  Scenario: Cas normal
    Given la regle RuleTest n'existe pas
    When un utilisateur avec le role ROLE_CREATE_RULES ajoute une nouvelle regle en utilisant un certificat full access avec le role ROLE_CREATE_RULES
    Then le serveur retourne la regle creee
