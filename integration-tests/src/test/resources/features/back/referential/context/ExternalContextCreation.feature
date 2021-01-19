@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsCreation
Feature: API Contextes : création d'un nouveau contexte applicatif

  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_CONTEXTS ajoute un nouveau contexte en utilisant un certificat full access avec le rôle ROLE_CREATE_CONTEXTS
    Then le serveur retourne le contexte créé
    And 2 permissions sont créés dans le contexte
    #And une trace de création d'un contexte est présente dans vitam
