## Project for INFO 7255

```bash
# To run RabbitMQ, elasticsearch and kibana
docker compose up

# To stop running RabbitMQ, elasticsearch and kibana
docker compose down
```

Site for RabbitMQ dashboard  
[Local RabbitMQ dashboard](localhost:15672)

Site for Kibana  
[Local Kibana dashboard](http://localhost:5601)

Site for Kibana dev console  
[Local Kibana dev console](http://localhost:5601/app/dev_tools#/console)

### elasticsearch script

```bash
# Search All
GET plan/_search
{
  "query" : {
      "match_all" : {}
   }
}

# Search children of type plan
GET plan/_search
{
  "query" : {
    "has_parent" : {
      "parent_type" : "plan",
      "query" : {
         "match_all" : {}
      }
    }
  }
}

# Search children of type linkedPlanServices with objectId 27283xvx9asdff-504
GET plan/_search
{
  "query" : {
    "has_parent" : {
      "parent_type" : "linkedPlanServices",
      "query" : {
        "bool" : {
          "must" : [
            {
             "match" : {
               "objectId" : "27283xvx9asdff-504"
            }
          }
        ]
       }
      }
     }
    }
  }


# Search for any object which copay is greater or equal than 100
GET plan/_search
{
  "query" : {
    "range" : {
      "copay" :
      {
        "gte": 100
      }
    }
  }
}

# Search parent of type planserviceCostShares with objectId 1234512xvc1314sdfsd-506
GET plan/_search
{
  "query" : {
    "has_child" : {
      "type" : "planserviceCostShares",
      "query" : {
        "bool" : {
          "must" : [
            {
             "match" : {
               "objectId" : "1234512xvc1314sdfsd-506"
            }
          }
        ]
       }
      }
     }
    }
   }


# Search parent for type planCostShares which has copay equals to 23
GET plan/_search
{
  "query" : {
    "has_child" : {
      "type" : "planCostShares",
      "query" : {
        "bool" : {
          "must" : [
            {
             "match" : {
               "copay": 23
            }
          }
        ]
       }
      }
     }
    }
   }
```
