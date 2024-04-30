package com.scania.sdip.sdos.orchestration.interfaces;


import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.model.JsonLdContextModel;

public interface IMetaDataModel extends IBaseModel{

    /**
     * used to run flow graph
     *
     * @param iParameterMemory parameterMemory
     * @param context   JsonLdContextModel
     * @param serviceArguments service arguments
     */
    public void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
                    ServiceArguments serviceArguments);
}
