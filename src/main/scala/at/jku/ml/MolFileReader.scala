package at.jku.ml

import java.io.ByteArrayInputStream

import org.openscience.cdk.AtomContainer
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.io.MDLV2000Reader

object MolFileReader {
  lazy val reader: MDLV2000Reader = new MDLV2000Reader

  def parseMolFile(molfile: String): IAtomContainer = {
    reader.setReader(new ByteArrayInputStream(molfile.getBytes))
    val molecule: IAtomContainer = reader.read(new AtomContainer)
    molecule
  }
}
