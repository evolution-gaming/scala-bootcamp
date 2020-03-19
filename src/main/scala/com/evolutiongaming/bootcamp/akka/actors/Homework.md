## Homework

Binary trees are tree based data structures where every node has at most two children (left and right). 
In this task, every node stores an integer element.

From this we can build a binary search tree by requiring for every node that
- values of elements in the left subtree are strictly smaller than the node's element
- values of elements in the right subtree are strictly bigger than the node's element

In addition, there should be no duplicates, hence we obtain a binary tree set.

Your task is to implement an actor-based binary tree set where each node is represented by one actor.

You can find the message-based API for the actor-based binary tree to be implemented in the BinaryTreeSet.scala.

The operations, represented by actor messages, that the implementation should support are the following:
- Insert
- Remove
- Contains

All three of the operations expect 
- an ActorRef representing the requester of the operation
- a numerical identifier of the operation 
- the element itself. 

Insert and Remove operations
- should result in an OperationFinished message sent to the requester ActorRef reference including the id of the operation. 
- should return an OperationFinished message even if the element was already present in the tree or was not found, respectively. 

Contains should result in a ContainsResult message containing the result of the lookup and the identifier of the Contains query.

Instead of implementing the usual binary tree removal, 
in your solution you should use a flag that is stored in every tree node (removed) indicating whether the element in the node has been removed or not. 
This will result in a very simple implementation that is concurrent and correct with minimal effort. 

Additional task*
Implement `Garbage Collection`: clean up all the removed elements, while additional operations might arrive from the external world.
