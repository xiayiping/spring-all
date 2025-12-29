```shell

liquibase --changelog-file=changelog.xml \
          --url=jdbc:postgresql://localhost:5432/mydb \
          --username=user \
          --password=pass \
          updateCount 3

\tools\liquibase\liquibase-4.32.0\liquibase.bat --defaults-file=liquibase.uat.properties update
\tools\liquibase\liquibase-4.32.0\liquibase.bat --defaults-file=liquibase.uat.properties rollback-count --count=1
\tools\liquibase\liquibase-4.32.0\liquibase.bat --defaults-file=liquibase.uat.properties clearCheckSums
```
