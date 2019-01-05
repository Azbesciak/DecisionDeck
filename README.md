# MCDA algorithms for DecisionDeck project

This repository contains modules for [DIVIZ](https://www.diviz.org/) and [R MCDA](https://www.decision-deck.org/r/) platforms.
It's a part of Bachelor's thesis.
 
 Project contains implementation of 4 methods divided into modules:
 - [TOPSIS](https://en.wikipedia.org/wiki/TOPSIS):
   - normalization and weighting - [`topsis-normalization-weighting`](https://github.com/Azbesciak/DecisionDeck/tree/master/topsis-normalization-weighting)
   - determine ideal alternatives - [`topsis-ideal-alternatives`](https://github.com/Azbesciak/DecisionDeck/tree/master/topsis-ideal-alternatives)
   - determine ranking by distance to ideal positive alternative - [`topsis-distance-ranking`](https://github.com/Azbesciak/DecisionDeck/tree/master/topsis-distance-ranking)
 - [AHP](https://en.wikipedia.org/wiki/Analytic_hierarchy_process): [`ahp`](https://github.com/Azbesciak/DecisionDeck/tree/master/ahp)
 - [VIKOR](https://en.wikipedia.org/wiki/VIKOR_method)
   - S (*the maximum group utility*) and  R (*the minimum individual regret of the opponent*) vectors - [`vikor-SR`](https://github.com/Azbesciak/DecisionDeck/tree/master/vikor-SR)
   - compromise solution - [`vikor-compromises`](https://github.com/Azbesciak/DecisionDeck/tree/master/vikor-SR)
 - [DEMATEL](https://www.hindawi.com/journals/mpe/2018/3696457/)
   - compute influences of each alternative/factor on others - [`dematel-influence`](https://github.com/Azbesciak/DecisionDeck/tree/master/dematel-influence)
   - get relationships between alternatives/factors with optional *alpha* threshold - [`dematel-relationship`](https://github.com/Azbesciak/DecisionDeck/tree/master/dematel-relationship)
   
### How to use
Requirements:
- Java 8 
> JVM projects works also with 8+ due to included in `build.gradle` XML dependencies; however, due to not attached xml jars with XMCDA, those are not visible on R Java classpath - this only works with JDK 8 when standalone usage is required. Propably installing those jars (for example [`javax.xml.bind`](https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api/2.4.0-b180830.0359)) indivitually will solve the problem; not tested though.
- R - at least 3.5 (required only when using R modules)
- optionally Gradle 4.10+; however, it is recommended to use embedded wrapper.

Additionaly R modules requires installed XMCDA package - compatible jar is attached with project in `/libs` directory.
Also, `dematel-influence` requires installed [`purrr`](https://purrr.tidyverse.org/) package.

 JVM connected (TOPSIS and AHP) projects are build under gradle. All of them can be employed with installed `gradle` or embedded `gradlew` usage - in the second case after type CMD command it will download wrapper (about 100 MB - just once).
 To create executable jar you need to type:
 ```cmd
 ./gradlew :project:shadowJar
```
where `project` is the name of required project, for example `topsis-normalization-weighting`.
Both XMCDA v2 and v3 are supported, so you need to specify version with flag `--v2` or `--v3`.


In case of R packages, you just need to use it in R environment with respect to XMCDA version. All clients are named in following way:
```
<project>CLI_XMCDA<xmcda version>.R
```
> `project` is the same as module name, however - camelCase'd 
In all cases, input and output dir paths are required, with preceding flag `-i` and `-o`, respectively.

If there is no dir at output path, it will be created.

If you are looking for input and output parameters, take a look into `description-wsDDv[2/3].xml` or `tests` in each module.

All `gradle.build` files in modules requires main project `build.gradle` - those only extends this file by module specific requirements, so if you are going to build those whithout the rest of the project dependency (not talking about single jar build - just about detatching this module from the whole project) you will need to change those files; otherwise dependencies will not be visible.
