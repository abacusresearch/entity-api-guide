using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Ch.Abacus.Odata;
using Ch.Abacus.Adre;

namespace AbacusODataClient
{
    public class AbaClient
    {
        private readonly static string SERVICE_URL = "http://localhost:8081/api/entity/v1/mandants/7777";
        private readonly static string AUTH_URL = "https://entity-api1-1.demo.abacus.ch/oauth/oauth2/v1/token";
        private readonly TokenCache _tokenCache;
        private readonly Container _context;

        public AbaClient()
        {
            //_tokenCache = new TokenCache(new Authenticator(AUTH_URL));
            _context = new Container(new Uri(SERVICE_URL));
            //_context.BuildingRequest += (sender, eventArgs) =>
            //{
            //    eventArgs.Headers.Add("Authorization", CreateAuthHeaderValue());
            //};
        }

        private string CreateAuthHeaderValue()
        {
            return "Bearer " + _tokenCache.GetAccessToken();
        }

        public async Task<IEnumerable<Subject>> GetAddressesAsync()
        {
            return await _context.Subjects.ExecuteAsync();
        }
    }
}
