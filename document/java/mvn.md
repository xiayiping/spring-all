## WHY NEED FLATTEN

${revision} is not work in parent.

- ${revision} not allowed in top module parent section
- ${revision} in <version> section will be literally copied to maven local repo, which will not recognized by other projects.

So has to use flatten plugin if you want to dynamically sync versions through all modules