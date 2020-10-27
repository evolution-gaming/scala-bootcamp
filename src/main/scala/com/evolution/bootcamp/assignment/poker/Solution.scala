package com.evolution.bootcamp.assignment.poker

final case class Solution(
  groups: List[Set[Hand]],
) {
  override def toString: String = groups.map { set =>
    set.map(_.toString).toList.sorted.mkString("=")
  }.mkString(" ")
}