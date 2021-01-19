@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsGet
Feature: API contextes : mettre à jour

  Scenario: un contexte
    Given un contexte a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_CONTEXTS met à jour un context en utilisant un certificat full access avec le rôle ROLE_UPDATE_CONTEXTS
    Then le serveur retourne le contexte mis à jour






 
