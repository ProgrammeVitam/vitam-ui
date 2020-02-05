@Ui
@Login
Feature: Se connecter sur le site vitamui
  Vérifier la fonctionnalité du Login

	@Traces
  #Scenario: Se connecter avec succès
  #	Given l'utilisateur demo
  #	And l'utilisateur affiche la page d'accueil
  #  And l'utilisateur non authentifié est redirigé vers la page de login
  #  When l'utilisateur saisit son email
  #  And l'utilisateur saisit son mot de passe
  #  Then la page qui liste les applications est affichée
    # And Une trace d'authentification de mon utilisateur est présente dans vitam

  Scenario: Se loguer avec email inconnu
    Given l'utilisateur unknow
    And l'utilisateur affiche la page d'accueil
    And l'utilisateur non authentifié est redirigé vers la page de login
    When l'utilisateur saisit son email
    Then l'email est inconnu

  Scenario: Se loguer avec mot de passe incorrect
  	Given l'utilisateur demo
    And l'utilisateur affiche la page d'accueil
    And l'utilisateur non authentifié est redirigé vers la page de login
    When l'utilisateur saisit son email
    And l'utilisateur saisit un mot de passe incorrect
    Then un message d'erreur est affiché
