package pl.cinemaparadiso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Konfiguracja kodowania znaków dla aplikacji
 * Zapewnia poprawne wyświetlanie polskich znaków (ą, ć, ę, ł, ń, ó, ś, ź, ż)
 */
@Configuration
public class EncodingConfig implements WebMvcConfigurer {
    
    /**
     * Konfiguruje kodowanie dla wszystkich odpowiedzi HTTP
     * Wymusza użycie UTF-8 dla wszystkich odpowiedzi
     */
    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }
    
    /**
     * Konfiguruje konwertery wiadomości HTTP
     * Zapewnia, że wszystkie odpowiedzi tekstowe używają UTF-8
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converters.add(stringConverter);
    }
}


