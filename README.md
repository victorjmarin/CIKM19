# CIKM19

This repository contains an implementation of the paper _Clustering Recurrent and Semantically-Cohesive Program Statements in Introductory Programming Assignments_.

We provide the dataset used in our evaluation as serialized program dependence graphs in the 'assignments.zip' file. Unfortunately, we cannot provide the original source code due to CodeChef's policies. However, each serialized submission is given its original identifier as a name and we provide links to each of the assignments.

## How to run the experiments

- Unzip 'assignments.zip', which contains the serialized program dependence graphs for submissions in the different assignments.
- Run ``edu.rit.goal.Exp1`` to run a single iteration of the core statement mining with fixed µ and ε.
- Run ``edu.rit.goal.Exp2`` to run an iterative process for core statement mining with µ set to a percentage of the submissions and fixed ε.

Below is a table with links to the different assignments as well as the identifier used in the experiments for each of them. 

| Assignment      | id |
| ----------- | ----------- |
| [JOHNY](https://www.codechef.com/problems/JOHNY)      | 0 
| [CARVANS](https://www.codechef.com/problems/CARVANS)   | 1
| [BUYING2](https://www.codechef.com/problems/BUYING2)   | 2 
| [MUFFINS3](https://www.codechef.com/problems/MUFFINS)   | 3
| [CLEANUP](https://www.codechef.com/problems/CLEANUP)   | 4
| [CONFLIP](https://www.codechef.com/problems/CONFLIP)   | 5
| [LAPIN](https://www.codechef.com/problems/LAPIN)   | 6
| [PERMUT2](https://www.codechef.com/problems/PERMUT2)   | 7
| [STONES](https://www.codechef.com/problems/STONES)   | 8
| [SUMTRIAN](https://www.codechef.com/problems/SUMTRIAN)   | 9