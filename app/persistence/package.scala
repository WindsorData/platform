import com.mongodb.DBObject
import model._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import util.persistence._
import com.mongodb.casbah.commons.MongoDBList
import com.mongodb.casbah.commons.MongoDBListBuilder
import util.persistence._
import util._

package object persistence {
  type DBO = DBObject
  registerBigDecimalConverter()

  implicit def company2RichCompany[A <% DBObject](company: A)(implicit collection: MongoCollection) =
    new {
      def save() {
        collection.insert(company)
      }
      
      def update = collection.update(
          MongoDBObject("ticker.value" -> company.get("ticker").asInstanceOf[DBO].get("value"), 
        		  		"disclosureFiscalYear.value" -> company.get("disclosureFiscalYear").asInstanceOf[DBO].get("value")), 
        		  		company, true)
    }

  def findAllCompanies(db: MongoDB = MongoClient()("windsor")): List[CompanyFiscalYear] =
    db("companies").toList.map(x => dbObject2Company(x.asInstanceOf[DBO]))

 def findCompanyBy(name: String, year: Int, db: MongoDB = MongoClient()("windsor")): Option[CompanyFiscalYear] = {
    db("companies").
      findOne(MongoDBObject("ticker.value" -> name, "disclosureFiscalYear.value" -> year)).
      map {dbObject2Company(_)}
  }
  
  //TODO: check if there's a way to do this better
  def findAllCompaniesNames(db: MongoDB = MongoClient()("windsor")): Seq[String] =
    db("companies").toSet[DBO].map(x => x.get("ticker").asInstanceOf[DBO].get("value").toString()).toSeq
    
  def findAllCompaniesFiscalYears(db: MongoDB = MongoClient()("windsor")): Seq[Int] =
    db("companies").toSet[DBO].map(x => x.get("disclosureFiscalYear").asInstanceOf[DBO].get("value").asInstanceOf[Int]).toSeq

  /**Conversions for creating mappings to mongo*/
  implicit def string2MongoArrow(key: Symbol) = new {

    def ~>[A <% DBO](value: A): (String, DBO) = (toString(key), value: DBO)

    def ~>[A <% DBO](value: Traversable[A]): (String, Traversable[DBO]) =
      (toString(key), value.map { x => (x: DBO) })

    def toString(s: Symbol) = s.toString.drop(1)
  }

  implicit def input2DbObject[A](input: Input[A]): DBO =
    MongoDBObject(
      "value" -> input.value,
      "calc" -> input.calc,
      "comment" -> input.comment,
      "note" -> input.note,
      "link" -> input.link)
      .filter {
        case (k, v) => v != null
      }

  implicit def executive2DbObject(executive: Executive): DBO = {
    MongoDBObject(
      'name ~> executive.name,
      'title ~> executive.title,
      'shortTitle ~> executive.shortTitle,
      'functionalMatches ~> executive.functionalMatches,
      'founder ~> executive.founder,
      'carriedInterest ~> executive.carriedInterest,
      'equityCompanyValue ~> executive.equityCompanyValue,
      'cashCompensation ~> executive.cashCompensations)
  }
  implicit def company2DbObject(company: CompanyFiscalYear): DBO =
    MongoDBObject(
      'ticker ~> company.ticker,
      'name ~> company.name,
      'disclosureFiscalYear ~> company.disclosureFiscalYear,
      'executives ~> company.executives)

  implicit def carried2DbObject(interest: CarriedInterest): DBO =
    MongoDBObject(
      'ownedShares ~> interest.ownedShares,
      'perfVest ~> interest.perfVest,
      'tineVest ~> interest.tineVest,
      'unvestedOptions ~> interest.unvestedOptions,
      'vestedOptions ~> interest.vestedOptions)

  implicit def cashCompensation2DbObject(interest: AnualCashCompensation): DBO =
    MongoDBObject(
      'baseSalary ~> interest.baseSalary,
      'actualBonus ~> interest.actualBonus,
      'targetBonus ~> interest.targetBonus,
      'thresholdBonus ~> interest.thresholdBonus,
      'maxBonus ~> interest.maxBonus,
      'new8KData ~> interest.new8KData)

  implicit def new8KData2DbObject(interest: New8KData): DBO =
    MongoDBObject(
      'baseSalary ~> interest.baseSalary,
      'targetBonus ~> interest.targetBonus)

  implicit def equity2DbObject(value: EquityCompanyValue): DBO =
    MongoDBObject(
      'bsPercentage ~> value.bsPercentage,
      'exPrice ~> value.exPrice,
      'options ~> value.options,
      'optionsValue ~> value.optionsValue,
      'price ~> value.price,
      'shares ~> value.shares,
      'timeVestRsValue ~> value.timeVestRsValue,
      'perfRSValue ~> value.perfRSValue,
      'shares2 ~> value.shares2,
      'price2 ~> value.price2,
      'perfCash ~> value.perfCash)

  def makeInput[A](it: DBO) = Input(
    Option(it.get("value").asInstanceOf[A]),
    Option(it.get("calc").asInstanceOf[String]),
    Option(it.get("comment").asInstanceOf[String]),
    Option(it.get("note").asInstanceOf[String]),
    Option(it.get("link").asInstanceOf[String]))

  def fetch[A](key: String)(implicit executive: DBO): Input[A] = {

    executive.get(key) match {
      case null => None
      case it: DBO => makeInput(it)
    }
  }

  def fetchDBList[A](key: String)(implicit executive: DBO): Traversable[Input[A]] =
    executive.get(key) match {
      case Nil => List(None, None, None)
      case its: BasicDBList => its.map[Input[A], Traversable[Input[A]]](x => makeInput(x.asInstanceOf[DBO]))
    }

  implicit def dbObject2CarriedInteres(interest: DBO): CarriedInterest = {
    implicit val dbo = interest
    CarriedInterest(
      ownedShares = fetch("ownedShares"),
      perfVest = fetch("perfVest"),
      tineVest = fetch("tineVest"),
      unvestedOptions = fetch("unvestedOptions"),
      vestedOptions = fetch("vestedOptions"))
  }

  implicit def dbObject2Executive(implicit executive: DBO): Executive =
    Executive(
      name = fetch("name"),
      title = fetch("title"),
      shortTitle = fetch("shortTitle"),
      functionalMatches = fetchDBList("functionalMatches"),
      founder = fetch("founder"),
      carriedInterest = executive.get("carriedInterest").asInstanceOf[DBO],
      equityCompanyValue = executive.get("equityCompanyValue").asInstanceOf[DBO],
      cashCompensations = executive.get("cashCompensation").asInstanceOf[DBO])

  implicit def dbObject2Company(implicit company: DBO): CompanyFiscalYear =
    CompanyFiscalYear(
      ticker = fetch("ticker"),
      name = fetch("name"),
      disclosureFiscalYear = fetch("disclosureFiscalYear"),
      executives = company.get("executives").asInstanceOf[BasicDBList].map(x => dbObject2Executive(x.asInstanceOf[DBO])))

  implicit def dbObject2Equity(value: DBO): EquityCompanyValue = {
    implicit val dbo = value
    EquityCompanyValue(
      optionsValue = fetch("optionsValue"),
      options = fetch("options"),
      exPrice = fetch("exPrice"),
      bsPercentage = fetch("bsPercentage"),
      perfCash = fetch("perfCash"),
      price = fetch("price"),
      shares = fetch("shares"),
      perfRSValue = fetch("perfRSValue"),
      shares2 = fetch("shares2"),
      price2 = fetch("price2"),
      timeVestRsValue = fetch("timeVestRsValue"))
  }

  implicit def dbObject2New8KData(new8kdata: DBO): New8KData = {
    implicit val dbo = new8kdata
    New8KData(fetch("baseSalary"), fetch("targetBonus"))
  }

  implicit def dbObject2cashCompensation(cash: DBO): AnualCashCompensation = {
    implicit val dbo = cash
    AnualCashCompensation(
      baseSalary = fetch("baseSalary"),
      actualBonus = fetch("actualBonus"),
      targetBonus = fetch("targetBonus"),
      thresholdBonus = fetch("thresholdBonus"),
      maxBonus = fetch("maxBonus"),
      new8KData = dbObject2New8KData(cash.get("new8KData").asInstanceOf[DBO]))
  }
}
