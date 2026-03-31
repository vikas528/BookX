/**
 * Mock API bootstrap — imported only when Vite runs in "mock" mode.
 *
 * Usage:  npm run dev:mock
 *
 * This file grabs the shared Axios client and patches it with
 * the mock adapter.  When running `npm run dev` (real backend),
 * this file is never imported and completely tree-shaken from the bundle.
 */
import client from '../api/client';
import { enableMockApi } from './mockAdapter';

enableMockApi(client);
