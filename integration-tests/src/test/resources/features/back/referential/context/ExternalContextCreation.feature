@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsCreation
Feature: API Contextes : creation d'un nouveau contexte applicatif

  Scenario: Cas normal
    When un utilisateur avec le role ROLE_CREATE_CONTEXTS ajoute un nouveau contexte en utilisant un certificat full access avec le role ROLE_CREATE_CONTEXTS
    Then le serveur retourne le contexte cree
    And 2 permissions sont crees dans le contexte
    #And une trace de creation d'un contexte est presente dans vitam
