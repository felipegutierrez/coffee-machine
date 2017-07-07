# coffee-machine

This is a project to practice Scala with Akka. It is based on activator-akka-scala-seed.

The package [sequential](src/main/scala/com/sequential/) has the class [CoffeeMachineSeq.scala](src/main/scala/com/sequential/CoffeeMachineSeq.scala). This class represents a sequential steps to make a Cappuccino.

The package [parallel](src/main/scala/com/parallel/) has parallel implementation of the same CoffeeMachine to make cappuccino. The class [CoffeeMachinePar.scala](src/main/scala/com/parallel/CoffeeMachinePar.scala) is the very first implementation of a parallel CoffeeMachine. This implementation only use the Future package and the Await to block the process until the cappuccino is ready.

The class [CoffeeMachinePromise.scala](src/main/scala/com/parallel/CoffeeMachinePromise.scala) shows the same CoffeeMachine but now with Promise package. Promise is a companion type that allows you to complete a Future by puting a value on it. This can be done exactly once. Once a Promise has been completed, it’s not possible to change it any more.

The class [CoffeeMachinePromiseHigherOrder.scala](src/main/scala/com/parallel/CoffeeMachinePromiseHigherOrder.scala) is an evolution of the last CoffeeMachine but now with Higher-Order functions. With Higher-Order functions we can make use of the DRY principle (Don't Repeat Yourself).

The class [CoffeeMachineActor.scala](src/main/scala/com/parallel/CoffeeMachineActor.scala) is an implementation of the CoffeeMachine using Actors from Akka package. Each job of the CoffeeMachine is using one different actor to not block the main task (make a cappuccino).


The class [CoffeeMachineActorSupervisor.scala](src/main/scala/com/parallel/supervisors/CoffeeMachineActorSupervisor.scala] and all the classes on the package [supervisors](src/main/scala/com/parallel/supervisors/) represent all actors of the CoffeeMachine with Akka. There are two main actors [CappuccinoActor](src/main/scala/com/parallel/supervisors/CappuccinoActor.scala) and [TeaActor](src/main/scala/com/parallel/supervisors/TeaActor.scala). These actors supervisors. All the other actors are Children Jobs that can fail and continue due an Exception.

The class [CoffeeMachineActorScheduler.scala](src/main/scala/com/parallel/scheduler/CoffeeMachineActorScheduler.scala) and all the classess on the package [scheduler](src/main/scala/com/parallel/scheduler/) represent all actors of the CoffeeMachine with Akka using Supervisor strategy and a scheduler. The [scheduler](http://doc.akka.io/docs/akka/current/scala/scheduler.html) fill the [WaterStorageActor.scala](src/main/scala/com/parallel/scheduler/WaterStorageActor.scala) each 3 seconds. When we ask for Tea or Cappuccino it is necessary 4 buckets of Water (unit to represent quantity). If there is no enough Water the `WaterLackException` is throwed and the [OneForOneStrategy](http://doc.akka.io/docs/akka/current/scala/fault-tolerance.html) makes a Fault Tolerance strategy to wait 1 second and ask for Water again.


The class [CoffeeMachineActorBreaks.scala](src/main/scala/com/parallel/breaks/CoffeeMachineActorBreaks.scala) and all the classes on the package [breaks](src/main/scala/com/parallel/breaks/) represent all actor of the CoffeeMachine with Akka using Supervisor with Restart and Resume strategy. The actors now have an ActoreRef on the constructor.


The class [RatingsCounter.scala](src/main/scala/com/spark/counter/RatingsCounter.scala) represents the first example of Spark + Scala program of this CoffeeMachine. I just created this class and imported spark dependencies to the project.

The class [SeveralSorts.scala](src/main/scala/com/spark/sort/SeveralSorts.scala) has several ways to sort a list of integers.


The class [CoffeeMachineActorFSM.scala](src/main/scala/com/parallel/fsm/CoffeeMachineActorFSM.scala) and all the classes on the package [fsm](src/main/scala/com/parallel/fsm/) represent all actors of the CoffeeMachine working with a [Finite State Machene](http://doc.akka.io/docs/akka/snapshot/scala/fsm.html). The Coffee Machine has 4 states: On, Off, Running and Idle. The user has to execute Events to get his coffee and the events change the states of the Coffee Machine.


The class [CoffeeMachinePersistentActor.scala](src/main/scala/com/parallel/persistent/CoffeeMachinePersistentActor.scala) is a Coffee Machine with a cash box to save coins in a [Persistent Actor](http://doc.akka.io/docs/akka/snapshot/scala/persistence.html) implemented using the class [CashBoxPersistentActor.scala](src/main/scala/com/parallel/persistent/CashBoxPersistentActor.scala). If you charge the Coffee Machine with coins and terminate (Ctrl+c) and restart the Coffee Machine, your coins will be sabed there. Because we are using persistent Actors.



