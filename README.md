## Introduction
This project is a genetic algorithm (GA) optimization software which used GA to optimize functions such as Schwefel, Rosenbrock, and Rastrigin functions. It is my assignment for the subject Concurrent Programming durng my undergraduate semester 6.

## Methodology
### Selection
For the selection process, I used the tournament selection of size 3, where I randomly select 3 individuals from the population and select the fittest individuals among them as the parent.
### Reproduction
For the reproduction process, there is 70% chance that crossover will occur, while 30% chance for the children to be the replica of their parents.
### Crossover
For the crossover process, I used a the one-point crossover method. At the same time, I improved the one-point crossover by applying another concept into it which is the so called semi crossover, where I will swap the position of the part of the genes obtained from the parents for the second child. Basically, through the semi crossover, it greatly improved the optimization rate of the functions. Below shows the diagram to help for this explanation.
![Semi crossover](https://github.com/ginsan95/Genetic-Algorithm-Optimization/blob/master/demo/doc/crossover.png?raw=true)
### Mutation
For the mutation process, I used 1% as the rate of mutation, while the mutation value changed dynamically depending on whether the population is stuck at a local minimum. If the population is stuck at a local minimum, special algorithm will be applied, which increases the mutation value and rate to get the population out from the local minimum.

## Screenshot
![Screenshot](https://github.com/ginsan95/Genetic-Algorithm-Optimization/blob/master/demo/screenshots/screenshot.png?raw=true)
