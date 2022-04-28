using Simple.OData.Client;
using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;

namespace AbacusODataClient
{
    public class SimpleAbaClient
    {
        private readonly static string BASE_URL = "https://entity-api1-1.demo.abacus.ch/";
        private readonly static string API_PATH = "api/entity/v1/mandants/7777";
        private readonly static string AUTH_URL = "https://entity-api1-1.demo.abacus.ch/oauth/oauth2/v1/token";
        private static readonly HttpClient _httpClient = new HttpClient();
        private readonly TokenCache _tokenCache;
        private readonly ODataClient _client;

        public SimpleAbaClient()
        {
            _tokenCache = new TokenCache(new Authenticator(AUTH_URL));
            _httpClient.BaseAddress = new Uri(BASE_URL);
            _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", _tokenCache.GetAccessToken());
            _client = new ODataClient(new ODataClientSettings(_httpClient, new Uri(API_PATH, UriKind.Relative)));
        }

        public async Task<IEnumerable<dynamic>> GetSubjectsAsync()
        {
            return await _client.FindEntriesAsync("Subjects");
        }

        public async Task<IDictionary<string, object>> GetSubject(long key)
        {
            return await _client.GetEntryAsync("Subjects", key);
        }
    }
}
