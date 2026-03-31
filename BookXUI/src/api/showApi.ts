import client from './client';

/** POST /admin/shows/{externalShowId}/sync — Sync show from Admin Service */
export const syncShow = async (externalShowId: string): Promise<string> => {
  const { data } = await client.post<string>(
    `/admin/shows/${encodeURIComponent(externalShowId)}/sync`,
  );
  return data;
};
