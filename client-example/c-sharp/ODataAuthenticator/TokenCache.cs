using System;

namespace AbacusODataClient
{
    public sealed class TokenCache
    {
        private readonly Authenticator _authenticator;
        private Token _token;

        public TokenCache(Authenticator authenticator)
        {
            _authenticator = authenticator;
            _token = new Token("", 0L);
        }

        private bool IsTokenExpired()
        {
            return _token.ExpiresIn < DateTime.Now.Ticks;
        }

        public string GetAccessToken()
        {
            if(!IsTokenExpired())
            {
                return _token.AccessToken;
            }
            else
            {
                try
                {
                    _token = _authenticator.GetToken().Result;
                    return _token.AccessToken;
                }
                catch (Exception e)
                {
                    Console.WriteLine(e.StackTrace);
                    throw new Exception("Failed to fetch access_token");
                }
            }
        }
    }
}