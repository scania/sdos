package com.scania.sdos.orchestration.interfaces;


import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.model.JsonLdContextModel;

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
