package mz.org.fgh.sifmoz.backend.task;

import mz.org.fgh.sifmoz.backend.healthInformationSystem.ISystemConfigsService;
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class SynchronizerTask implements ISynchronizerTask {

    @Autowired
    ISystemConfigsService systemConfigsService;

    SystemConfigs instalationConfig;

    public final static String MOBILE_SERVER = "MOBILE";
    public final static String IDMED_SERVER = "IDMED";

    public SynchronizerTask() {
        if (systemConfigsService != null) {
            instalationConfig = systemConfigsService.getByKey("INSTALATION_TYPE")
        } else {
            SystemConfigs.withTransaction {
                instalationConfig = SystemConfigs.findByKey("INSTALATION_TYPE")
            }
        }
    }

    protected boolean isProvincial() {
        return instalationConfig.getValue().equals("PROVINCIAL")
    }

    protected String getUsOrProvince() {
        return  instalationConfig.getDescription();
    }

    protected SystemConfigs getInstalationConfig() {
        return  instalationConfig;
    }
}
