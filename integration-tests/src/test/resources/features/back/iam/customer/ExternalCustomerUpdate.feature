@Api
@ApiIam
@ApiIamCustomers
@ApiIamCustomersUpdate
Feature: Opérations de mise à jour des clients

  Scenario: Mettre à jour un client
    Given un client a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_CUSTOMERS met à jour un client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_CUSTOMERS
    Then le serveur retourne le client mis à jour

  Scenario: Mettre à jour l'aspect subrogeable d'un client
    Given un client a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_CUSTOMERS rend le client subrogeable dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_CUSTOMERS
    Then le serveur retourne le client mis à jour sur les propriétés subrogeable

  Scenario: Désactiver l'OTP d'un client
    Given un client a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_CUSTOMERS désactive l'OTP d'un client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_CUSTOMERS
    Then le serveur retourne le client avec l'OTP désactivé
    And les utilisateurs du client ont leur OTP désactivé

  Scenario Outline: Mettre à jour un client, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_UPDATE_CUSTOMERS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_UPDATE_CUSTOMERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api Customers
    Given un client a été créé
    And deux tenants et un rôle par défaut pour la mise à jour d'un client
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_UPDATE_CUSTOMERS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_UPDATE_CUSTOMERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur met à jour un client
    Then le serveur <autorise ou refuse> l'accès à l'API Customers

  Examples:
    | (userRole) avec ou sans | (headerTenant) principal ou secondaire | (certRole) avec ou sans | (certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess | autorise ou refuse |
    | avec | principal | avec | sur le tenant principal | autorise |
    | sans | principal | avec | sur le tenant principal | refuse |
    | avec | secondaire | avec | sur le tenant principal | refuse |
    | sans | secondaire | avec | sur le tenant principal | refuse |
    | avec | principal | sans | sur le tenant principal | refuse |
    | sans | principal | sans | sur le tenant principal | refuse |
    | avec | secondaire | sans | sur le tenant principal | refuse |
    | sans | secondaire | sans | sur le tenant principal | refuse |
    | avec | principal | avec | sur le tenant secondaire | refuse |
    | sans | principal | avec | sur le tenant secondaire | refuse |
    | avec | secondaire | avec | sur le tenant secondaire | refuse |
    | sans | secondaire | avec | sur le tenant secondaire | refuse |
    | avec | principal | sans | sur le tenant secondaire | refuse |
    | sans | principal | sans | sur le tenant secondaire | refuse |
    | avec | secondaire | sans | sur le tenant secondaire | refuse |
    | sans | secondaire | sans | sur le tenant secondaire | refuse |
    | avec | principal | avec | étant fullAccess | autorise |
    | sans | principal | avec | étant fullAccess | refuse |
    | avec | secondaire | avec | étant fullAccess | refuse |
    | sans | secondaire | avec | étant fullAccess | refuse |
    | avec | principal | sans | étant fullAccess | refuse |
    | sans | principal | sans | étant fullAccess | refuse |
    | avec | secondaire | sans | étant fullAccess | refuse |
    | sans | secondaire | sans | étant fullAccess | refuse |

# ED Il n'est pas possible de créer un client non modifiable
  #Scenario: Mettre à jour un client non modifiable
    #Given un client a été créé sans être modifiable
    #When un utilisateur avec le rôle ROLE_UPDATE_CUSTOMERS met à jour un client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_CUSTOMERS
    #Then le serveur refuse la mise à jour du client
