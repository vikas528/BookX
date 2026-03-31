import axios from 'axios';
import config from './config';

/**
 * Pre-configured Axios instance that points at the BookXShow backend.
 *
 * All requests automatically include:
 *   • baseURL  →  e.g. http://localhost:8080/bookxshow/v1
 *   • timeout  →  30 s (configurable)
 *   • JSON content-type
 */
const client = axios.create({
  baseURL: config.apiUrl,
  timeout: config.timeout,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

// ── Response interceptor: unwrap errors into a friendlier shape ──
client.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.data?.message) {
      return Promise.reject(new Error(error.response.data.message));
    }
    return Promise.reject(error);
  },
);

export default client;
