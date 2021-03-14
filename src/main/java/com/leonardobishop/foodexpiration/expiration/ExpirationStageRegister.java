package com.leonardobishop.foodexpiry.expiration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExpirationStageRegister {

    private final List<ExpirationStage> expirationStages;
    private final ExpirationStage emptyExpirationStage;
    private boolean acceptingRegistrations;

    public ExpirationStageRegister() {
        this.expirationStages = new ArrayList<>();
        this.emptyExpirationStage = new ExpirationStage("Empty Expiration Stage (please configure)", TimeUnit.MILLISECONDS, 0, new ArrayList<>());
        this.acceptingRegistrations = true;
    }

    public void clearRegistrations() {
        expirationStages.clear();
        acceptingRegistrations = true;
    }

    public void register(ExpirationStage expirationStage) {
        if (!acceptingRegistrations) {
            throw new IllegalStateException("Not currently accepting registrations!");
        }
        expirationStages.add(expirationStage);
    }

    public void finaliseRegistrations() {
        Collections.sort(expirationStages);
        Collections.reverse(expirationStages);
        acceptingRegistrations = false;
    }

    public boolean isAcceptingRegistrations() {
        return acceptingRegistrations;
    }

    public ExpirationStage getStageOf(long diff) {
        if (acceptingRegistrations) {
            throw new IllegalStateException("Registrations still being accepted!");
        }
        for (ExpirationStage expirationStage : expirationStages) {
            if (diff >= expirationStage.inMillis()) {
                return expirationStage;
            }
        }
        if (expirationStages.isEmpty()) return emptyExpirationStage;
        return expirationStages.get(0);
    }

}
