package com.kerneldc.metarbatch.util.namedparameter;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class NamedParameterSet {

	private final Map<String, NamedParameter> paramMap;

    public NamedParameterSet(Set<NamedParameter> params) {
        this.paramMap = Objects.requireNonNull(params)
            .stream()
            .collect(Collectors.toUnmodifiableMap(
                NamedParameter::getName,
                param -> param,
                (a, b) -> {
                    throw new IllegalArgumentException("Duplicate parameter name: " + a.getName());
                }));
    }

    public <T> T get(String name, Class<T> expectedType) {
        NamedParameter param = paramMap.get(name);
        if (param == null) {
            throw new NoSuchElementException("Parameter [" + name + "] not found");
        }

        Object value = param.getValue();
        if (!expectedType.isInstance(value)) {
            throw new IllegalArgumentException("Parameter [" + name + "] is not of type " + expectedType.getSimpleName());
        }

        return expectedType.cast(value);
    }

    public boolean contains(String name) {
        return paramMap.containsKey(name);
    }

    public List<NamedParameter> toList() {
        return List.copyOf(paramMap.values());
    }}
