package models

import com.google.inject.Inject
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import play.api.Configuration

import java.io.File
import java.nio.file.Files
import java.util.Base64
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImageDao @Inject()(config: Configuration) {
  def fetchImages(paths: Seq[String]): Future[Unit] = {
    Future {
      val hostname = "raptor.kent.ac.uk"
      val username = config.get[String]("USERNAME")
      val password = config.get[String]("RAPTOR_PASSWORD")
      val destinationFile = IMAGES_PATH

      val ssh = new SSHClient()
      ssh.addHostKeyVerifier(new PromiscuousVerifier())
      ssh.connect(hostname)
      ssh.authPassword(username, password)
      val sftp: SFTPClient = ssh.newSFTPClient()
      paths.foreach(sourceFile => sftp.get(sourceFile, destinationFile))
      sftp.close()
      ssh.disconnect()
    }
  }

  def imagesToString: Map[String, String] = {
    new File(IMAGES_PATH)
      .listFiles
      .map(_.toPath)
      .map { path =>
        val imageBytes: Array[Byte] = Files.readAllBytes(path)
        path.toFile.getName -> Base64.getEncoder.encodeToString(imageBytes)
      }
      .toMap
  }

}