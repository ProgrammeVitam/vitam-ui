/*
 * Minimal login SPA for the SAS POC (Phase 1).
 * Two-step flow: email → mini HRD (resolve customerId + providerType) → password → OAuth2 redirect.
 * The vanilla JS is a Phase 1 shortcut; the target Phase 2 replaces this with an Angular project
 * inside ui/ui-frontend/projects/auth-ui/.
 */

const state = {
  email: null,
  customerId: null,
  providerType: null,
  providerId: null,
};

const $ = (id) => document.getElementById(id);

function showError(msg) {
  const err = $('error');
  err.textContent = msg;
  err.hidden = false;
}

function clearError() {
  $('error').hidden = true;
}

async function postJson(url, body) {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(body),
  });
  return response;
}

async function resolve() {
  clearError();
  const email = $('email').value.trim();
  if (!email) return showError('Adresse email requise.');

  const response = await postJson('/api/login/resolve', { email });
  if (response.status === 404) {
    return showError("Aucune organisation associée à cet email.");
  }
  if (response.status === 409) {
    return showError('Plusieurs organisations trouvées : cas non supporté par le POC (Phase 2).');
  }
  if (!response.ok) {
    return showError('Erreur de résolution HRD (' + response.status + ').');
  }
  const data = await response.json();
  state.email = email;
  state.customerId = data.customerId;
  state.providerId = data.identityProviderId;
  state.providerType = data.providerType;

  if (state.providerType !== 'internal') {
    return showError("Fournisseur d'identité externe non supporté par le POC (Phase 2).");
  }

  $('step-email').hidden = true;
  $('step-password').hidden = false;
  $('email-readonly').value = state.email;
  $('password').focus();
}

async function authenticate() {
  clearError();
  const password = $('password').value;
  if (!password) return showError('Mot de passe requis.');

  const response = await postJson('/api/login/authenticate', {
    email: state.email,
    password,
    customerId: state.customerId,
  });
  if (response.status === 401) {
    return showError('Identifiants invalides.');
  }
  if (!response.ok) {
    return showError('Erreur d\'authentification (' + response.status + ').');
  }
  const data = await response.json();
  if (data.redirectUrl) {
    window.location.assign(data.redirectUrl);
  } else {
    showError('Aucune URL de redirection retournée par le serveur.');
  }
}

function back() {
  clearError();
  $('step-password').hidden = true;
  $('step-email').hidden = false;
  $('password').value = '';
}

document.addEventListener('DOMContentLoaded', () => {
  $('btn-resolve').addEventListener('click', resolve);
  $('btn-authenticate').addEventListener('click', authenticate);
  $('btn-back').addEventListener('click', back);
  $('email').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') resolve();
  });
  $('password').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') authenticate();
  });
});
