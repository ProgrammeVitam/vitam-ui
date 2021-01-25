@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsGet
Feature: API contextes : recuperation des regles

 Scenario: toutes les regles
    When un utilisateur avec le role ROLE_GET_RULES recupere toutes les regles en utilisant un certificat full access avec le role ROLE_GET_RULES
    Then le serveur retourne toutes les regles

 Scenario: une regle par son identifant
    Given la regle RuleTest existe
    When un utilisateur avec le role ROLE_GET_RULES recupere une regle par son identifiant en utilisant un certificat full access avec le role ROLE_GET_RULES
    Then le serveur retourne la regle avec cet identifiant

 Scenario: toutes les regle par son intitule de regle
    Given la regle RuleTest existe
    When un utilisateur avec le role ROLE_GET_RULES recupere toutes les regles par intitule en utilisant un certificat full access avec le role ROLE_GET_RULES
    Then le serveur retourne toutes les regle avec cet intitule

 Scenario: toutes les regle par son intitule de regle
    Given la regle RuleTest existe
    When un utilisateur avec le role ROLE_GET_RULES recupere toutes les regles par type en utilisant un certificat full access avec le role ROLE_GET_RULES
    Then le serveur retourne toutes les regle avec ce type

 Scenario: toutes les regles avec pagination
    When un utilisateur avec le role ROLE_GET_CONTEXTS recupere toutes les regles avec pagination en utilisant un certificat full access avec le role ROLE_GET_CONTEXTS
    Then le serveur retourne les regles paginees
