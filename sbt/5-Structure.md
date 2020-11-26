# Permissions

We will live code a new microservice now called `Permissons`.

The requirements are the following:

1. There are users, groups and permissions:
1.1. Each user has a unique login and a password.
1.2. Each group has a unique name.
1.3. Each permission has a unique name.

2. Microservice should provide REST interface to:
2.1. Create a user, a group or permission.
2.2. Assign a user to a group.
2.3. Assign a permission to a group.

3. It should use the following technical stack:
3.1. Http4s to provide HTTP interface: https://http4s.org/
3.2. Quill library to access a database: https://github.com/getquill/quill
3.3. H2 database engine for storage: https://www.h2database.com/html/main.html

We will code it wrongly first and the refactor to be a bit better.
The homework will be related this task, but you do not have to worry if you
are not fully following live coding. I will commit the code after the lecture
so you can do a homework on it.

# Refactoring

What is the problem with what we coded? It is packaged by the layers, whcih is
a bad practice: http://www.javapractices.com/topic/TopicAction.do?Id=205

Let's talk about it a bit, and then refactor.
