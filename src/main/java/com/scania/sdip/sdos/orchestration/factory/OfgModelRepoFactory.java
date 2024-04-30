package com.scania.sdip.sdos.orchestration.factory;

import com.scania.sdip.sdos.orchestration.OfgModelRepo;

public class OfgModelRepoFactory {

    private OfgModelRepoFactory(){
        //default constructor
    }

    /**
     * Return  OfgModelRepo instance.
     *
     * @return the OfgModelRepo
     */
    public static OfgModelRepo getModelRepo() {
        return new OfgModelRepo();
    }
}
