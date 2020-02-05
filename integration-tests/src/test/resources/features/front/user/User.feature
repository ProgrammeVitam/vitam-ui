@Ui
@AppUsers
Feature: Se connecter sur l'application utilisateur
  Vérifier l'application utilisateur

  Scenario: Se connecter avec succès
  	Given l'utilisateur admin
    When l'utilisateur se connecte dans l'application utilisateur
    Then la liste des utilisateurs est affichée
