using System;
using System.Threading.Tasks;

namespace AbacusODataClient
{
    class Program
    {
        static void Main(string[] args)
        {
            var client = new SimpleAbaClient();
            var subject = client.GetSubject(5).Result;
            Console.WriteLine($"{subject["LastName"]}");
            //foreach (var subject in subjects)
            //{
            //    Console.WriteLine($"{subject["LastName"]}");
            //}
        }
    }
}
