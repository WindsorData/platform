package model.mapping

import libt.spreadsheet.Strip
import libt.{Model, Path}
import libt.spreadsheet.reader._
import model.PeerCompanies._
import libt.workflow._
import org.apache.poi.ss.usermodel.Workbook
import libt.spreadsheet.reader.WorkbookMapping
import libt.spreadsheet.Offset
import libt.spreadsheet.reader.Area
import libt.error.generic.Validated

package object peers extends Mappeable {

  val peersMapping = Seq[Strip](
    Path('companyName),
    Path('ticker),
    Path('src_doc),
    Path('filing_date),
    Path('group),
    Path('fiscalYear),
    Path('comments),
    Path('link),
    Path('groupDesc),
    Path('changes),
    Path('subGroup),
    Path('peerCoName),
    Path('peerTicker),
    Path('value))

  val pk = Seq(Path('peerTicker), Path('ticker), Path('fiscalYear))

  def Workflow: FrontPhase[Seq[Model]] =
    MappingPhase(Mapping) >> {
      (_, xs) => Validated(xs.head.filter { model =>
        pk.forall(model(_).rawValue[Any].nonEmpty)
      })
    }

  def Mapping = WorkbookMapping(Seq(Area(TPeers, Offset(1, 1), None, new ColumnOrientedLayout(RawValueReader), peersMapping)))
}
