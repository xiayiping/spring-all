## For how low/high cardinality is added to context
Check ```Simpleobservation```, search for ```modifiedContext``` \
You can see that the context which will be sent to baggage is combined by "convention" + "ObservationFilter.map".
## Convention and Context
The ```DefaultServerRequestObservationConvention``` has defined the "default" key/values. It's created in side ```WebMvcObservationAutoConfiguration```. 


tracing id is put to log4j thread local by calling functions in org.apache.logging.log4j.spi.ThreadContextMap