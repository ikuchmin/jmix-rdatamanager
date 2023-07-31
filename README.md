## Reactive DataManager for Jmix

Adds RDataManager with async methods. Actual for cases when app doing many requests to show a screen      

Basic concept of this implementation is running getting data in the same thread if transaction exist and use another thread if transaction doesn't exist
It is controlled by DataStore implementation.

If you are using Jmix "Additional data stores" you should be careful of transactions. If all 
stores are transactional according to basic concept all data executions will be in
the main thread. But if main data store isn't transactional it will dispatch execution
to another thread and annotation @Transaction on method losing an effect 

## TODO

- implement abstract class using for basic behavior

## Performance issues

- isLoaded has to high price (possible save fetchPlan into Entity)
- merge and tracking