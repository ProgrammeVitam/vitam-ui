(function () {
  const currentEl = document.getElementById('current');
  const newEl = document.getElementById('new');
  const confirmEl = document.getElementById('confirm');
  const submitBtn = document.getElementById('btn-submit');
  const stepChange = document.getElementById('step-change');
  const stepDone = document.getElementById('step-done');
  const errorEl = document.getElementById('error');
  const currentUserEl = document.getElementById('current-user');
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

  // Reads the XSRF-TOKEN cookie set by Spring Security so the SPA can echo it on POST /change.
  // Undefined when the cookie hasn't been set yet — the initial GET /api/password/policy below
  // triggers Spring to set it before the user has a chance to submit.
  function csrfToken() {
    const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : null;
  }

  // Load policy so users see the rules before typing (rendered as bullet points).
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
    .catch(() => {
      // Silent — the server still enforces the policy; SPA hint is nice-to-have.
    });

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
    if (policy && policy.minLength && next.length < policy.minLength) {
      showError('Le nouveau mot de passe doit faire au moins ' + policy.minLength + ' caractères.');
      return;
    }

    submitBtn.disabled = true;
    try {
      const headers = { 'Content-Type': 'application/json' };
      const token = csrfToken();
      if (token) headers['X-XSRF-TOKEN'] = token;
      const res = await fetch('/api/password/change', {
        method: 'POST',
        credentials: 'include',
        headers: headers,
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
