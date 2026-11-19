package muajam.shared

import zio.json.*

final case class WordResponse(
    id: Int,
    arabic: String,
    withoutHarakat: String,
    russian: String,
    plural: Option[String],
    pluralWithoutHarakat: Option[String],
    moduleId: Int,
    createdAt: String
) derives JsonCodec

final case class ModuleResponse(
    id: Int,
    name: String,
    createdAt: String,
    wordsCount: Int,
    words: List[WordResponse]
) derives JsonCodec
