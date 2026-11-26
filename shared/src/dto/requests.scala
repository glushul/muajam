package muajam.shared.dto

import zio.json.*

final case class WordRequest(
    id: Option[Int],
    arabic: String,
    russian: String,
    plural: Option[String]
) derives JsonDecoder,
      JsonEncoder

final case class ModuleRequest(
    id: Option[Int],
    name: String,
    words: List[WordRequest]
) derives JsonDecoder,
      JsonEncoder
