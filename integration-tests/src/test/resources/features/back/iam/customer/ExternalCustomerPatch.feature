@Api
@ApiIam
@ApiIamCustomers
@ApiIamCustomersPatch
Feature: API Customers : mise à jour partielle d'un client

	@Traces
  Scenario: Cas normal
    Given un client a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_CUSTOMERS met à jour partiellement un client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_CUSTOMERS
    Then le serveur retourne le client partiellement mis à jour
    And une trace de mise à jour du client est présente dans vitam
