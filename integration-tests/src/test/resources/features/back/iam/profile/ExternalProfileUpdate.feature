@Api
@ApiIam
@ApiIamProfiles
@ApiIamProfilesUpdate
Feature: API Profile : mise à jour d'un profile

  Scenario: Opération non implémentée
    Given un profil a été créé
    When un utilisateur met à jour un profil
    Then le serveur refuse la mise à jour car l'opération n'est pas implémentée
