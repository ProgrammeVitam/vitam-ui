(function () {
  const currentEl = document.getElementById('current');
  const newEl = document.getElementById('new');
  const confirmEl = document.getElementById('confirm');
  const submitBtn = document.getElementById('btn-submit');
  const stepChange = document.getElementById('step-change');
  const stepDone = document.getElementById('step-done');
  const errorEl = document.getElementById('error');
  const currentUserEl = document.getElementById('current-user');

  function showError(msg) {
    errorEl.textContent = msg;
    errorEl.hidden = false;
  }
  function clearError() {
    errorEl.hidden = true;
    errorEl.textContent = '';
  }

  submitBtn.addEventListener('click', async () => {
    clearError();
    const current = currentEl.value;
    const next = newEl.value;
    const confirm = confirmEl.value;
    if (!current || !next || !confirm) {
      showError('Tous les champs sont obligatoires.');
      return;
    }
    if (next !== confirm) {
      showError('Le nouveau mot de passe et sa confirmation ne correspondent pas.');
      return;
    }
    if (next === current) {
      showError('Le nouveau mot de passe doit être différent de l’ancien.');
      return;
    }

    submitBtn.disabled = true;
    try {
      const res = await fetch('/api/password/change', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentPassword: current, newPassword: next }),
      });
      if (res.status === 204) {
        stepChange.hidden = true;
        stepDone.hidden = false;
        return;
      }
      if (res.status === 401 || res.status === 403) {
        showError('Mot de passe actuel incorrect ou session expirée.');
        return;
      }
      if (res.status === 400) {
        showError('Le nouveau mot de passe ne respecte pas la politique de sécurité.');
        return;
      }
      if (res.status === 409) {
        showError('Ce mot de passe a déjà été utilisé — choisissez-en un différent.');
        return;
      }
      const body = await res.text();
      showError('Erreur ' + res.status + ' : ' + (body || 'inconnue'));
    } catch (e) {
      showError('Erreur réseau : ' + e.message);
    } finally {
      submitBtn.disabled = false;
    }
  });
})();
