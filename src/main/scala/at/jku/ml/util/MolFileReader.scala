package at.jku.ml.util

import java.io.ByteArrayInputStream

import org.openscience.cdk.AtomContainer
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.io.MDLV2000Reader

import scala.util.Try

object MolFileReader {
  lazy val reader: MDLV2000Reader = new MDLV2000Reader

  def parseMolFile(molFile: String): Try[IAtomContainer] = Try {
    reader.setReader(new ByteArrayInputStream(molFile.getBytes))
    val molecule: IAtomContainer = reader.read(new AtomContainer)
    reader.close()
    molecule
  }
}
