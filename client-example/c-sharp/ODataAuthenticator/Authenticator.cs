using System;
using System.Text;
using System.Net.Http;
using System.Threading.Tasks;
using System.Net.Http.Headers;
using System.Text.Json;

namespace AbacusODataClient
{
    public class Authenticator
    {
        private readonly static string CLIENT_ID = Environment.GetEnvironmentVariable("CLIENT_ID");
        private readonly static string CLIENT_SECRET = Environment.GetEnvironmentVariable("CLIENT_SECRET");
        private static readonly HttpClient client = new HttpClient();

        private string AuthUrl { get; }
        private static string BasicAuthHeader
        {
            get
            {
                var stringToEncode = new StringBuilder()
                    .Append(CLIENT_ID)
                    .Append(":")
                    .Append(CLIENT_SECRET)
                    .ToString();
                var plainTextBytes = Encoding.UTF8.GetBytes(stringToEncode);
                var encodedCredentials = Convert.ToBase64String(plainTextBytes);
                var basicAuthHeader = new StringBuilder().Append("Basic ").Append(encodedCredentials).ToString();
                return basicAuthHeader;
            }
        }

        public Authenticator(string serviceUrl)
        {
            AuthUrl = serviceUrl;
        }

        public async Task<Token> GetToken()
        {
            StringContent queryString = new StringContent("grant_type=client_credentials", Encoding.UTF8, "application/x-www-form-urlencoded");
            client.DefaultRequestHeaders.Authorization = AuthenticationHeaderValue.Parse(BasicAuthHeader);
            var response = await client.PostAsync(AuthUrl, queryString);
            var responseString = await response.Content.ReadAsStringAsync();
            return ParseResponse(responseString);
        }

        private static Token ParseResponse(string responseString)
        {
            JsonDocument document = JsonDocument.Parse(responseString);
            var root = document.RootElement;
            var accessToken = root.GetProperty("access_token");
            var expiresIn = root.GetProperty("expires_in");
            var expiresInAsLong = GetExpiresInAsLong(expiresIn.GetInt64());
            return new Token(accessToken.GetString(), expiresInAsLong);
        }

        private static long GetExpiresInAsLong(long expiresIn)
        {
            return DateTime.Now.Ticks + expiresIn;
        }
    }
}
