package net.danlew.displate.model

enum class LimitedType(val type: String) {
  event_exclusive("Event Exclusive"),
  standard("LE"),
  ultra("ULE"),
  lumino("Lumino");

  companion object {
    fun parse(type: String): LimitedType {
      return LimitedType.values().first { it.type == type }
    }
  }
}