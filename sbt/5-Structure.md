# Permissions

We will live code a new microservice now called `Permissons`.

The requirements are the following:

1. There are users, groups and permissions:
   - Each user has a unique login and a password.
   - Each group has a unique name.
   - Each permission has a unique name.

2. Microservice should provide REST interface to:
   - Create a user, a group or permission.
   - Assign a user to a group.
   - Assign a permission to a group.
   - Get permissions by user login.

3. It should use the following technical stack:
   - Http4s to provide HTTP interface: https://http4s.org
   - Quill library to access a database: https://https://getquill.io
   - H2 database engine for storage: https://h2database.com

We will code it wrongly first and the refactor to be a bit better.
The homework will be related this task, but you do not have to worry if you
are not fully following live coding. I will commit the code after the lecture
so you can do a homework on it.

# Refactoring

What is the problem with what we coded? It is packaged by the layers, whcih is
a bad practice: http://www.javapractices.com/topic/TopicAction.do?Id=205

Let's talk about it a bit, and then refactor.
