package windsor.output.writers.generic

import libt._
import libt.spreadsheet._
import mapping.DocSrcMappingComponent

trait StandardDocSrcMapping extends DocSrcMappingComponent {

  val docSrcMapping =
    Seq[Strip](
      Path('ticker),
      Path('name),
      Path('disclosureFiscalYear),
      Path('tenK),
      Path('def14a)) ++ Multi(Path('otherDocs), 7, Path('type), Path('date))


}