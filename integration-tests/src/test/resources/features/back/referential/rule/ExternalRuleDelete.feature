@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsDelete
Feature: API contextes : suppression d'une règle

 Scenario: suppression d'une règle
    Given la règle RuleTest existe
    When un utilisateur avec le rôle ROLE_GET_RULES une règle en utilisant un certificat full access avec le rôle ROLE_GET_RULES
    Then la règle n'est pas
