# coffee-machine

This is a project to practive Scala with Akka. It is based on activator-akka-scala-seed.

The package [sequential](src/main/scala/com/sequential/) has the class [CoffeeMachineSeq.scala](src/main/scala/com/sequential/CoffeeMachineSeq.scala). This class represents a sequential steps to make a Cappuccino.

The package [parallel](src/main/scala/com/parallel/) has parallel implementation of the same CoffeeMachine to make cappuccino. The class [CoffeeMachinePar.scala](src/main/scala/com/parallel/CoffeeMachinePar.scala) is the very first implementation of a parallel CoffeeMachine. This implementation only use the Future package and the Await to block the process until the cappuccino is ready.

The class [CoffeeMachinePromise.scala](src/main/scala/com/parallel/CoffeeMachinePromise.scala) shows the same CoffeeMachine but now with Promise package. Promise is a companion type that allows you to complete a Future by puting a value on it. This can be done exactly once. Once a Promise has been completed, it’s not possible to change it any more.

The class [CoffeeMachinePromiseHigherOrder.scala](src/main/scala/com/parallel/CoffeeMachinePromiseHigherOrder.scala) is an evolution of the last CoffeeMachine but now with Higher-Order functions. With Higher-Order functions we can make use of the DRY principle (Don't Repeat Yourself).

The class [CoffeeMachineActor.scala](src/main/scala/com/parallel/CoffeeMachineActor.scala) is an implementation of the CoffeeMachine using Actors from Akka package. Each job of the CoffeeMachine is using one different actor to not block the mean task (make a cappuccino).


