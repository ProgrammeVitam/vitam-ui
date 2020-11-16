@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsPatch
Feature: API contextes : mise a jour d'une regle

 Scenario: mise a jour du type d'une regle
    Given la regle RuleTest existe
    Given la regle RuleTest a ses valeurs par defaut
    When un utilisateur avec le role ROLE_GET_RULES modifie le type d'une regle en utilisant un certificat full access avec le role ROLE_GET_RULES
    Then le type de la regle est a jour

 Scenario: mise a jour de la duree d'une regle
    Given la regle RuleTest existe
    Given la regle RuleTest a ses valeurs par defaut
    When un utilisateur avec le role ROLE_GET_RULES modifie la duree d'une regle en utilisant un certificat full access avec le role ROLE_GET_RULES
    Then la duree de la regle est a jour

 Scenario: mise a jour de la mesure de la duree d'une regle
    Given la regle RuleTest existe
    Given la regle RuleTest a ses valeurs par defaut
    When un utilisateur avec le role ROLE_GET_RULES modifie la mesure de la duree d'une regle en utilisant un certificat full access avec le role ROLE_GET_RULES
    Then la mesure de la duree de la regle est a jour

 Scenario: mise a jour de la description d'une regle
    Given la regle RuleTest existe
    Given la regle RuleTest a ses valeurs par defaut
    When un utilisateur avec le role ROLE_GET_RULES modifie la description d'une regle en utilisant un certificat full access avec le role ROLE_GET_RULES
    Then la description de la regle est a jour

 Scenario: mise a jour de tous les champs d'une regle
    Given la regle RuleTest existe
    Given la regle RuleTest a ses valeurs par defaut
    When un utilisateur avec le role ROLE_GET_RULES modifie les champs d'une regle en utilisant un certificat full access avec le role ROLE_GET_RULES
    Then les champs de la regle sont a jour
