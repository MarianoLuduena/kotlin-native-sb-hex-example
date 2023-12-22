package com.example.payments.archunit

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.Architectures

@AnalyzeClasses(packages = ["com.example.payments"], importOptions = [ImportOption.DoNotIncludeTests::class])
internal object LayeredArchitectureTest {

    private const val DOMAIN = "Domain"
    private const val ADAPTERS = "Adapters"
    private const val APPLICATION = "Application"
    private const val CONFIG = "Config"
    private const val BASE_PKG = "com.example.payments."

    @ArchTest
    val layer_dependencies_are_respected: ArchRule =
        Architectures.layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer(CONFIG).definedBy(BASE_PKG + "config..")
            .layer(DOMAIN).definedBy(BASE_PKG + "domain..")
            .layer(ADAPTERS).definedBy(BASE_PKG + "adapter..")
            .layer(APPLICATION).definedBy(BASE_PKG + "application..")
            .whereLayer(APPLICATION).mayOnlyBeAccessedByLayers(ADAPTERS, CONFIG)
            .whereLayer(ADAPTERS).mayOnlyBeAccessedByLayers(CONFIG)
            .whereLayer(DOMAIN).mayOnlyBeAccessedByLayers(APPLICATION, ADAPTERS, CONFIG)

}
