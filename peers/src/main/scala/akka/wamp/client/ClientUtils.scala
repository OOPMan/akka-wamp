package akka.wamp.client

import akka.actor._
import akka.wamp._

trait ClientUtils extends Scope.SessionScope { 
  this: Actor =>

  val config = context.system.settings.config.getConfig("akka.wamp.client")

  /**
    * The boolean switch (default is false) to validate
    * against strict URIs rather than loose URIs
    */
  val strictUris = config.getBoolean("validate-strict-uris")

  /** WAMP types Validator */
  implicit val validator = new Validator(strictUris)
  
  /** Execution context */
  implicit val ec = context.system.dispatcher
  
}