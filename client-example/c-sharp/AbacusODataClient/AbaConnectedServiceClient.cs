using System;
using System.Collections.Generic;
using System.Threading.Tasks;
namespace AbacusODataClient
{
    /**
     * This class uses the microsoft connected odata service. To use it as is it requires following the tutorial mentioned in the README
     */

    public class AbaConnectedServiceClient
    {
        private readonly static string SERVICE_URL = "https://entity-api1-1.demo.abacus.ch/entity/v1/mandants/7777";
        private readonly static string AUTH_URL = "https://entity-api1-1.demo.abacus.ch/oauth/oauth2/v1/token";
        private readonly TokenCache _tokenCache;
       // private readonly Container _context;

        /**
         * Here we add a request interceptor to handle our Client Credential Code Grant while accessing the Abacus API
         */

        public AbaConnectedServiceClient()
        {
            //_tokenCache = new TokenCache(new Authenticator(AUTH_URL));
            //_context = new Container(new Uri(SERVICE_URL));
            //_context.BuildingRequest += (sender, eventArgs) =>
            //{
            //    eventArgs.Headers.Add("Authorization", CreateAuthHeaderValue());
            //};
        }

        private string CreateAuthHeaderValue()
        {
            return "Bearer " + _tokenCache.GetAccessToken();
        }

        /**
         * This is an example function to query the Subjects async using the connected service
         */

        //public async Task<IEnumerable<Subject>> GetSubjectsAsync()
        //{
        //    return await _context.Subjects.ExecuteAsync();
        //}
    }
}
