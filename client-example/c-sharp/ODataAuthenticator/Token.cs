namespace AbacusODataClient
{
    public class Token
    {
        public string AccessToken { get; }
        public long ExpiresIn { get; }

        public Token(string accessToken, long expiresIn)
        {
            AccessToken = accessToken;
            ExpiresIn = expiresIn;
        }
    }
}