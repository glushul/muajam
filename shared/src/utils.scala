package muajam.shared

def stripHarakat(text: String): String =
  text.toLowerCase.filterNot { ch =>
    val code = ch.toInt
    (code >= 0x064b && code <= 0x065f) ||
    code == 0x0670 ||
    (code >= 0x06d6 && code <= 0x06ed)
  }
