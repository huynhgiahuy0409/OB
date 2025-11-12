package com.mservice.fs.onboarding.model.common.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class ContractType {
    private String name;
    private boolean applyZeroInterest;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ContractType that = (ContractType) object;
        return applyZeroInterest == that.applyZeroInterest && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, applyZeroInterest);
    }
}
