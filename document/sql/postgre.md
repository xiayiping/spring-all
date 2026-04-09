# sql

```sql
CREATE TABLE example_table (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name varchar(64) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

```

## special in postgre for timezone

The SQL standard differentiates timestamp without time zone and timestamp with time zone literals by the presence of a “+” or “-” symbol and time zone offset after the time. Hence, according to the standard,

TIMESTAMP '2004-10-19 10:23:54'
is a timestamp without time zone, while

TIMESTAMP '2004-10-19 10:23:54+02'
is a timestamp with time zone. 

PostgreSQL never examines the content of a literal string before determining its type, and therefore will treat both of the above as timestamp without time zone. To ensure that a literal is treated as timestamp with time zone, give it the correct explicit type:

TIMESTAMP WITH TIME ZONE '2004-10-19 10:23:54+02'

In a value that has been determined to be timestamp without time zone, PostgreSQL will silently ignore any time zone indication. That is, the resulting value is derived from the date/time fields in the input string, and is not adjusted for time zone.

For timestamp with time zone values, an input string that includes an explicit time zone will be converted to UTC (Universal Coordinated Time) using the appropriate offset for that time zone. If no time zone is stated in the input string, then it is assumed to be in the time zone indicated by the system's TimeZone parameter, and is converted to UTC using the offset for the timezone zone. In either case, the value is stored internally as UTC, and the originally stated or assumed time zone is not retained.