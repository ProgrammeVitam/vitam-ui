(function () {
  const stepRequest = document.getElementById('step-request');
  const stepRequestDone = document.getElementById('step-request-done');
  const stepReset = document.getElementById('step-reset');
  const stepResetDone = document.getElementById('step-reset-done');
  const errorEl = document.getElementById('error');
  const opaqueMessageEl = document.getElementById('opaque-message');
  const policyHintsEl = document.getElementById('policy-hints');

  let policy = null;

  function showError(msg) {
    errorEl.textContent = msg;
    errorEl.hidden = false;
  }
  function clearError() {
    errorEl.hidden = true;
    errorEl.textContent = '';
  }

  function hideAll() {
    [stepRequest, stepRequestDone, stepReset, stepResetDone].forEach((el) => (el.hidden = true));
  }

  // Load the policy — used on the reset (new password) step.
  fetch('/api/password/policy', { credentials: 'include' })
    .then((res) => (res.ok ? res.json() : null))
    .then((p) => {
      if (!p) return;
      policy = p;
      const items = [];
      if (p.minLength) items.push('Longueur minimum : ' + p.minLength + ' caractères');
      (p.messages || []).forEach((m) => items.push(m));
      if (p.maxOldPassword) items.push('Ne pas réutiliser vos ' + p.maxOldPassword + ' derniers mots de passe');
      if (items.length === 0) return;
      policyHintsEl.innerHTML = items.map((i) => '<li>' + i + '</li>').join('');
      policyHintsEl.hidden = false;
    })
    .catch(() => {});

  const params = new URLSearchParams(window.location.search);
  const token = params.get('token');

  if (token) {
    hideAll();
    stepReset.hidden = false;
  }

  // --- Step 1: request a reset link by email ---
  const emailEl = document.getElementById('email');
  const btnRequest = document.getElementById('btn-request');
  btnRequest.addEventListener('click', async () => {
    clearError();
    const email = emailEl.value.trim();
    if (!email) {
      showError('Adresse email obligatoire.');
      return;
    }
    btnRequest.disabled = true;
    try {
      const res = await fetch('/api/password/reset/request', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email }),
      });
      if (!res.ok) {
        showError('Erreur ' + res.status + ' — réessayez plus tard.');
        return;
      }
      const body = await res.json();
      opaqueMessageEl.textContent = body.message;
      hideAll();
      stepRequestDone.hidden = false;
    } catch (e) {
      showError('Erreur réseau : ' + e.message);
    } finally {
      btnRequest.disabled = false;
    }
  });

  // --- Step 2: reset with token + new password ---
  const newEl = document.getElementById('new');
  const confirmEl = document.getElementById('confirm');
  const btnReset = document.getElementById('btn-reset');
  btnReset.addEventListener('click', async () => {
    clearError();
    const next = newEl.value;
    const confirm = confirmEl.value;
    if (!next || !confirm) {
      showError('Tous les champs sont obligatoires.');
      return;
    }
    if (next !== confirm) {
      showError('Le nouveau mot de passe et sa confirmation ne correspondent pas.');
      return;
    }
    if (policy && policy.minLength && next.length < policy.minLength) {
      showError('Le nouveau mot de passe doit faire au moins ' + policy.minLength + ' caractères.');
      return;
    }

    btnReset.disabled = true;
    try {
      const res = await fetch('/api/password/reset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token: token, newPassword: next }),
      });
      if (res.status === 204) {
        hideAll();
        stepResetDone.hidden = false;
        return;
      }
      if (res.status === 400 || res.status === 401) {
        showError('Ce lien de réinitialisation est invalide ou a expiré. Recommencez la procédure.');
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
      btnReset.disabled = false;
    }
  });
})();
