package net.danlew.displate.model

enum class LimitedType(val size: String, val cost: Int) {
  standard("M", 139),
  ultra("L", 289)
}