package you.shall.not.pass.configuration;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.representer.Representer;
import you.shall.not.pass.security.staticresource.StaticResourceAccessPolicy;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableJpaRepositories(basePackages = "you.shall.not.pass.repositories")
public class CommonConfig {

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Value("classpath:policies/**")
    private Resource[] configs;

    @Bean("yamlConfigure")
    public Yaml configure() {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        representer.getPropertyUtils().setBeanAccess(BeanAccess.FIELD);
        return new Yaml(new Constructor(StaticResourceAccessPolicy.class), representer);
    }

    @Bean("StaticResourceAccessPolicies")
    public List<StaticResourceAccessPolicy> getPolicies(Yaml yamlConfigure) {
        List<StaticResourceAccessPolicy> accessStaticResourceAccessPolicies = new ArrayList<>();
        for (Resource resource : configs) {
            try {
                accessStaticResourceAccessPolicies.add(yamlConfigure.load(resource.getInputStream()));
            } catch (Exception e) {
                log.warn("could not read config {}, reason {}", resource.getFilename(), e.getMessage());
            }
        }
        log.info("applicationList: {}", accessStaticResourceAccessPolicies);
        return accessStaticResourceAccessPolicies;
    }

}
