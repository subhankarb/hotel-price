
## The Problem Set:

You are provided with hotels database in CSV (Comma Separated Values) format.
We need you to implement HTTP service, according to the API requirements described below. Please use Scala for the solution.

  - RateLimit: API calls need to be rate limited (request per 10 seconds) based on API Key provided in each http call.
     - On exceeding the limit, api key must be suspended for next 5 minutes.
     - Api key can have different rate limit set, in this case from configuration, 
        and if not present there must be a global rate limit applied.
  - Search hotels by CityId
  - Provide optional sorting of the result by Price (both ASC and DESC order).
Note: Please don’t use any external library or key-value store for RateLimit. 
You need to create a InMemory Implementation for RateLimit.



## RateLimit:
Each ApiKey class keep track of: what time it last used ans allowance
Allowance is the limit of request it would handel in unit sec. 
The logic behind the rate limiter is
``` scala
allowance = allowance + timePassed * (rate / 1.0)
if (allowance > rate) allowance = rate
if (allowance < 1)flase
else 
    allowance = allowance - 1.0 
    true
```
So after n number of request where n is number of request per sec
allowance get burnt and it block the request.



## In memory Fake DB:
The application take the fixer csv and put it in im-memory db implementation ```FakeDB```
We can insert, delete, filter sort. For see the implementation plz see ```app.models.FakeDB```
There are 2 tables in the db. One for api key and one for hotel data.

## End Points:

### /apikey?rate=4
    
This end point returns back api key with rate we mention
rate: [request rate /second][optional] if not present global level 10req/sec will be used
   
```
{
     "message": {
       "key": "079bfad0-2f0e-4a6d-82be-ff616c4451ab",
       "rate": 4
     }
}
```

### /hotels?city=amsterdam&sort=ASC&page=1&size=2

This end point return back the list of hotels.
Query Params:
city [required]
sort [ASC/DESC] [optional] if not mentioned data returned back as ASC
page [optional] if not mentioned page number 1 returned
size [optional] if not mentioned page size is default 10

Header:
ApiKey: [Mandatory] use the key got back from /apikey
 

```
{
  "result": [
    {
      "city": "amsterdam",
      "hotelId": 13,
      "room": "superior",
      "price": 1000
    },
    {
      "city": "amsterdam",
      "hotelId": 2,
      "room": "superior",
      "price": 2000
    }
  ],
  "total_data": 6,
  "page": 1,
  "size": 2
}
```
