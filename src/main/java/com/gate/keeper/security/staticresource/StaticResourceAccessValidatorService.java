package com.gate.keeper.security.staticresource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import com.gate.keeper.domain.AccessLevel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaticResourceAccessValidatorService implements StaticResourceAccessValidator {

    private final List<StaticResourceAccessPolicy> accessConfigurations;

    private static final AntPathMatcher ANT_PATH_MATCHER;

    static {
        ANT_PATH_MATCHER = new AntPathMatcher();
        ANT_PATH_MATCHER.setCaseSensitive(false);
    }

    @Override
    public StaticResourceAccessValidatorResult findAccessForPath(String requestedPath) {
        if (accessConfigurations == null || accessConfigurations.isEmpty()) {
            //TODO throw proper error
            throw new RuntimeException("no static resource access mappings founds");
        }

        final List<StaticResourceAccessPolicy.PolicyEntry> policyEntryList = accessConfigurations.stream()
                .map(StaticResourceAccessPolicy::getStaticResources)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Optional<AccessLevel> accessLevel = policyEntryList.stream()
                .filter(policyEntry -> policyEntry.getPathPattern() != null
                        && ANT_PATH_MATCHER.match(policyEntry.getPathPattern(), requestedPath))
                .findFirst()
                .map(StaticResourceAccessPolicy.PolicyEntry::getRequiredAccessLevel)
                .map(AccessLevel::find);

        StaticResourceAccessValidatorResult.StaticResourceAccessValidatorResultBuilder builder =
                StaticResourceAccessValidatorResult.builder();

        accessLevel.ifPresent(builder::requiredAccessLevel);
        return builder.requestedUri(requestedPath).build();
    }
}
