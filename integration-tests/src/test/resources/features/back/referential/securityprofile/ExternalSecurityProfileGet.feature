@Api
@ApiReferential
@ApiReferentialSecurityProfile
@ApiReferentialSecurityProfileGet
Feature: API SecurityProfile : récupérer

 Scenario: tous les profile de sécurité
    When un utilisateur avec le rôle ROLE_GET_SECURITY_PROFILE récupère tous les profile de sécurité applicatifs en utilisant un certificat full access avec le rôle ROLE_GET_SECURITY_PROFILE
    Then le serveur retourne tous les profile de sécurité

 Scenario: un profile de sécurité par son identifant
    When un utilisateur avec le rôle ROLE_GET_SECURITY_PROFILE récupère un profile de sécurité par son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_SECURITY_PROFILE
    Then le serveur retourne le profile de sécurité avec cet identifiant

 Scenario: tous les profile de sécurité par code et nom
    When un utilisateur avec le rôle ROLE_GET_SECURITY_PROFILE récupère tous les profile de sécurité par code et nom en utilisant un certificat full access avec le rôle ROLE_GET_SECURITY_PROFILE
    Then le serveur retourne les profile de sécurité par code et nom

 Scenario: tous les profile de sécurité par code ou nom
    When un utilisateur avec le rôle ROLE_GET_SECURITY_PROFILE récupère tous les profile de sécurité par code ou nom en utilisant un certificat full access avec le rôle ROLE_GET_SECURITY_PROFILE
    Then le serveur retourne les profile de sécurité par code ou nom

 Scenario: tous les profile de sécurité avec pagination
    When un utilisateur avec le rôle ROLE_GET_SECURITY_PROFILE récupère tous les profile de sécurité avec pagination en utilisant un certificat full access avec le rôle ROLE_GET_SECURITY_PROFILE
    Then le serveur retourne les profile de sécurité paginés

 Scenario: l'historique d'un profile de sécurité
    When un utilisateur avec le rôle ROLE_GET_SECURITY_PROFILE récupère l'historique d'un profile de sécurité à partir de son son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_SECURITY_PROFILE
    Then le serveur retourne l'historique du profile de sécurité





