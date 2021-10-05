package com.evolution.domain

final case class Config(
    url: String,
    allowedCasinos: Boolean = true,
    showAllUsers: Boolean = true,
    onlyGroups: Boolean = true,
)
