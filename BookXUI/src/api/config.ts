/**
 * ═══════════════════════════════════════════════════════════
 *  BookXShow API Configuration
 * ═══════════════════════════════════════════════════════════
 *
 *  Edit the .env file (or set environment variables) to
 *  configure the backend connection:
 *
 *    VITE_API_BASE_URL  — Backend host  (e.g. http://localhost:8080)
 *    VITE_API_PREFIX    — API prefix    (e.g. /bookxshow/v1)
 *
 *  These values are baked in at build time by Vite.
 *  For runtime overrides, edit the `config` object below.
 */

export interface ApiConfig {
  /** Full base URL of the backend (no trailing slash) */
  baseUrl: string;
  /** API path prefix (e.g. /bookxshow/v1)  */
  prefix: string;
  /** Computed: baseUrl + prefix */
  readonly apiUrl: string;
  /** Request timeout in ms */
  timeout: number;
}

const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const prefix = import.meta.env.VITE_API_PREFIX || '/bookxshow/v1';

const config: ApiConfig = {
  baseUrl,
  prefix,
  get apiUrl() {
    return `${this.baseUrl}${this.prefix}`;
  },
  timeout: 30_000,
};

export default config;
