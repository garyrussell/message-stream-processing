using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;

namespace SimpleConsumer.Controllers
{
    [Route("api/[controller]")]
    public class ValuesController : Controller
    {
        
        // POST api/values
        [HttpPost]
        public void Post([FromBody]string value)
        {
            Console.WriteLine("Got the message: " + value);
        }

    }
}
