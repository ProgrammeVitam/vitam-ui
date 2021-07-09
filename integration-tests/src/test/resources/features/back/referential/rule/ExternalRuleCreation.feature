@Api
@ApiReferential
@ApiReferentialRules
@ApiReferentialRulesCreation
Feature: API Rules : creation d'une nouvelle regle

  Scenario: Cas normal
    Given la règle RuleTest n'existe pas
    When un utilisateur avec le rôle ROLE_CREATE_RULES ajoute une nouvelle règle en utilisant un certificat full access avec le rôle ROLE_CREATE_RULES
    Then le serveur retourne la règle créée

  Scenario: Import d'un fichier référentiel de règles valide
    When un utilisateur importe des règles à partir d'un fichier csv valide
    Then l'import des règles a réussi

  Scenario: Import d'un fichier référentiel de règles invalide
    When un utilisateur importe des règles à partir d'un fichier csv invalide
    Then l'import des règles a échoué
