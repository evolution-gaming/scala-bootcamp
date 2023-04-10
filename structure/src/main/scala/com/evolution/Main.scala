package com.evolution

import akka.actor.ActorSystem
import com.evolution.clients.AppHttpClient
import com.evolution.controllers._
import com.evolution.domain.Config
import com.evolution.infrastructure.http.{HttpClient, HttpServer}
import com.evolution.infrastructure.json.JsonParser
import com.evolution.repository.{CasinoRepository, UserAnalysisRepository, UserReportsRepository, UserRepository}
import com.evolution.services._

object Main extends App {

  val system     = ActorSystem("System")
  val httpClient = new HttpClient
  val config     = Config("http://localhost")

  val userRepository         = new UserRepository
  val userAnalysisRepository = new UserAnalysisRepository(userRepository)
  val userReportsRepository  = new UserReportsRepository(userRepository)
  val userService            = new UserService(
    userRepository,
    userAnalysisRepository,
    userReportsRepository,
    config,
  )
  val permissionService      = new PermissionService(userRepository)
  val groupService           = new GroupService(userRepository)
  val casinoRepository       = new CasinoRepository
  val casinoService          = new CasinoService(casinoRepository, config)

  val assignmentController = new AssignmentController(userService)
  val userController       = new UserController(userService)
  val casinoController     = new CasinoController(casinoService)
  val permissionController = new PermissionController(permissionService)
  val groupController      = new GroupController(groupService)

  val httpServer = new HttpServer(
    assignmentController,
    userController,
    casinoController,
    groupController,
    permissionController,
  )

  httpServer.start()

  val appHttpClient    = new AppHttpClient(
    casinoService,
    httpClient,
    config,
    new JsonParser,
  )
  val scheduledUpdates =
    new ScheduledUpdates(appHttpClient, casinoService, system)

  scheduledUpdates.start()
}
