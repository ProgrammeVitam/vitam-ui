@Api
@ApiIam
@ApiIamSubrogations
@ApiIamSubrogationsDecline
Feature: Opérations de refus des subrogations

  Scenario: Décliner une subrogation
    Given l'utilisateur admin@vitamui.com n'a pas de subrogations en cours
    And une subrogation a été créée pour le bon utilisateur
    When un utilisateur décline la subrogation
    Then le serveur accepte de décliner la subrogation

  Scenario: Décliner une subrogation d'un autre utilisateur
    Given l'utilisateur admin@vitamui.com n'a pas de subrogations en cours
    And une subrogation a été créée pour le bon utilisateur
    When un autre utilisateur décline la subrogation
    Then le serveur refuse de décliner la subrogation
