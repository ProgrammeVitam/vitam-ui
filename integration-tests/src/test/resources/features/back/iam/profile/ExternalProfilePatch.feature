@Api
@ApiIam
@ApiIamProfiles
@ApiIamProfilesPatch
Feature: API Profiles : mise à jour partielle d'un profil

	@Traces
  Scenario: Cas normal
    Given un profil a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES
    Then le serveur retourne le profil partiellement mis à jour
    And une trace de mise à jour du profil est présente dans vitam

  Scenario: Cas readonly
    Given un profil a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES
    Then le serveur refuse la mise à jour partielle du profil à cause du readonly

  Scenario: Cas mauvais client de l'utilisateur
    Given un profil a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil mais avec un mauvais client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES
    Then le serveur refuse la mise à jour partielle du profil à cause du mauvais client

  Scenario: Cas mauvais tenant de l'utilisateur
    Given un profil a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil mais avec un mauvais tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES
    Then le serveur refuse la mise à jour partielle du profil à cause du mauvais tenant

  Scenario: Cas nom existant du profil
    Given un profil a été créé
    # Nous avons besoin d'initialiser le profil avec le nom que nous voulons utiliser avant de tester la modification
    When un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES
    When un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES
    Then le serveur refuse la mise à jour partielle du profil à cause du nom existant

  Scenario: Cas d'un utilisateur sans le bon niveau
    Given un profil a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_PROFILES sans le bon niveau met à jour partiellement un profil dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES
    Then le serveur refuse la mise à jour partielle du profil à cause du mauvais niveau

  Scenario: Cas d'un profil désactive
    Given un profil a été créé
    And un groupe a été créé
    And le profil est ajouté au groupe précédemment créé
    When un utilisateur avec le rôle ROLE_UPDATE_PROFILES désactive un profil dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES
    Then le serveur refuse la mise à jour partielle du profil à cause de la désactivation

  Scenario: Cas d'un utilisateur avec un rôle qu'il ne possède pas
    Given un profil a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_PROFILES met à jour partiellement un profil avec un rôle qu'il ne possède pas dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROFILES
    Then le serveur refuse la mise à jour partielle du profil de l'utilisateur qui ne possède pas le bon rôle

  Scenario Outline: Cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_UPDATE_PROFILES sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_UPDATE_PROFILES <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api profiles
    Given un profil a été créé
    And deux tenants et un rôle par défaut pour la mise à jour partielle d'un profil
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_UPDATE_PROFILES sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_UPDATE_PROFILES <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur met à jour partiellement un profil
    Then le serveur <autorise ou refuse> l'accès à l'API profiles

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
