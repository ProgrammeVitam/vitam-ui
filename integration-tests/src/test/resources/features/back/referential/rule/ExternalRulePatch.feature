@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsPatch
Feature: API contextes : mise à jour d'une règle

 Scenario: mise à jour du type d'une règle
    Given la règle RuleTest existe
    Given la règle RuleTest a ses valeurs par défaut
    When un utilisateur avec le rôle ROLE_GET_RULES modifie le type d'une règle en utilisant un certificat full access avec le rôle ROLE_GET_RULES
    Then le type de la règle est à jour

 Scenario: mise à jour de la durée d'une règle
    Given la règle RuleTest existe
    Given la règle RuleTest a ses valeurs par défaut
    When un utilisateur avec le rôle ROLE_GET_RULES modifie la durée d'une règle en utilisant un certificat full access avec le rôle ROLE_GET_RULES
    Then la durée de la règle est à jour

 Scenario: mise à jour de la mesure de la durée d'une règle
    Given la règle RuleTest existe
    Given la règle RuleTest a ses valeurs par défaut
    When un utilisateur avec le rôle ROLE_GET_RULES modifie la mesure de la durée d'une règle en utilisant un certificat full access avec le rôle ROLE_GET_RULES
    Then la mesure de la durée de la règle est à jour

 Scenario: mise à jour de la description d'une règle
    Given la règle RuleTest existe
    Given la règle RuleTest a ses valeurs par défaut
    When un utilisateur avec le rôle ROLE_GET_RULES modifie la description d'une règle en utilisant un certificat full access avec le rôle ROLE_GET_RULES
    Then la description de la règle est à jour

 Scenario: mise à jour de tous les champs d'une règle
    Given la règle RuleTest existe
    Given la règle RuleTest a ses valeurs par défaut
    When un utilisateur avec le rôle ROLE_GET_RULES modifie les champs d'une règle en utilisant un certificat full access avec le rôle ROLE_GET_RULES
    Then les champs de la règle sont à jour
