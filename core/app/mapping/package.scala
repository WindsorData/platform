import libt.spreadsheet.Strip

/**
 * Created with IntelliJ IDEA.
 * User: matias
 * Date: 8/9/13
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
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
