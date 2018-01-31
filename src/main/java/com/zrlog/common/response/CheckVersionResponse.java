package com.zrlog.common.response;

import com.zrlog.web.plugin.Version;

public class CheckVersionResponse {
    private boolean upgrade;
    private Version version;

    public boolean isUpgrade() {
        return upgrade;
    }

    public void setUpgrade(boolean upgrade) {
        this.upgrade = upgrade;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
}
