/*
 * NovaTutor V70.19 — App-level persistence coordinator.
 *
 * V70.19 is the foundation phase of the 3-version persistence rebuild.
 * Individual modules still own their state, but every mounted persistent
 * module registers with this coordinator. Navigation and page-exit flushes
 * therefore use one central app-level transaction instead of module-specific
 * calls scattered through the UI.
 */
import {writeWorkspace} from './workspaceStore.js';

export const APP_WORKSPACE_SCHEMA_VERSION = 2;
const APP_PREFIX = 'nova_app_workspace_v2';
const hasStorage = () => typeof window !== 'undefined' && !!window.localStorage;
const safeParse = (raw) => { try { return raw ? JSON.parse(raw) : null; } catch { return null; } };
const appKey = (userId) => `${APP_PREFIX}_${String(userId || 'guest')}`;

const readIndex = (userId) => {
  if (!hasStorage()) return {schemaVersion: APP_WORKSPACE_SCHEMA_VERSION, updatedAt:null, modules:{}};
  const parsed = safeParse(window.localStorage.getItem(appKey(userId)));
  if (!parsed || parsed.schemaVersion !== APP_WORKSPACE_SCHEMA_VERSION || !parsed.modules) {
    return {schemaVersion: APP_WORKSPACE_SCHEMA_VERSION, updatedAt:null, modules:{}};
  }
  return parsed;
};

export const touchModule = (userId, module, updatedAt = new Date().toISOString()) => {
  if (!hasStorage()) return;
  try {
    const index = readIndex(userId);
    index.modules[String(module)] = {updatedAt};
    index.updatedAt = updatedAt;
    window.localStorage.setItem(appKey(userId), JSON.stringify(index));
  } catch {}
};

export const getAppWorkspaceIndex = (userId) => readIndex(userId);

const registry = () => {
  if (typeof window === 'undefined') return null;
  if (!window.__novaAppWorkspace) {
    window.__novaAppWorkspace = {
      flushers: new Map(),
      flushing: false,
      generation: 0
    };
  }
  return window.__novaAppWorkspace;
};

export const registerWorkspaceFlusher = (key, flush) => {
  const r = registry();
  if (!r || !key || typeof flush !== 'function') return () => {};
  r.flushers.set(key, flush);
  return () => r.flushers.delete(key);
};

export const flushAllWorkspaces = (reason = 'navigation') => {
  const r = registry();
  if (!r || r.flushing) return r?.generation || 0;
  r.flushing = true;
  r.generation += 1;
  try {
    [...r.flushers.values()].forEach(flush => { try { flush(reason); } catch {} });
  } finally {
    r.flushing = false;
  }
  return r.generation;
};

export const beginNavigationPersistence = (reason = 'navigation') => {
  const generation = flushAllWorkspaces(reason);
  if (typeof window !== 'undefined') {
    try { window.dispatchEvent(new CustomEvent('nova:workspace-flushed', {detail:{reason, generation}})); } catch {}
  }
  return generation;
};

export const resetAppWorkspaceRegistry = () => {
  const r = registry();
  if (!r) return;
  r.flushers.clear();
  r.generation += 1;
};

// Keep a tiny, explicit API for future V70.20 migration. It deliberately does
// not copy module state into a second store yet; module state remains canonical
// in workspaceStore.js until every module is migrated to the app store.
export const writeAppModuleCheckpoint = (userId, module, state) => {
  const ok = writeWorkspace(userId, module, state);
  if (ok) touchModule(userId, module);
  return ok;
};
