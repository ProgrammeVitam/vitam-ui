@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsDelete
Feature: API contextes : suppression d'une regle

 Scenario: suppression d'une regle
    Given la regle RuleTest existe
    When un utilisateur avec le role ROLE_GET_RULES une regle en utilisant un certificat full access avec le role ROLE_GET_RULES
    Then la regle n'existe pas
