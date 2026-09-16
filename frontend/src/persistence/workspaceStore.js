/*
 * NovaTutor V70.12 — Persistent Workspace Engine
 *
 * Browser-only, dependency-free persistence layer used by learning modules.
 * It preserves the V70.11 storage keys for compatibility while wrapping
 * values in a versioned workspace envelope.
 */

export const WORKSPACE_SCHEMA_VERSION = 1;
export const WORKSPACE_PREFIX = 'nova_workspace_v1';

const hasStorage = () => typeof window !== 'undefined' && !!window.localStorage;

const safeJsonParse = (raw) => {
  if (!raw) return null;
  try { return JSON.parse(raw); } catch { return null; }
};

export const workspaceKey = (userId, module) =>
  `${WORKSPACE_PREFIX}_${String(userId || 'guest')}_${String(module || 'unknown')}`;

const legacyKey = (userId, module) =>
  `nova_module_state_${userId || 'guest'}_${module}`;

const normalizeEnvelope = (raw) => {
  if (!raw || typeof raw !== 'object') return null;

  // New V70.12 envelope.
  if (raw.schemaVersion === WORKSPACE_SCHEMA_VERSION && raw.state && typeof raw.state === 'object') {
    return {
      schemaVersion: WORKSPACE_SCHEMA_VERSION,
      updatedAt: raw.updatedAt || null,
      state: raw.state
    };
  }

  // V70.11 compatibility: migrate the old plain state without changing
  // the old key, so rollback remains safe.
  if (!raw.schemaVersion && typeof raw === 'object') {
    const { updatedAt, ...state } = raw;
    return {
      schemaVersion: WORKSPACE_SCHEMA_VERSION,
      updatedAt: updatedAt || null,
      state
    };
  }

  return null;
};

export const readWorkspace = (userId, module, fallback = null) => {
  if (!hasStorage()) return fallback;

  const primary = safeJsonParse(window.localStorage.getItem(workspaceKey(userId, module)));
  const normalizedPrimary = normalizeEnvelope(primary);
  if (normalizedPrimary) return normalizedPrimary;

  // One-time lazy migration from V70.11.
  const legacy = safeJsonParse(window.localStorage.getItem(legacyKey(userId, module)));
  const normalizedLegacy = normalizeEnvelope(legacy);
  return normalizedLegacy || fallback;
};

export const writeWorkspace = (userId, module, state) => {
  if (!hasStorage()) return false;

  const envelope = {
    schemaVersion: WORKSPACE_SCHEMA_VERSION,
    updatedAt: new Date().toISOString(),
    state: state && typeof state === 'object' ? state : {}
  };

  try {
    window.localStorage.setItem(workspaceKey(userId, module), JSON.stringify(envelope));
    // V70.19 app-level index: metadata only. The module envelope remains canonical.
    const indexKey = `nova_app_workspace_v2_${String(userId || 'guest')}`;
    const existing = safeJsonParse(window.localStorage.getItem(indexKey));
    const index = (existing && existing.schemaVersion === 2 && existing.modules)
      ? existing
      : {schemaVersion:2, updatedAt:null, modules:{}};
    index.modules[String(module)] = {updatedAt: envelope.updatedAt};
    index.updatedAt = envelope.updatedAt;
    window.localStorage.setItem(indexKey, JSON.stringify(index));
    return true;
  } catch {
    return false;
  }
};

export const migrateWorkspaceIfNeeded = (userId, module) => {
  if (!hasStorage()) return null;

  const primary = safeJsonParse(window.localStorage.getItem(workspaceKey(userId, module)));
  if (normalizeEnvelope(primary)) return normalizeEnvelope(primary);

  const legacy = safeJsonParse(window.localStorage.getItem(legacyKey(userId, module)));
  const migrated = normalizeEnvelope(legacy);
  if (migrated) {
    try {
      window.localStorage.setItem(workspaceKey(userId, module), JSON.stringify(migrated));
    } catch {}
  }
  return migrated;
};

export const removeWorkspace = (userId, module) => {
  if (!hasStorage()) return;
  try {
    window.localStorage.removeItem(workspaceKey(userId, module));
    window.localStorage.removeItem(legacyKey(userId, module));
  } catch {}
};

export const subscribeWorkspace = (userId, module, callback) => {
  if (!hasStorage() || typeof callback !== 'function') return () => {};

  const key = workspaceKey(userId, module);
  const onStorage = (event) => {
    if (event.key !== key || event.storageArea !== window.localStorage) return;
    const parsed = normalizeEnvelope(safeJsonParse(event.newValue));
    callback(parsed);
  };

  window.addEventListener('storage', onStorage);
  return () => window.removeEventListener('storage', onStorage);
};
