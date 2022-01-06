@Api
@ApiIam
@ApiIamCustomers
@ApiIamCustomersCreation
Feature: API Customers : création d'un nouveau client

  @Traces
  @ApiIamCustomersCreationWithoutLogo
  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_CUSTOMERS ajoute un nouveau client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_CUSTOMERS
    Then le serveur retourne le client créé
    And un identity provider par défaut est associé au client
    And un tenant par défaut est créé dans vitamui
    And 23 profils sont créés pour le tenant principal
    And un utilisateur admin est associé au client
    And une trace de création d'un client est présente dans vitam

