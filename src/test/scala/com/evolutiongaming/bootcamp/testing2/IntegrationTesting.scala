package com.evolutiongaming.bootcamp.testing2

// This is the most expensive type of the tests, because of potential flakiness
// and the need of the heavyweight machines to execute them.
//
// Nowadays, with advent of Selenium DSL etc., it is quite easy to use integration
// testing DSLs for the applications, and, as consequence, some teams are crazy
// overdoing with these. We recommend to push as much as possible testing towards
// unit and compiler level tests.
//
// Integration tests could be run using embedded servers or using real virtual
// environments using docker containers etc.
//
object IntegrationTesting