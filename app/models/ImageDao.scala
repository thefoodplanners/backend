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

  /**
   * Copies all the images in the given path from the raptor server and
   * store it locally.
   * Uses sshj, a java library for creating SSH clients that can fetch
   * files from remote servers.
   * @param paths The file paths used to find where the images are stored on raptor.
   * @return Nothing.
   */
  def fetchImages(paths: Seq[String]): Future[Unit] = {
    Future {
      val hostname = "raptor.kent.ac.uk"
      val username = config.get[String]("USERNAME")
      val password = config.get[String]("RAPTOR_PASSWORD")
      val destinationFile = IMAGES_PATH

      // Create SSH client
      val ssh = new SSHClient()
      ssh.addHostKeyVerifier(new PromiscuousVerifier())
      ssh.connect(hostname)
      ssh.authPassword(username, password)
      val sftp: SFTPClient = ssh.newSFTPClient()
      // Copy image file to local directory
      paths.foreach(sourceFile => sftp.get(sourceFile, destinationFile))
      sftp.close()
      ssh.disconnect()
    }
  }

  /**
   * Encodes all the images in the local directory to a Base64 string.
   * @return A map, with the key being the name of the image file and
   *         the value being the Base64 encoded string.
   */
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