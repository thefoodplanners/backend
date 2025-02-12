package co.uk.foodgen

import cats.effect.IOApp

object Main extends IOApp.Simple:

  def run = Server.server
