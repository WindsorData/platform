package output.writers.generic

import libt.Path
import libt.spreadsheet.Gap
import libt.spreadsheet.Strip
import model.mapping.FullTop5MappingComponent

trait FullTop5WithTtdcMappingComponent extends FullTop5MappingComponent {
  override def executiveMapping =
    Seq[Strip](Gap, Path('calculated, 'ttdc), Path('calculated, 'ttdcPayRank)) ++ super.executiveMapping
}
