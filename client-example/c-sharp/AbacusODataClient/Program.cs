using System;
using System.Threading.Tasks;

namespace AbacusODataClient
{
    class Program
    {
        static void Main(string[] args)
        {
            var client = new SimpleAbaClient();
            var subjects = client.GetSubjectsAsync();
            foreach (var subject in subjects.Result)
            {
                Console.WriteLine($"{subject["Name"]}");
            }
        }
    }
}
