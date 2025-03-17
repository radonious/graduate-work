package edu.plag.config

import edu.plag.util.FileStorageProperties
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import org.springframework.web.servlet.resource.ResourceResolverChain
import java.util.Objects.nonNull

@Configuration
class WebConfig: WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        this.serveDirectory(registry, "/", "classpath:/static/")
        registry.addResourceHandler("/images/**")
            .addResourceLocations("file:" + FileStorageProperties.getUploadPath())
    }

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(MappingJackson2HttpMessageConverter())
    }

    /**
     * Определение редиректа для возможности обновления SPA (Single-Page Application)
     * Без него этого обновления любой страницы, кроме корня "/index.html" приведет к 404
     * из-за того что такого ресурса нет на сервере, а по сути он реактивно генерируется JS
     * @param registry регистратор
     * @param endpoint куда редирект
     * @param location расположение файлов
     */
    private fun serveDirectory(registry: ResourceHandlerRegistry, endpoint: String, location: String) {
        // Создаем паттерн для перенаправления
        val endpointPatterns =
            if (endpoint.endsWith("/")) arrayOf(endpoint.substring(0, endpoint.length - 1), endpoint, "$endpoint**")
            else arrayOf(endpoint, "$endpoint/", "$endpoint/**")

        // Применяем паттерн
        registry.addResourceHandler(*endpointPatterns)
            .addResourceLocations(if (location.endsWith("/")) location else "$location/").resourceChain(false)
            .addResolver(object : PathResourceResolver() {
                // Если запрашиваемый ресурс валидный - возвращаем его
                // В иных случаях перенаправляем на /index.html
                override fun resolveResource(
                    request: HttpServletRequest?,
                    requestPath: String,
                    locations: List<Resource>,
                    chain: ResourceResolverChain
                ): Resource? {
                    val resource = super.resolveResource(request, requestPath, locations, chain)
                    return if (nonNull(resource)) {
                        resource
                    } else super.resolveResource(request, "/index.html", locations, chain)
                }
            })
    }
}