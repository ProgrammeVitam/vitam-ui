@Api
@ApiIam
@ApiIamTenants
@ApiIamTenantsUpdate
Feature: Opérations de mise à jour des tenants

  Scenario: Mettre à jour un tenant
    Given un tenant a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_TENANTS met à jour un tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_TENANTS
    Then le serveur retourne le tenant mis à jour

  Scenario: Mettre à jour un tenant en readonly
      Given un tenant a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_TENANTS met à jour un tenant en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_TENANTS
    Then le serveur refuse la mise à jour du tenant

  Scenario: Mettre à jour un tenant par un utilisateur sans le rôle ROLE_UPDATE_TENANTS
    Given un tenant a été créé
    When un utilisateur sans le rôle ROLE_UPDATE_TENANTS met à jour un tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_TENANTS
    Then le serveur refuse l'accès à l'API tenants

  Scenario: Cas nom existant du tenant
    Given un tenant a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_TENANTS met à jour un tenant avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_TENANTS
    Then le serveur refuse la mise à jour du tenant

