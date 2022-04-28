using System;
using System.Threading.Tasks;

namespace AbacusODataClient
{
    class Program
    {
        static void Main(string[] args)
        {
            var client = new AbaClient();
            var subjects = client.GetAddressesAsync().Result;
            foreach (var subject in subjects)
            {
                Console.WriteLine($"{subject.FirstName} {subject.LastName}");
            }
        }
    }
}
