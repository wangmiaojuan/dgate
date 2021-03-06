package top.dteam.dgate.config

import io.vertx.core.http.HttpMethod

class ApiGatewayRepository {

    @Delegate
    List<ApiGatewayConfig> respository

    ApiGatewayRepository() {
        this.respository = new ArrayList<>()
    }

    static ApiGatewayRepository load(String file = System.getProperty('conf')) {
        String config
        if (file) {
            config = new File(file).text
        } else {
            config = Thread.currentThread().getContextClassLoader().getResource('conf').text
        }

        build(config)
    }

    static ApiGatewayRepository build(String config) {
        ApiGatewayRepository apiGatewayRepository = new ApiGatewayRepository()

        ConfigSlurper slurper = new ConfigSlurper()
        ConfigObject configObject = slurper.parse(config)
        configObject.keySet().each { apiGateway ->
            apiGatewayRepository.respository << buildApiGateway(apiGateway, configObject[apiGateway])
        }

        apiGatewayRepository
    }

    private static ApiGatewayConfig buildApiGateway(def key, def body) {
        String name = key
        int port = body.port
        String login = body.login ?: null
        CorsConfig cors = buildCors(body.cors)
        List<UrlConfig> urlConfigs = new ArrayList<>()
        body.urls.keySet().each { url ->
            urlConfigs << buildUrl(url, body.urls[url])
        }

        new ApiGatewayConfig(name: name, port: port, urlConfigs: urlConfigs, login: login, cors: cors)
    }

    private static CorsConfig buildCors(Map cors) {
        if (cors) {
            new CorsConfig(cors)
        } else {
            null
        }
    }

    private static UrlConfig buildUrl(def key, def body) {
        String url = key
        Object required = body.required ?: null
        List<HttpMethod> methods = body.methods ?: []
        Map expected = body.expected
        List<UpstreamURL> upstreamURLs = new ArrayList<>()
        body.upstreamURLs.each { upstreamURL ->
            upstreamURLs << new UpstreamURL(host: upstreamURL.host, port: upstreamURL.port, url: upstreamURL.url,
                    before: upstreamURL.before, after: upstreamURL.after)
        }

        new UrlConfig(url: url, required: required, methods: methods, expected: expected, upstreamURLs: upstreamURLs)
    }

}
