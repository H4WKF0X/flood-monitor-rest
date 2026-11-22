package com.example.floodmonitor.config;

import com.ctc.wstx.api.WstxOutputProperties;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

import java.time.Clock;
import java.util.Random;

@Configuration
public class AppConfig {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public Random random() {
        return new Random();
    }

    @Bean
    public MappingJackson2XmlHttpMessageConverter xmlConverter(Jackson2ObjectMapperBuilder builder) {
        XmlMapper xmlMapper = builder.createXmlMapper(true).build();
        xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        xmlMapper.getFactory().getXMLOutputFactory()
                .setProperty(WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL, true);
        return new MappingJackson2XmlHttpMessageConverter(xmlMapper);
    }
}
