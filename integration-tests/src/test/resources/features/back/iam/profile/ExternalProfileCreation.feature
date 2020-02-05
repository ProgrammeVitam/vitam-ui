@Api
@ApiIam
@ApiIamProfiles
@ApiIamProfilesCreation
Feature: API Profile : création de profils

	@Traces
  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un nouveau profil dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES
    Then le serveur retourne le profil créé
    And une trace de création de profil est présente dans vitam

  Scenario: Cas readonly
    When un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un profil en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES
    Then le serveur refuse la création du profil à cause du readonly

  Scenario: Cas mauvais client de l'utilisateur
    When un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un profil mais avec un mauvais client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES
    Then le serveur refuse la création du profil à cause du mauvais client

  Scenario: Cas mauvais tenant de l'utilisateur
    When un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un profil mais avec un mauvais tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES
    Then le serveur refuse la création du profil à cause du mauvais tenant

  Scenario: Cas nom existant du profil
    When un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un profil avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES
    Then le serveur refuse la création du profil à cause du nom existant

  Scenario: Cas d'un utilisateur avec un rôle qu'il ne possède pas
    When un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un profil avec un rôle qu'il ne possède pas dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES
    Then le serveur refuse la création du profil de l'utilisateur qui ne possède pas le bon rôle

  Scenario Outline: Cas sécurité, par un utilisateur <(userRole) avec ou sans> les rôles ROLE_CREATE_PROFILES sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> les rôles ROLE_CREATE_PROFILES <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api Profiles
    Given deux tenants et un rôle par défaut pour l'ajout d'un profil
    And un utilisateur <(userRole) avec ou sans> les rôles ROLE_CREATE_PROFILES sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> les rôles ROLE_CREATE_PROFILES <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur ajoute un nouveau profil
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

  Scenario Outline: Création d'un profil de niveau <(newProfil) vide ou TEST ou TEST.BIS ou AUTRE> par un utilisateur de niveau <(profil) vide ou TEST ou TEST.BIS ou AUTRE> => le serveur autorise l'accès
    Given un tenant et customer system
    And un niveau <(profil) vide ou TEST ou TEST.BIS ou AUTRE>
    And un utilisateur de ce niveau avec le rôle ROLE_CREATE_PROFILES
    When cet utilisateur crée un nouveau profil avec pour attribut le niveau <(newProfil) vide ou TEST ou TEST.BIS ou AUTRE>
    Then le serveur retourne le nouveau profil
    And le niveau du nouveau profil est bien le niveau <(newProfil) vide ou TEST ou TEST.BIS ou AUTRE>

  Examples:
  | (profil) vide ou TEST ou TEST.BIS ou AUTRE | (newProfil) vide ou TEST ou TEST.BIS ou AUTRE |
  | TEST | TEST.BIS |
  | vide | TEST.BIS |
  | vide | TEST |
  | vide | vide |
  | vide | AUTRE |

    Scenario Outline: Création d'un profil utilisateur de niveau <(newProfil) vide ou TEST ou TEST.BIS ou AUTRE> par un utilisateur de niveau <(profil) vide ou TEST ou TEST.BIS ou AUTRE> => le serveur autorise l'accès
    Given un tenant et customer system
    And un niveau <(profil) vide ou TEST ou TEST.BIS ou AUTRE>
    And un utilisateur de ce niveau avec les rôles ROLE_CREATE_PROFILES+ROLE_GET_USERS+ROLE_GET_GROUPS
    When cet utilisateur crée un nouveau profil utilisateur avec pour attribut le niveau <(newProfil) vide ou TEST ou TEST.BIS ou AUTRE>
    Then le serveur retourne le nouveau profil
    And le niveau du nouveau profil est bien le niveau <(newProfil) vide ou TEST ou TEST.BIS ou AUTRE>

  Examples:
  | (profil) vide ou TEST ou TEST.BIS ou AUTRE | (newProfil) vide ou TEST ou TEST.BIS ou AUTRE |
  | TEST | TEST.BIS |
  | vide | TEST.BIS |
  | vide | TEST |
  | vide | vide |
  | vide | AUTRE |

  Scenario Outline: Création d'un profil de niveau <(newProfil) vide ou TEST ou TEST.BIS ou AUTRE> par un utilisateur de niveau <(profil) vide ou TEST ou TEST.BIS ou AUTRE> => le serveur refuse l'accès
    Given un tenant et customer system
    And un niveau <(profil) vide ou TEST ou TEST.BIS ou AUTRE>
    And un utilisateur de ce niveau avec le rôle ROLE_CREATE_PROFILES
    When cet utilisateur crée un nouveau profil avec pour attribut le niveau <(newProfil) vide ou TEST ou TEST.BIS ou AUTRE>
    Then le serveur refuse l'accès pour cause de niveau non autorisé

  Examples:
  | (profil) vide ou TEST ou TEST.BIS ou AUTRE | (newProfil) vide ou TEST ou TEST.BIS ou AUTRE |
  | TEST | TEST |
  | TEST | vide |
  | TEST.BIS | vide |
  | TEST.BIS | TEST.BIS |
  | TEST.BIS | TEST |
  | AUTRE | vide |
  | AUTRE | TEST |
  | AUTRE | TEST.BIS |
  | TEST | AUTRE |
  | TEST.BIS | AUTRE |
  | AUTRE | AUTRE |

