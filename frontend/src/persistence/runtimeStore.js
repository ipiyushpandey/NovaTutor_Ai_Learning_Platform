/*
 * NovaTutor V70.22 — central in-memory runtime workspace.
 *
 * The app is a SPA, so module components are intentionally allowed to unmount
 * during navigation. Their learning state must therefore live outside the
 * component tree. This runtime cache is the canonical in-session source;
 * workspaceStore remains the durable browser persistence layer.
 */

const root = () => {
  if (typeof window === 'undefined') return null;
  if (!window.__novaRuntimeStore) {
    window.__novaRuntimeStore = {
      schemaVersion: 1,
      users: new Map(),
      listeners: new Map()
    };
  }
  return window.__novaRuntimeStore;
};

const userKey = userId => String(userId || 'guest');
const durableKey = (userId, module) => `nova_runtime_v2_${userKey(userId)}_${moduleKey(module)}`;
const hasStorage = () => typeof window !== 'undefined' && !!window.localStorage;
const safeParse = raw => { try { return raw ? JSON.parse(raw) : null; } catch { return null; } };
const moduleKey = module => String(module || 'unknown');
const runtimeListeners = () => {
  const r = root();
  if (!r) return null;
  if (!r.listeners) r.listeners = new Map();
  return r.listeners;
};
const listenerKey = (userId, module) => `${userKey(userId)}::${moduleKey(module)}`;
const emitRuntime = (userId, module) => {
  const listeners = runtimeListeners();
  listeners?.get(listenerKey(userId, module))?.forEach(fn => { try { fn(); } catch {} });
};

const clone = value => {
  if (value == null) return value;
  try { return JSON.parse(JSON.stringify(value)); } catch { return value; }
};

const ensureUser = userId => {
  const r = root();
  if (!r) return null;
  const key = userKey(userId);
  if (!r.users.has(key)) r.users.set(key, new Map());
  return r.users.get(key);
};

export const hasRuntimeState = (userId, module) => {
  const user = ensureUser(userId);
  return !!user?.has(moduleKey(module));
};

export const readRuntimeState = (userId, module, fallback = null) => {
  const key = moduleKey(module);
  const user = ensureUser(userId);
  if (user?.has(key)) return clone(user.get(key));
  if (hasStorage()) {
    const persisted = safeParse(window.localStorage.getItem(durableKey(userId, module)));
    if (persisted && persisted.state && typeof persisted.state === 'object') {
      const state = clone(persisted.state);
      if (user) user.set(key, clone(state));
      return state;
    }
  }
  return fallback;
};

export const subscribeRuntimeState = (userId, module, listener) => {
  const listeners = runtimeListeners();
  if (!listeners || typeof listener !== 'function') return () => {};
  const key = listenerKey(userId, module);
  if (!listeners.has(key)) listeners.set(key, new Set());
  listeners.get(key).add(listener);
  return () => {
    const set = listeners.get(key);
    if (!set) return;
    set.delete(listener);
    if (!set.size) listeners.delete(key);
  };
};

export const getRuntimeModuleSnapshot = (userId, module, fallback = null) => {
  const user = ensureUser(userId);
  const key = moduleKey(module);
  if (user?.has(key)) return user.get(key);
  if (hasStorage()) {
    const persisted = safeParse(window.localStorage.getItem(durableKey(userId, module)));
    if (persisted && persisted.state && typeof persisted.state === 'object') {
      const state = clone(persisted.state);
      if (user) user.set(key, state);
      return state;
    }
  }
  return fallback;
};

export const updateRuntimeState = (userId, module, updater, fallback = null) => {
  const current = getRuntimeModuleSnapshot(userId, module, fallback);
  const next = typeof updater === 'function' ? updater(current) : updater;
  writeRuntimeState(userId, module, next);
  return getRuntimeModuleSnapshot(userId, module, next);
};

export const writeRuntimeState = (userId, module, state) => {
  const user = ensureUser(userId);
  if (!user) return false;
  const snapshot = clone(state);
  const key = moduleKey(module);
  user.set(key, snapshot);
  // V70.23: the runtime cache has a durable mirror. This is intentionally
  // separate from the legacy workspace envelope so a module can recover even
  // if its component tree was destroyed before an autosave effect ran.
  if (hasStorage()) {
    try {
      window.localStorage.setItem(durableKey(userId, module), JSON.stringify({schemaVersion:2,updatedAt:new Date().toISOString(),state:snapshot}));
    } catch {}
  }
  emitRuntime(userId, module);
  return true;
};

export const removeRuntimeState = (userId, module) => {
  const user = ensureUser(userId);
  const removed = !!user?.delete(moduleKey(module));
  if (hasStorage()) { try { window.localStorage.removeItem(durableKey(userId, module)); } catch {} }
  return removed;
};

export const clearRuntimeUser = userId => {
  const r = root();
  if (!r) return;
  r.users.delete(userKey(userId));
  if (hasStorage()) {
    try {
      const prefix = `nova_runtime_v2_${userKey(userId)}_`;
      Object.keys(window.localStorage).filter(k=>k.startsWith(prefix)).forEach(k=>window.localStorage.removeItem(k));
    } catch {}
  }
};

export const clearRuntimeStore = () => {
  const r = root();
  if (!r) return;
  r.users.clear();
  r.listeners?.clear();
};

export const getRuntimeSnapshot = userId => {
  const user = ensureUser(userId);
  if (!user) return {};
  const result = {};
  user.forEach((value, key) => { result[key] = clone(value); });
  return result;
};
