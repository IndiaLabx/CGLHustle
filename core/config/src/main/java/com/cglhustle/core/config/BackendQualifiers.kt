package com.cglhustle.core.config

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PrimaryBackend

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QuestionBackend

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PrimaryBackendHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QuestionBackendHttpClient
