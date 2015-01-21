package output.writers

import model.mapping.FullOutputDilutionMappingComponent
import model.mapping.FullOutputGuidelinesMappingComponent

import mapping.DocSrcMappingComponent

import output.writers.generic.FullTop5WithTtdcMappingComponent
import output.writers.generic.Top5Writer

object FullTop5Writer extends Top5Writer
  with DocSrcMappingComponent
  with FullOutputDilutionMappingComponent
  with FullTop5WithTtdcMappingComponent
  with FullOutputGuidelinesMappingComponent {

  override val fileName = "EmptyFullOutputTemplate.xls"

  val docSrcMapping = StandardTop5Writer.docSrcMapping
}
