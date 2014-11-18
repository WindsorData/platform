import libt.spreadsheet.Strip

/**
 * Generic mapping components that are used across 
 * the many tabs of the several Top5 reports.
 * 
 * Here only the interfaces of those components are described, following the 
 * Cake Pattern for dependency injection. 
 */
package object mapping {

  trait DocSrcMappingComponent {
    val docSrcMapping : Seq[Strip]
  }

  trait DilutionMappingComponent {
    val dilutionMapping : Seq[Strip]
    val bsInputsMapping : Seq[Strip]
    val usageAndSVTDataMapping: Seq[Strip]
  }

  trait Top5MappingComponent {
    val grantTypesMapping: Seq[Strip]
    def executiveMapping: Seq[Strip]
  }

  trait GuidelinesMappingComponent {
    val stBonusPlanMapping: Seq[Strip]
    val guidelinesMapping: Seq[Strip]
  }
}
